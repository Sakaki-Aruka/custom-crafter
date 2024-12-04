package com.github.sakakiaruka.customcrafter.customcrafter.api.search

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.*
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeType
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Container
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Enchant
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Potion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import kotlin.math.abs
import kotlin.math.max

object Search {

    class SearchResult internal constructor(
        private val vanilla: Recipe?,
        private val customs: List<CRecipe>
    ) {
        fun vanilla() = this.vanilla
        fun customs() = this.customs

        /**
         * A result of [Search.search].
         *
         * @param[vanilla] A found vanilla recipe.
         * @param[customs] Found custom recipes.
         *
         */
        // when call Search#search with natural: Boolean
        // - true: when this finds matched custom recipes, does not search about vanilla.
        // - false: always search vanilla, but this does not mean 'vanilla' is non-null.

        // why 'customs' is List?
        //  -> Cause the search method does not know recipes class implements 'hashCode' and 'equals'.
        // why this class does not support 'hashCode' and 'equals'?
        //  -> There is same reason with the above q.
    }

    // one: Boolean

    /**
     * 6x6 crafting items.
     * xxxxxx
     * xxxxxx
     * xxxxxx
     * xxxxxx
     * xxxxxx
     * xxxxxx
     *
     * zero origin & do not skip empty slots (use ItemStack#empty() )
     * A search-result is not guaranteed what is not empty.
     *
     * @param[player] A craft-request sender.
     * @param[items] Materials of crafting.
     * @param[natural] Force to search vanilla recipes or not.(true=not, false=force). The default is true.
     * @return[SearchResult] A result of a request. If you send one that contains invalid params, returns null.
     */
    fun search(player: Player, items: List<ItemStack>, natural: Boolean = true): SearchResult? {
        if (items.size != 36) return null
        val inventory: Inventory = Bukkit.createInventory(null, 54)
        val chunkedInput: List<List<ItemStack>> = items.chunked(6)
        for (y: Int in (0..<6)) {
            for (x: Int in (0..<6)) {
                val index: Int = x + y * 9
                inventory.setItem(index, chunkedInput[y][x])
            }
        }

        return search(player, inventory, natural)
    }


    internal fun search(player: Player, inventory: Inventory, natural: Boolean = true): SearchResult? {
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory)
            .takeIf { it?.isNotEmpty() == true } ?: return null

        val customs: List<CRecipe> = CustomCrafterAPI.RECIPES
            .filter { it.items.size == mapped.size }
            .filter { recipe -> permission(mapped, recipe, player) }
            .filter { recipe ->
                when (recipe.type) {
                    CRecipeType.NORMAL -> normal(mapped, recipe)
                    CRecipeType.AMORPHOUS -> amorphous(mapped, recipe)
                }
            }

        val vanilla: Recipe? =
            if (natural && customs.isNotEmpty()) null
            else VanillaSearch.search(player, inventory)

        return SearchResult(vanilla, customs)
    }

    private fun permission(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe, player: Player): Boolean {
        //
        return true
    }

    private fun normal(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Boolean {
        val basic: Boolean =
            squareSize(mapped.keys) == squareSize(recipe.items.keys)
                    && sameShape(mapped.keys, recipe)
                    && allCandidateContains(mapped, recipe)

        val inputSorted: List<ItemStack> = coordinateSort(mapped)
        val recipeSorted: List<CMatter> = coordinateSort(recipe.items)

        for ((i, m) in inputSorted.zip(recipeSorted)) {
            if (!m.mass && m.amount != 1 && i.amount != m.amount) return false
            val inOne: ItemStack = i.asOne()
            val recipeOne: CMatter = m.asOne()

            recipeOne.persistentDataContainer?.let {
                if (!recipeOne.predicatesResult(inOne, inOne.itemMeta.persistentDataContainer)) return false
            }

            if (recipeOne is CEnchantMatter) {
                if (!Enchant.enchant(inOne, recipeOne as CEnchantMatter)) return false
            }

            if (recipeOne is CEnchantmentStoreMatter && inOne.itemMeta is EnchantmentStorageMeta) {
                if (!Enchant.enchantStored(inOne, recipeOne as CEnchantmentStoreMatter)) return false
            }

            if (recipeOne is CPotionMatter && inOne.itemMeta is PotionMeta) {
                if (!Potion.potion(inOne, recipeOne as CPotionMatter)) return false
            }
        }
        return basic
    }

    private fun amorphous(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Boolean {
        val filterCandidates: List<(Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>>)> = listOf(
            // candidates
            Container.amorphous(mapped, recipe), // containers
            Enchant.amorphous(mapped, recipe), // enchants
            Enchant.storesAmorphous(mapped, recipe), // enchantStores
            // potions
        )

        val filters: List<Set<AmorphousFilterCandidate>> = filterCandidates
            .takeIf { p ->
                p.all { f -> f.first != AmorphousFilterCandidate.Type.NOT_ENOUGH }
            }?.let { slice ->
                slice
                    .filter { pair -> pair.first != AmorphousFilterCandidate.Type.NOT_REQUIRED }
                    .filter { pair -> pair.second.isNotEmpty() }
                    .map { it.second.toSet() }
            } ?: return false // have Type.NOT_ENOUGH

        if (filters.isEmpty()) return false
        val relate: Map<CoordinateComponent, CoordinateComponent> = combination(filters)
            .takeIf { it.isNotEmpty() } ?: return false
    }

    private fun combination(filters: List<Set<AmorphousFilterCandidate>>): Map<CoordinateComponent, CoordinateComponent> {
        val merged: MutableSet<AmorphousFilterCandidate> = mutableSetOf()
        for (filter: Set<AmorphousFilterCandidate> in filters) {
            for (f: AmorphousFilterCandidate in filter) {
                if (!merged.any { it.coordinate == f.coordinate }) {
                    merged.add(f)
                    continue
                }

                val a: List<CoordinateComponent> = merged.first { it.coordinate == f.coordinate }.list
                val b: List<CoordinateComponent> = f.list
                val bothContained: List<CoordinateComponent> = bothContained(a, b)
                if (bothContained.isEmpty()) return emptyMap()
                merged.add(AmorphousFilterCandidate(f.coordinate, bothContained))
            }
        }

        val conflict: MutableSet<AmorphousFilterCandidate> = mutableSetOf()
        val finished: MutableMap<CoordinateComponent, CoordinateComponent> = mutableMapOf()
        for (element: AmorphousFilterCandidate in merged) {
            if (element.list.size == 1) {
                if (!finished.containsKey(element.coordinate)) {
                    finished[element.coordinate] = element.list.first()
                    continue
                }
                return emptyMap()
            }
            conflict.add(element)
        }

        if (hasDuplicate(finished)) return emptyMap()

        // generate combination

    }

    private fun applyCombination(conflict: MutableSet<AmorphousFilterCandidate>, finished: MutableMap<CoordinateComponent, CoordinateComponent>) {
        val sizes: List<Int> = conflict.map { it.list.size }
        // ???
    }

    private fun hasDuplicate(map: Map<CoordinateComponent, CoordinateComponent>): Boolean {
        return map.values.toSet().size != map.values.size
    }

    private fun bothContained(a: List<CoordinateComponent>, b: List<CoordinateComponent>): List<CoordinateComponent> {
        val target: List<CoordinateComponent> = if (a.size <= b.size) b else a //ed
        val taker: List<CoordinateComponent> = if (a.size <= b.size) a else b //er
        return taker.filter { c -> target.contains(c) }
    }

    private fun allCandidateContains(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Boolean {
        val inputCoordinateSorted: List<CoordinateComponent> = coordinateSort(mapped.keys)
        val recipeCoordinateSorted: List<CoordinateComponent> = coordinateSort(recipe.items.keys)
        for ((ic, rc) in inputCoordinateSorted.zip(recipeCoordinateSorted)) {
            if (!recipe.items[rc]!!.candidate.contains(mapped[ic]!!.type)) return false
        }
        return true
    }

    private fun squareSize(mapped: Set<CoordinateComponent>): Int {
        val xSorted: List<CoordinateComponent> = mapped.sortedBy { it.x }
        val xGap: Int = abs(xSorted.first().x - xSorted.last().x)
        val ySorted: List<CoordinateComponent> = mapped.sortedBy { it.y }
        val yGap: Int = abs(ySorted.first().x - ySorted.last().y)
        return max(xGap, yGap)
    }

    private fun sameShape(mapped: Set<CoordinateComponent>, recipe: CRecipe): Boolean {
        if (recipe.type == CRecipeType.AMORPHOUS || mapped.size != recipe.items.size) return false
        val r: List<CoordinateComponent> = coordinateSort(recipe.items.keys)
        val i: List<CoordinateComponent> = coordinateSort(mapped)
        val set: MutableSet<Pair<Int, Int>> = mutableSetOf()
        for ((inputCoordinate, recipeCoordinate) in i.zip(r)) {
            set.add(Pair(abs(inputCoordinate.x - recipeCoordinate.x), abs(inputCoordinate.y - recipeCoordinate.y)))
        }
        return set.size == 1
    }

    private fun <T> coordinateSort(coordinates: Map<CoordinateComponent, T>): List<T> {
        val result: MutableList<T> = mutableListOf()
        for (c in coordinateSort(coordinates.keys)) {
            coordinates[c]?.let { result.add(it) }
        }
        return result
    }

    private fun coordinateSort(coordinates: Collection<CoordinateComponent>): List<CoordinateComponent> {
        val result: MutableList<CoordinateComponent> = mutableListOf()
        coordinates.groupBy { it.y }
            .let {
                for (y in it.keys.sorted()) {
                    it[y]?.let { list -> list.sortedBy { c -> c.x }
                        .forEach { c -> result.add(c) }
                    }
                }
            }
        return result
    }
}
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
import kotlin.math.asin
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

        if (filterCandidates.any { pair -> pair.first == AmorphousFilterCandidate.Type.NOT_ENOUGH }) return false

        val targets: Set<CoordinateComponent> = filterCandidates
            .map { e -> e.second.map { afc -> afc.coordinate } }
            .flatten()
            .toSet()

        val limits: MutableMap<CoordinateComponent, Int> = mutableMapOf()
        for (coordinate: CoordinateComponent in targets) {
            filterCandidates
                .filter { it.first != AmorphousFilterCandidate.Type.NOT_REQUIRED }
                .let { pairs ->
                    limits[coordinate] = pairs.count { p -> p.second.any { f -> f.coordinate == coordinate } }
                }
        }

        val filters: MutableSet<AmorphousFilterCandidate> = mutableSetOf()
        for (coordinate: CoordinateComponent in targets) {
            val list: MutableList<List<CoordinateComponent>> = mutableListOf()
            filterCandidates
                .filter { it.first != AmorphousFilterCandidate.Type.SUCCESSFUL }
                .map { it.second } // List<List<AFC>>
                .forEach { l -> // List<AFC>
                    l.filter { f -> f.coordinate == coordinate }
                        .takeIf { it.count() == limits[coordinate] }
                        ?.forEach { f -> list.add(f.list) }
                        ?: return false
                }
            val merged: MutableList<CoordinateComponent> = mutableListOf()
            list.forEach { e -> merged.addAll(e) }
            filters.add(AmorphousFilterCandidate(coordinate, merged))
        }

        val confirmed: MutableMap<CoordinateComponent, CoordinateComponent> = mutableMapOf()
        val removeMarked: MutableSet<CoordinateComponent> = mutableSetOf()
        filters.filter { f -> f.list.size == 1 }
            .forEach { f ->
                confirmed[f.coordinate] = f.list.first()
                removeMarked.add(f.coordinate)
            }
        removeMarked.forEach { r -> filters.removeIf { f -> f.coordinate == r } }

        // CSP solver here
        confirmed.putAll(csp(filters).takeIf { it.isNotEmpty() } ?: return false)
        return recipe.items.size == confirmed.size
    }

    private fun csp(candidates: Set<AmorphousFilterCandidate>): Map<CoordinateComponent, CoordinateComponent> {
        val solution: MutableMap<CoordinateComponent, CoordinateComponent> = mutableMapOf()

        fun backtrack(index: Int, candidateList: List<AmorphousFilterCandidate>): Boolean {
            if (index == candidateList.size) return true
            val current: AmorphousFilterCandidate = candidateList[index]
            for (value: CoordinateComponent in current.list) {
                if (!solution.values.contains(value)) {
                    solution[current.coordinate] = value
                    // next
                    if (backtrack(index + 1, candidateList)) return true

                    // back
                    solution.remove(current.coordinate)
                }
            }
            return false
        }
        return if (backtrack(0, candidates.toList())) solution else emptyMap()
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
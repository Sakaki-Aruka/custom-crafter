package com.github.sakakiaruka.customcrafter.customcrafter.api.search

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CEnchantMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.MappedRelation
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.MappedRelationComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeType
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Container
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Enchant
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Potion
import org.bukkit.Bukkit
import org.bukkit.Material
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
        private val customs: List<Pair<CRecipe, MappedRelation>>,
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
        if (!CustomCrafterAPI.isCustomCrafterGUI(inventory) || CustomCrafterAPI.isGUITooOld(inventory)) return null
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory)
            .takeIf { it?.isNotEmpty() == true } ?: return null

        val customs: List<Pair<CRecipe, MappedRelation>> = CustomCrafterAPI.RECIPES
            .filter { it.items.size == mapped.size }
            .map { recipe ->
                val relation: MappedRelation? =
                    when (recipe.type) {
                        CRecipeType.NORMAL -> {
                            if (normal(mapped, recipe)) {
                                val components: Set<MappedRelationComponent> =
                                    recipe.items.keys.zip(mapped.keys)
                                        .map { MappedRelationComponent(it.first, it.second) }.toSet()
                                MappedRelation(components)
                            }
                            else null
                        }
                        CRecipeType.AMORPHOUS -> amorphous(mapped, recipe)
                    }
                Pair(recipe, relation)
            }
            .filter { p -> p.second != null }
            .map { Pair(it.first, it.second as MappedRelation) }

        val vanilla: Recipe? =
            if (natural && customs.isNotEmpty()) null
            else VanillaSearch.search(player, inventory)

        return SearchResult(vanilla, customs)
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
                if (!Enchant.enchant(inOne, recipeOne)) return false
            }

            if (recipeOne is CEnchantmentStoreMatter && inOne.itemMeta is EnchantmentStorageMeta) {
                if (!Enchant.enchantStored(inOne, recipeOne)) return false
            }

            if (recipeOne is CPotionMatter && inOne.itemMeta is PotionMeta) {
                if (!Potion.potion(inOne, recipeOne)) return false
            }
        }
        return basic
    }

    private fun candidateAmorphous(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items.keys.toList()
        val inputs: List<CoordinateComponent> = mapped.keys.toList()

        if (recipes.size > inputs.size) return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
        else if (recipes.isEmpty()) return Pair(AmorphousFilterCandidate.Type.NOT_REQUIRED, emptyList())

        val map: MutableMap<Int, List<Int>> = mutableMapOf()
        for (index: Int in recipes.indices) {
            val candidates: Set<Material> = recipe.items[recipes[index]]!!.candidate
            val matched: List<Int> = inputs
                .withIndex()
                .filter { (_, component) ->
                    val inType: Material = mapped[component]!!.type
                    candidates.contains(inType)
                }
                .map { (index, _) -> index }
            map[index] = matched
        }

        val result: MutableList<AmorphousFilterCandidate> = mutableListOf()
        for (slice in map.entries) {
            val R: CoordinateComponent = recipes[slice.key]
            val list: List<CoordinateComponent> = inputs
                .withIndex()
                .filter { slice.value.contains(it.index) }
                .map { it.value }
            result.add(AmorphousFilterCandidate(R, list))
        }

        val type =
            if (result.isEmpty()) AmorphousFilterCandidate.Type.NOT_ENOUGH
            else AmorphousFilterCandidate.Type.SUCCESSFUL

        return Pair(type, result)
    }

    private fun amorphous(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): MappedRelation? {
        val filterCandidates: List<(Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>>)> = listOf(
            candidateAmorphous(mapped, recipe), // candidates
            Container.amorphous(mapped, recipe), // containers
            Enchant.amorphous(mapped, recipe), // enchants
            Enchant.storesAmorphous(mapped, recipe), // enchantStores
            Potion.amorphous(mapped, recipe)// potions
        )

        if (filterCandidates.any { pair -> pair.first == AmorphousFilterCandidate.Type.NOT_ENOUGH }) return null//return false

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
                        ?: return null//return false
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
        //confirmed.putAll(csp(filters).takeIf { it.isNotEmpty() } ?: return null)//return false)
        // type =List<MappedRelation>?
        return solveCSP(filters, mapped, recipe)
            .firstOrNull()
            ?.let { r ->
                val components: MutableSet<MappedRelationComponent> = mutableSetOf()
                confirmed.forEach { (recipe, input) ->
                    components.add(MappedRelationComponent(recipe, input))
                }
                r.components.forEach { c -> components.add(c) }
                MappedRelation(components)
            }
        //return recipe.items.size == confirmed.size
    }

    private fun solveCSP(
        candidates: Set<AmorphousFilterCandidate>,
        mapped: Map<CoordinateComponent, ItemStack>,
        cRecipe: CRecipe
    ): List<MappedRelation> {//List<Map<CoordinateComponent, CoordinateComponent>> {
        //val solutions: MutableList<MappedRelationComponent> = mutableListOf()
        val solutions = mutableListOf<Map<CoordinateComponent, CoordinateComponent>>()
        val currentSolution = mutableMapOf<CoordinateComponent, CoordinateComponent>()

        fun backtrack(index: Int, candidateList: List<AmorphousFilterCandidate>) {
            if (index == candidateList.size) {
                // 全ての候補を割り当てた
                solutions.add(currentSolution.toMap())
                return
            }

            val current = candidateList[index]
            for (value in current.list) {
                // 現在の解に値が既に使われていないかチェック
                if (!currentSolution.values.contains(value)) {
                    currentSolution[current.coordinate] = value

                    // 次の候補に進む
                    backtrack(index + 1, candidateList)

                    // 戻り操作
                    currentSolution.remove(current.coordinate)
                }
            }
        }

        backtrack(0, candidates.toList())

        return solutions
            .map { map ->
            val components: MutableSet<MappedRelationComponent> = mutableSetOf()
            val temporalInventory: Inventory = Bukkit.createInventory(null, 54)
            val newItems: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
            map.entries.forEach { (recipe, input) ->
                components.add(MappedRelationComponent(recipe, input))
                val inputIndex: Int = input.x + input.y * 9
                temporalInventory.setItem(inputIndex, mapped[input])
                newItems[input] = cRecipe.items[recipe]!!
            }

            MappedRelation(components)
            }
            .filter { r ->
                val newItems: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
                val temporaryInventory: Inventory = Bukkit.createInventory(null, 54)
                r.components.forEach { component ->
                    val matter: CMatter = cRecipe.items[component.recipe]!!
                    newItems[component.input] = matter
                    val c: CoordinateComponent = component.input
                    val index: Int = c.x + c.y * 9
                    temporaryInventory.setItem(index, mapped[c])
                }
                val replaced: CRecipe = cRecipe.replaceItems(newItems)
                normal(mapped, replaced)
            }
        //return solutions
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
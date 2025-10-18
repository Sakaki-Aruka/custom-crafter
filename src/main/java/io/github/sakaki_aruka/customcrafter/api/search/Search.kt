package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.recipe.CVanillaRecipe
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import java.util.UUID
import kotlin.math.abs

object Search {

    /**
     * A result of [Search.search].
     *
     * @param[vanilla] A found vanilla recipe.
     * @param[customs] Found custom recipes.
     *
     */
    class SearchResult internal constructor(
        private val vanilla: Recipe?,
        private val customs: List<Pair<CRecipe, MappedRelation>>,
    ) {
        /**
         * returns a nullable vanilla recipe.
         *
         * When below situations, [vanilla] is null.
         * - If [customs] is not empty and a search query's 'natural' is true.
         * - If an input is not matched all registered vanilla recipes.
         * @return[vanilla] PaperMCs Recipe ([Recipe]). This is NOT a [CRecipe].
         */
        fun vanilla() = this.vanilla

        /**
         * returns custom recipes.
         *
         */
        fun customs() = this.customs

        /**
         * returns search result size that is sum of vanilla and customs.
         *
         * examples
         * ```
         * vanilla = null, customs.size = 3 -> 0 + 3 = 3
         * vanilla != null, customs.size = 0 -> 1 + 0 = 1
         * vanilla != null, customs.size = 1 -> 1 + 1 = 2
         * ```
         *
         * @return[Int] size of result
         * @since 5.0.8
         */
        fun size(): Int {
            val v: Int = if (this.vanilla != null) 1 else 0
            val c: Int = this.customs.size
            return v + c
        }

        /**
         * returns all CRecipe and relation list. (If 'vanilla' is not null, a result list contains converted CRecipe.)
         *
         * (If an element converted from a vanilla recipe, it does not contain 'MappedRelation'.)
         *
         * @return[List] = List<Pair<CRecipe, MappedRelation?>>: Result
         * @since 5.0.11
         */
        fun getMergedResults(): List<Pair<CRecipe, MappedRelation?>> {
            val result: MutableList<Pair<CRecipe, MappedRelation?>> = mutableListOf()
            this.vanilla?.let { v ->
                CVanillaRecipe.fromVanilla(v as CraftingRecipe)?.let { r -> result.add(r to null) }
            }
            this.customs.forEach { (recipe, relation) -> result.add(recipe to relation) }
            return result
        }

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
     *
     * zero origin & do not skip empty slots (do padding = use ItemStack#empty())
     *
     *  A search-result is not guaranteed what is not empty.
     *
     * @param[player] A craft-request sender.
     * @param[items] Materials of crafting. this size must equal 36(6*6).
     * @param[natural] Force to search vanilla recipes or not.(true=not, false=force). The default is true.
     * @param[onlyFirst] get only first matched custom recipe and mapped. (default = false)
     * @param[sourceRecipes] A list of searched recipes. (default = CustomCrafterAPI.getRecipes() / since 5.0.10)
     * @return[SearchResult?] A result of a request. If you send one that contains invalid params, returns null.
     */
    fun search(
        player: Player,
        items: Array<ItemStack>,
        natural: Boolean = true,
        onlyFirst: Boolean = false,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): SearchResult? {
        if (items.size != 36) return null
        val materials: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        for (y: Int in (0..<6)) {
            for (x: Int in (0..<6)) {
                val index: Int = x + y * 9
                materials[CoordinateComponent(x, y)] = items[index]
            }
        }

        val view = CraftView(materials, ItemStack.empty())
        return search(player.uniqueId, view, natural, onlyFirst, sourceRecipes)
    }

    /**
     * search main method.
     *
     * this is more recommended than [search] (use List<ItemStack>).
     *
     * @param[crafterID] a crafter's UUID
     * @param[view] input crafting gui's view
     * @param[natural] Force to search vanilla recipes or not.(true=not, false=force). The default is true.
     * @param[onlyFirst] get only first matched custom recipe and mapped. (default = false)
     * @param[sourceRecipes] A list of searched recipes. (default = CustomCrafterAPI.getRecipes() / since 5.0.10)
     * @return[SearchResult?] A result of a request. If you send one that contains invalid params, returns null.
     */
    fun search(
        crafterID: UUID,
        view: CraftView,
        natural: Boolean = true,
        onlyFirst: Boolean = false,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): SearchResult {
        val mapped: Map<CoordinateComponent, ItemStack> = view.materials

        val customs: MutableList<Pair<CRecipe, MappedRelation>> = mutableListOf()
        for (recipe in sourceRecipes.filter { r -> r.items.size == mapped.size }) {
            when (recipe.type) {
                CRecipeType.NORMAL -> {
                    if (!normal(mapped, recipe, crafterID)) {
                        continue
                    }
                    val components: Set<MappedRelationComponent> = recipe.items.entries.zip(mapped.entries)
                        .map { (recipeEntry, inputEntry) -> MappedRelationComponent(recipeEntry.key, inputEntry.key) }
                        .toSet()
                    customs.add(recipe to MappedRelation(components))
                }

                CRecipeType.AMORPHOUS -> {
                    amorphous(mapped, recipe, crafterID)?.let { relation ->
                        customs.add(recipe to relation)
                    }
                }
            }

            if (onlyFirst && customs.isNotEmpty()) {
                break
            }
        }

        val vanilla: Recipe? =
            if (natural && customs.isNotEmpty()) null
            else {
                val world: World = Bukkit.getPlayer(crafterID)
                    ?.world
                    ?: Bukkit.getWorlds().first()
                VanillaSearch.search(world, view)
            }

        return SearchResult(vanilla, customs)
    }

    private fun normal(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID,
        fromAmorphous: Boolean = false
    ): Boolean {
        val basic: Boolean =
            if (fromAmorphous) allCandidateContains(mapped, recipe)
            else {
                sameShape(mapped.keys, recipe)
                        && allCandidateContains(mapped, recipe)
            }

        val inputSorted: List<ItemStack> = coordinateSort(mapped)
        val recipeSorted: List<CMatter> = coordinateSort(recipe.items)

        for ((i, m) in inputSorted.zip(recipeSorted)) {
            if (!m.mass && m.amount != 1 && i.amount < m.amount) return false
            val inOne: ItemStack = i.asOne()
            val recipeOne: CMatter = m.asOne()

            if (!recipeOne.predicatesResult(inOne, mapped, recipe, crafterID)) return false
            recipe.filters?.let { set ->
                for (filter in set) {
                    val (type, result) = applyNormalFilters(inOne, recipeOne, filter) ?: continue
                    return when (type) {
                        CRecipeFilter.ResultType.NOT_REQUIRED -> continue
                        CRecipeFilter.ResultType.FAILED -> false
                        CRecipeFilter.ResultType.SUCCESS -> {
                            if (result) continue
                            else false
                        }
                    }
                }
            }
        }
        return basic
    }

    private inline fun <reified T : CMatter> applyNormalFilters(
        item: ItemStack,
        matter: CMatter,
        filter: CRecipeFilter<T>
    ): Pair<CRecipeFilter.ResultType, Boolean>? {
        return try {
            filter.normal(item, matter as T)
        } catch (_: Exception) { null }
    }


    private fun getAmorphousCandidateCheckResult(
        input: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe
    ): Map<Int, Set<Triple<Int, Boolean, Boolean>>> {
        // Key=RecipeSlot, Value=<InputSlot, Checked, CheckResult>
        val result: MutableMap<Int, Set<Triple<Int, Boolean, Boolean>>> = mutableMapOf()
        // map init
        for (x in 0..5) {
            for (y in 0..5) {
                val i = x + y*9
                result[i] = mutableSetOf(Triple(-1, false, false))
            }
        }

        for ((r, matter) in recipe.items) {
            val set: MutableSet<Triple<Int, Boolean, Boolean>> = mutableSetOf()
            for ((i, item) in input.entries) {
                if (matter.candidate.contains(item.type)) {
                    set.add(Triple(i.toIndex(), true, true))
                } else {
                    set.add(Triple(i.toIndex(), true, false))
                }
            }
            result[r.toIndex()] = set.toSet()
        }
        return result
    }

    private inline fun <reified T: CMatter> getAmorphousFilterCheckResult(
        input: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        filter: CRecipeFilter<T>
    ): Map<Int, Set<Triple<Int, Boolean, Boolean>>> {
        // Key=RecipeSlot, Value=<InputSlot, Checked, CheckResult>
        val result: MutableMap<Int, Set<Triple<Int, Boolean, Boolean>>> = mutableMapOf()
        // map init
        for (x in 0..5) {
            for (y in 0..5) {
                val i = x + y*9
                result[i] = mutableSetOf(Triple(-1, false, false))
            }
        }

        for ((r, matter) in recipe.items.filter { (_, m)  -> m is T }) {
            val set: MutableSet<Triple<Int, Boolean, Boolean>> = mutableSetOf()
            for ((i, item) in input.entries) {
                val (type: CRecipeFilter.ResultType, checkResult: Boolean) = applyNormalFilters(item, matter, filter) ?: continue
                val filterResult: Boolean = when (type) {
                    CRecipeFilter.ResultType.NOT_REQUIRED -> true
                    CRecipeFilter.ResultType.FAILED -> false
                    CRecipeFilter.ResultType.SUCCESS -> checkResult
                }
                set.add(Triple(i.toIndex(), true, filterResult))
            }
            result[r.toIndex()] = set.toSet()
        }
        return result
    }

    private fun getAmorphousMatterPredicatesCheckResult(
        input: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): Map<Int, Set<Triple<Int, Boolean, Boolean>>> {
        // Key=RecipeSlot, Value=<InputSlot, Checked, CheckResult>
        val result: MutableMap<Int, Set<Triple<Int, Boolean, Boolean>>> = mutableMapOf()
        // map init
        for (x in 0..5) {
            for (y in 0..5) {
                val i = x + y*9
                result[i] = mutableSetOf(Triple(-1, false, false))
            }
        }

        for ((r, matter) in recipe.items) {
            val set: MutableSet<Triple<Int, Boolean, Boolean>> = mutableSetOf()
            for ((i, item) in input.entries) {
                if (matter.hasPredicates()) {
                    set.add(Triple(i.toIndex(), true, matter.predicatesResult(item, input, recipe, crafterID)))
                }
            }
            result[r.toIndex()] = set.toSet()
        }
        return result
    }

    private fun getAmorphousAmountCheckResult(
        input: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe
    ): Map<Int, Set<Triple<Int, Boolean, Boolean>>> {
        // Key=RecipeSlot, Value=<InputSlot, Checked, CheckResult>
        val result: MutableMap<Int, Set<Triple<Int, Boolean, Boolean>>> = mutableMapOf()
        // map init
        for (x in 0..5) {
            for (y in 0..5) {
                val i = x + y*9
                result[i] = mutableSetOf(Triple(-1, false, false))
            }
        }

        for ((r, matter) in recipe.items) {
            val set: MutableSet<Triple<Int, Boolean, Boolean>> = mutableSetOf()
            for ((i, item) in input.entries) {
                val amountResult: Boolean =
                    if (matter.mass) {
                        item.amount > 0
                    } else {
                        item.amount >= matter.amount
                    }
                set.add(Triple(i.toIndex(), true, amountResult))
            }
            result[r.toIndex()] = set.toSet()
        }
        return result
    }

    private fun amorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): MappedRelation? {

        // MapKey=RecipeSlot, MapValue=<InputSlot, Checked, CheckResult>
        val results: MutableMap<Int, MutableList<Triple<Int, Boolean, Boolean>>> = mutableMapOf()

        fun addResults(resultMap: Map<Int, Set<Triple<Int, Boolean, Boolean>>>) {
            for ((k, v) in resultMap) {
                if (!results.containsKey(k)) {
                    results[k] = v.toMutableList()
                } else {
                    results[k]!!.addAll(v)
                }
            }
        }

        addResults(getAmorphousCandidateCheckResult(mapped, recipe))
        if (!recipe.filters.isNullOrEmpty()) {
            for (filter in recipe.filters!!) {
                addResults(getAmorphousFilterCheckResult(mapped, recipe, filter))
            }
        }
        addResults(getAmorphousMatterPredicatesCheckResult(mapped, recipe, crafterID))
        addResults(getAmorphousAmountCheckResult(mapped, recipe))

        //debug
        for ((k, v) in results.toSortedMap()) {
            println("k=$k, v=$v")
        }

        val merged: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
        for ((k, set) in results) {
            val candidates: MutableSet<Int> = mutableSetOf()
            val ignored: MutableSet<Int> = mutableSetOf()
            for ((slot, checked, result) in set) {
                if (slot in ignored) {
                    continue
                } else if (!checked) {
                    continue
                } else if (!result) {
                    candidates.remove(slot)
                    ignored.add(slot)
                    continue
                }

                candidates.add(slot)
            }
            merged[k] = candidates
        }

        //debug
        for ((k, v) in merged.toSortedMap()) {
            println("(merged) k=$k, v=${v.sorted()}")
        }

        val model = Model("ExactCoverProblem")
        val assignmentVars: MutableMap<Int, IntVar> = mutableMapOf()
        for ((key, possible) in merged) {
            if (possible.isEmpty()) {
                //return null
                continue
            }
            val domainValues = possible.toIntArray()
            assignmentVars[key] = model.intVar("Key_$key", domainValues)
        }
        val variablesList = assignmentVars.values.toList()
        if (variablesList.isNotEmpty()) {
            model.allDifferent(*variablesList.toTypedArray()).post()
        }

        val relationComponents: MutableSet<MappedRelationComponent> = mutableSetOf()
        if (model.solver.solve()) {
            // results found
            for ((k, v) in assignmentVars) {
                // Key=Recipe, Value=Input
                relationComponents.add(
                    MappedRelationComponent(
                        recipe = CoordinateComponent.fromIndex(k),
                        input = CoordinateComponent.fromIndex(v.value)
                    )
                )
            }
        } else {
            // not found
            return null
        }

        return if (relationComponents.isEmpty()) {
            null
        } else {
            MappedRelation(relationComponents)
        }
    }


    private fun allCandidateContains(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Boolean {
        val inputCoordinateSorted: List<CoordinateComponent> = coordinateSort(mapped.keys)
        val recipeCoordinateSorted: List<CoordinateComponent> = coordinateSort(recipe.items.keys)
        for ((ic, rc) in inputCoordinateSorted.zip(recipeCoordinateSorted)) {
            if (!recipe.items[rc]!!.candidate.contains(mapped[ic]!!.type)) return false
        }
        return true
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
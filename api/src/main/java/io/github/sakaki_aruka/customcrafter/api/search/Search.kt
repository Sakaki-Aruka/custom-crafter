package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.recipe.CVanillaRecipe
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import java.util.UUID
import java.util.concurrent.CompletableFuture

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
     * Returns a CompletableFuture that performs asynchronous searches using the input items and recipes.
     *
     * Since almost all processing in this search is done asynchronously, exceptions will occur when accessing the world or entities (due to Bukkit API's asynchronous processing limitations).
     *
     * @param[crafterID] Crafter UUID
     * @param[view] View of input slots
     * @param[sourceRecipes] Search target recipes (default = [CustomCrafterAPI.getRecipes])
     * @return[CompletableFuture] Future task of a search result
     * @since 5.0.17
     */
    @JvmStatic
    @JvmOverloads
    fun asyncSearch(
        crafterID: UUID,
        view: CraftView,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): CompletableFuture<SearchResult> {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }

        val world: World = Bukkit.getPlayer(crafterID)
            ?.world
            ?: Bukkit.getWorlds().first()
        val vanilla: Recipe? = VanillaSearch.search(world, view)

        val mapped: Map<CoordinateComponent, ItemStack> = view.materials

        val recipes: List<CRecipe> = sourceRecipes.filter { recipe ->
            mapped.size in recipe.requiresInputItemAmountMin()..recipe.requiresInputItemAmountMax()
        }

        val futures: List<CompletableFuture<Pair<CRecipe, MappedRelation>?>> = recipes.mapNotNull { recipe ->
            CompletableFuture.supplyAsync( {
                when (recipe.type) {
                    CRecipe.Type.SHAPED -> shaped(view, recipe, crafterID, isAsync = true)
                    CRecipe.Type.SHAPELESS -> shapeless(view, recipe, crafterID, isAsync = true)
                }?.let { recipe to it }
            }, InternalAPI.asyncExecutor())
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { SearchResult(vanilla, futures.mapNotNull { it.join() }) }
    }

    /**
     * We will perform recipe searches using a synchronous process.
     *
     * If there are many recipes to search or if recipes with complex search conditions exist, it may cause delays in the server's game loop and a decrease in TPS.
     *
     * @param[crafterID] a crafter's UUID
     * @param[view] input crafting gui's view
     * @param[forceSearchVanillaRecipe] Force to search vanilla recipes or not.(true=force, false=not). The default is true.
     * @param[onlyFirst] get only first matched custom recipe and mapped. (default = false)
     * @param[sourceRecipes] A list of searched recipes. (default = CustomCrafterAPI.getRecipes() / since 5.0.10)
     * @return[SearchResult] A result of a request. If you send one that contains invalid params, returns null.
     * @throws[IllegalArgumentException] Throws when 'view.materials' is empty or their size out of range 1 to 36.
     */
    @JvmStatic
    @JvmOverloads
    fun search(
        crafterID: UUID,
        view: CraftView,
        forceSearchVanillaRecipe: Boolean = true,
        onlyFirst: Boolean = false,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes(),
    ): SearchResult {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }
        val mapped: Map<CoordinateComponent, ItemStack> = view.materials

        val customs: MutableList<Pair<CRecipe, MappedRelation>> = mutableListOf()
        for (recipe in sourceRecipes.filter { r -> mapped.size in r.requiresInputItemAmountMin()..r.requiresInputItemAmountMax() }) {
            when (recipe.type) {
                CRecipe.Type.SHAPED -> shaped(view, recipe, crafterID)
                CRecipe.Type.SHAPELESS -> shapeless(view, recipe, crafterID)
            }?.let { customs.add(recipe to it) }

            if (onlyFirst && customs.isNotEmpty()) {
                break
            }
        }

        val vanilla: Recipe? =
            if (!forceSearchVanillaRecipe && customs.isNotEmpty()) null
            else {
                val world: World = Bukkit.getPlayer(crafterID)
                    ?.world
                    ?: Bukkit.getWorlds().first()
                VanillaSearch.search(world, view)
            }

        return SearchResult(vanilla, customs)
    }

    private fun shaped(
        view: CraftView,
        recipe: CRecipe,
        crafterID: UUID,
        isAsync: Boolean = false
    ): MappedRelation? {
        if (recipe.items.size < view.materials.size) {
            return null
        }

        val sortedRecipeCoordinates: List<CoordinateComponent> = recipe.items.keys.sortedBy { it.toIndex() }
        val sortedInputCoordinates: List<CoordinateComponent> = view.materials.keys.sortedBy { it.toIndex() }
        val dx: Int = sortedRecipeCoordinates.first().x - sortedInputCoordinates.first().x
        val dy: Int = sortedRecipeCoordinates.first().y - sortedInputCoordinates.first().y
        val components: MutableSet<MappedRelationComponent> = mutableSetOf()
        for (recipeCoordinate in sortedRecipeCoordinates) {
            val inputCoordinate = CoordinateComponent(recipeCoordinate.x - dx, recipeCoordinate.y - dy)
            val matter: CMatter = recipe.items.getValue(recipeCoordinate)
            val input: ItemStack = view.materials[inputCoordinate] ?: ItemStack.empty()
            if (input.type !in matter.candidate) {
                return null
            }

            if (!input.type.isAir) {
                if (matter.mass && input.amount < 1) {
                    return null
                } else if (!matter.mass && input.amount < matter.amount) {
                    return null
                }
            }

            val matterPredicateContext = CMatterPredicate.Context(recipeCoordinate, matter, input, view.materials, recipe, crafterID, isAsync = isAsync)
            if (!matter.predicatesResult(matterPredicateContext)) {
                return null
            }
            components.add(MappedRelationComponent(recipeCoordinate, inputCoordinate))
        }

        val relation = MappedRelation(components)

        val recipePredicateContext = CRecipePredicate.Context(view, crafterID, recipe, relation, isAsync = isAsync)
        if (!recipe.getRecipePredicateResults(recipePredicateContext)) {
            return null
        }

        return relation
    }


    private fun getShapelessCandidateCheckResult(
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


    private fun getShapelessMatterPredicatesCheckResult(
        input: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID,
        isAsync: Boolean = false
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
                    val ctx = CMatterPredicate.Context(r, matter, item, input, recipe, crafterID, isAsync = isAsync)
                    set.add(Triple(i.toIndex(), true, matter.predicatesResult(ctx)))
                }
            }
            result[r.toIndex()] = set.toSet()
        }
        return result
    }

    private fun getShapelessAmountCheckResult(
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

    private fun shapeless(
        view: CraftView,
        recipe: CRecipe,
        crafterID: UUID,
        isAsync: Boolean = false
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

        addResults(getShapelessCandidateCheckResult(view.materials, recipe))
        addResults(getShapelessMatterPredicatesCheckResult(view.materials, recipe, crafterID, isAsync))
        addResults(getShapelessAmountCheckResult(view.materials, recipe))

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

        if (relationComponents.isEmpty()) {
            return null
        }

        val relation = MappedRelation(relationComponents)
        val recipePredicateContext = CRecipePredicate.Context(view, crafterID, recipe, relation, isAsync = isAsync)
        if (!recipe.getRecipePredicateResults(recipePredicateContext)) {
            return null
        }

        return relation
    }
}
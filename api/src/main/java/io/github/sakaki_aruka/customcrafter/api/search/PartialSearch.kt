package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.UnPartialSearchableRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Provides partial recipe match search functionality.
 *
 * A partial match occurs when the player's current crafting grid satisfies
 * some — but not necessarily all — of a recipe's required slots.
 * This is useful for recipe hints, crafting guides, and autocomplete suggestions.
 *
 * Both [CRecipe.Type.SHAPED] and [CRecipe.Type.SHAPELESS] recipe types are supported.
 * Recipes that implement [io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.UnPartialSearchableRecipe]
 * are excluded from all partial searches.
 *
 * @see asyncPartialSearch
 */
object PartialSearch {
    /**
     * Represents the degree to which a recipe's slots are satisfied by the current input.
     */
    enum class MatchState {
        /** Every required recipe slot is covered by a corresponding input item. */
        ALL,
        /** One or more required recipe slots have no matching input item. */
        PARTIAL_NOT_ENOUGH
        ;

        /**
         * Returns `true` when this state is not [ALL].
         */
        fun isPartialMatch(): Boolean = this != ALL
    }

    /**
     * The common interface for a single partial-search result entry.
     *
     * Each instance represents one candidate recipe together with the match
     * information derived from comparing the current [CraftView] against that recipe.
     */
    interface PartialSearchResult {
        /** The candidate recipe that was evaluated. */
        val recipe: CRecipe

        /**
         * Returns the set of recipe slot coordinates that are covered by at least one input item.
         */
        fun matched(): Set<CoordinateComponent>

        /**
         * Returns the set of recipe slot coordinates that have no matching input item.
         */
        fun notEnough(): Set<CoordinateComponent>

        /**
         * Returns the [MatchState] for this result.
         *
         * The default implementation returns [MatchState.ALL] when [notEnough] is empty,
         * otherwise [MatchState.PARTIAL_NOT_ENOUGH].
         */
        fun state(): MatchState {
            return when {
                this.notEnough().isEmpty() -> MatchState.ALL
                else -> MatchState.PARTIAL_NOT_ENOUGH
            }
        }
    }

    /**
     * Partial-search result for a [CRecipe.Type.SHAPED] recipe.
     *
     * @param[recipe] The shaped recipe that was evaluated.
     * @param[relation] The coordinate mapping between matched recipe slots and input slots.
     */
    class PartialShapedResult(
        override val recipe: CRecipe,
        val relation: MappedRelation,
    ): PartialSearchResult {
        /**
         * Returns the recipe slot coordinates that are covered by [relation].
         */
        override fun matched(): Set<CoordinateComponent> {
            return this.relation.components.map { (k, _) -> k }.toSet()
        }

        /**
         * Returns the recipe slot coordinates not present in [relation].
         */
        override fun notEnough(): Set<CoordinateComponent> {
            return this.recipe.items.keys - matched()
        }
    }

    /**
     * Partial-search result for a [CRecipe.Type.SHAPELESS] recipe.
     *
     * Because shapeless recipes have no positional constraint, a recipe slot may be
     * satisfiable by more than one input slot.  [relations] captures these *weak*
     * (non-exclusive) candidate associations; the true [MatchState] is determined by
     * computing a maximum bipartite matching over [relations] in [state].
     *
     * @param[recipe] The shapeless recipe that was evaluated.
     * @param[relations] A map from each matchable recipe slot coordinate to the set of
     *   input slot coordinates that are individually compatible with that slot.
     */
    class PartialShapelessResult(
        override val recipe: CRecipe,
        internal val relations: Map<CoordinateComponent, Set<CoordinateComponent>>,
    ): PartialSearchResult {
        /**
         * Returns the recipe slot coordinates that have at least one compatible input slot.
         */
        override fun matched(): Set<CoordinateComponent> {
            return this.relations.keys
        }

        /**
         * Returns the recipe slot coordinates that have no compatible input slot.
         */
        override fun notEnough(): Set<CoordinateComponent> {
            return this.recipe.items.keys - matched()
        }

        /**
         * Returns the [MatchState] by computing a maximum bipartite matching over [relations].
         *
         * Unlike the default implementation, this override accounts for the case where a
         * single input slot appears as a candidate for multiple recipe slots — a naive
         * `notEnough().isEmpty()` check would produce a false [MatchState.ALL] in that
         * situation.  An augmenting-path algorithm (Kuhn's algorithm) is used to find
         * the true maximum matching size and compare it against [CRecipe.items].
         */
        override fun state(): MatchState {
            val inputToRecipe = mutableMapOf<CoordinateComponent, CoordinateComponent>()
            val matchCount = relations.keys.count { recipeSlot ->
                tryAugment(recipeSlot, mutableSetOf(), inputToRecipe)
            }
            return if (matchCount == recipe.items.size) MatchState.ALL else MatchState.PARTIAL_NOT_ENOUGH
        }

        private fun tryAugment(
            recipeSlot: CoordinateComponent,
            visited: MutableSet<CoordinateComponent>,
            inputToRecipe: MutableMap<CoordinateComponent, CoordinateComponent>
        ): Boolean {
            for (inputSlot in (relations[recipeSlot] ?: return false)) {
                if (!visited.add(inputSlot)) continue
                val prev = inputToRecipe[inputSlot]
                if (prev == null || tryAugment(prev, visited, inputToRecipe)) {
                    inputToRecipe[inputSlot] = recipeSlot
                    return true
                }
            }
            return false
        }

        /**
         * Returns the weak candidate associations keyed by [CMatter] instead of by coordinate.
         *
         * Each entry maps a recipe-slot matter to the set of input slot coordinates that are
         * individually compatible with it.  The set may be empty when no input matched that
         * matter, mirroring the information in [relations] with a matter-oriented key.
         *
         * @return A map from each recipe matter to the set of compatible input slot coordinates.
         */
        fun weakRelations(): Map<CMatter, Set<CoordinateComponent>> {
            return recipe.items.map { (c, matter) ->
                matter to (this.relations[c] ?: emptySet())
            }.toMap()
        }
    }

    /**
     * Runs a partial recipe search asynchronously and returns a future that resolves to
     * all [PartialSearchResult] entries whose recipes are at least partially satisfied by
     * the items currently placed in the crafting grid.
     *
     * Recipes that implement
     * [io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.UnPartialSearchableRecipe]
     * and recipes whose minimum required slot count exceeds the number of items in [view]
     * are excluded before any per-recipe work is dispatched.
     *
     * The behaviour of [searchQuery] fields:
     * - [Search.SearchQuery.searchMode] — when [Search.SearchQuery.SearchMode.ONLY_FIRST],
     *   the future completes as soon as the first non-empty per-recipe result is available
     *   and the remaining tasks are cancelled.
     * - [Search.SearchQuery.asyncContext] — propagated to per-recipe evaluations so that
     *   predicate execution and interruption signals are handled consistently.
     * - `vanillaSearchMode` — not applicable; partial search is custom-recipe only.
     *
     * @param[crafterId] UUID of the player whose crafting grid is being evaluated.
     * @param[view] Snapshot of the crafting grid to match against.
     * @param[searchQuery] Controls search behaviour such as result mode and async context.
     *   Defaults to [Search.SearchQuery.ASYNC_DEFAULT].
     * @param[sourceRecipes] The pool of recipes to search.
     *   Defaults to all registered custom recipes via [CustomCrafterAPI.getRecipes].
     * @return A [CompletableFuture] that resolves to the list of partial-search results.
     *   The list is empty when no recipe even partially matches the current input.
     * @throws IllegalArgumentException if [view] contains zero items or more than 36 items.
     */
    @JvmStatic
    @JvmOverloads
    fun asyncPartialSearch(
        crafterId: UUID,
        view: CraftView,
        searchQuery: Search.SearchQuery = Search.SearchQuery.ASYNC_DEFAULT,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): CompletableFuture<List<PartialSearchResult>> {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }

        val recipes: List<CRecipe> = sourceRecipes
            .filter { it !is UnPartialSearchableRecipe }
            .filter { recipe -> view.materials.size <= recipe.requiresInputItemAmountMax() }

        val tasks: List<CompletableFuture<List<PartialSearchResult>>> = recipes.map { recipe ->
            CompletableFuture.supplyAsync({
                when (recipe.type) {
                    CRecipe.Type.SHAPED -> shaped(view, recipe, crafterId, searchQuery)
                    CRecipe.Type.SHAPELESS -> shapeless(view, recipe, crafterId, searchQuery)
                }
            }, InternalAPI.executor)
        }

        if (searchQuery.searchMode == Search.SearchQuery.SearchMode.ONLY_FIRST) {
            val findFirst: CompletableFuture<List<PartialSearchResult>> = CompletableFuture()
            tasks.forEach { t ->
                t.thenApply { results ->
                    if (results.isNotEmpty() && findFirst.complete(results)) {
                        tasks.forEach { it.cancel(true) }
                        searchQuery.asyncContext?.interrupt()
                    }
                }
            }
            CompletableFuture.allOf(*tasks.toTypedArray()).thenRun {
                if (!findFirst.isDone) findFirst.complete(emptyList())
            }
            return findFirst
        }

        return CompletableFuture.allOf(*tasks.toTypedArray())
            .thenApply { tasks.flatMap { it.join() } }
    }

    private fun shaped(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        searchQuery: Search.SearchQuery
    ): List<PartialShapedResult> {
        val normalCheckResult: Search.SearchResult = Search.search(crafterId, view, searchQuery, listOf(recipe))
        val merged: List<Pair<CRecipe, MappedRelation>> = normalCheckResult.getMergedResults(view)

        if (normalCheckResult.size() > 0) {
            return merged.map { (_, relation) -> PartialShapedResult(recipe, relation) }
        }

        return getHoldShapeRelations(view, recipe)
            .filterNot { it.notEnough().isEmpty() } // excludes false positive (Search#search: false -> not matched)
            .filter { shapedResult ->
                shapedResult.relation.components.all { (recipeCoordinate, inputCoordinate) ->
                    val matter: CMatter = recipe.items[recipeCoordinate] ?: return@filter false
                    val item: ItemStack = view.materials[inputCoordinate] ?: return@filter false
                    if (item.type !in matter.candidate) {
                        return@filter false
                    }
                    if (matter.anyAmount) {
                        if (item.amount < 1) {
                            return@filter false
                        }
                    } else {
                        if (matter.amount > item.amount) {
                            return@filter false
                        }
                    }
                    val context = CMatterPredicate.Context(recipeCoordinate, matter, item, view.materials, recipe, crafterId, searchQuery.asyncContext)
                    matter.predicatesResult(context)
                }
            }
    }

    private fun getHoldShapeRelations(view: CraftView, recipe: CRecipe): List<PartialShapedResult> {
        if (view.materials.isEmpty()) {
            return emptyList()
        }

        val sortedOriginalInput: List<CoordinateComponent> = view.materials.keys.sortedBy { it.toIndex() }
        val packedInput: List<CoordinateComponent> = packToLeftTop(sortedOriginalInput)
        val firstInput: CoordinateComponent = packedInput.first()

        val sortedOriginalRecipe: List<CoordinateComponent> = recipe.items.keys.sortedBy { it.toIndex() }
        val packedRecipe: List<CoordinateComponent> = packToLeftTop(sortedOriginalRecipe)
        val recipePackedOriginalMap: Map<CoordinateComponent, CoordinateComponent> = packedRecipe.zip(sortedOriginalRecipe).toMap()

        if (packedInput.size > packedRecipe.size) {
            return emptyList()
        }

        val result: MutableList<PartialShapedResult> = mutableListOf()
        for (recipeCoordinate in packedRecipe) {
            val dx: Int = recipeCoordinate.x - firstInput.x
            val dy: Int = recipeCoordinate.y - firstInput.y
            val movedByDiff: List<CoordinateComponent> = packedInput.map { c ->
                CoordinateComponent(c.x + dx, c.y + dy)
            }
            if (movedByDiff.any { it.x !in (0..<6) || it.y !in (0..<6) } || !packedRecipe.containsAll(movedByDiff)) {
                continue
            }
            val movedToOriginalInput: Map<CoordinateComponent, CoordinateComponent> = movedByDiff.zip(sortedOriginalInput).toMap()

            val matched: MutableList<MappedRelationComponent> = mutableListOf()
            if (packedRecipe.size == movedByDiff.size) {
                for (r in packedRecipe) {
                    matched.add(MappedRelationComponent(recipePackedOriginalMap.getValue(r), movedToOriginalInput.getValue(r)))
                }
            } else {
                for (r in packedRecipe) {
                    if (r in movedByDiff) {
                        matched.add(MappedRelationComponent(recipePackedOriginalMap.getValue(r), movedToOriginalInput.getValue(r)))
                    }
                }
            }
            result.add(PartialShapedResult(recipe, MappedRelation(matched.toSet())))
        }
        return result
    }

    private fun shapeless(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        searchQuery: Search.SearchQuery
    ): List<PartialShapelessResult> {
        val normalCheckResult: Search.SearchResult = Search.search(crafterId, view, searchQuery, listOf(recipe))
        if (normalCheckResult.size() > 0) {
            return normalCheckResult.getMergedResults(view).map { (_, relation) ->
                val relations: Map<CoordinateComponent, Set<CoordinateComponent>> =
                    relation.components.associate { (recipeCoord, inputCoord) -> recipeCoord to setOf(inputCoord) }
                PartialShapelessResult(recipe, relations)
            }
        }

        val asyncContext = searchQuery.asyncContext
        val relations: MutableMap<CoordinateComponent, Set<CoordinateComponent>> = mutableMapOf()
        for ((recipeSlot, matter) in recipe.items) {
            if (asyncContext?.isInterrupted() == true) return emptyList()
            val validInputs: MutableSet<CoordinateComponent> = mutableSetOf()
            for ((inputSlot, item) in view.materials) {
                if (item.type !in matter.candidate) continue
                val amountOk = if (matter.anyAmount) item.amount >= 1 else item.amount >= matter.amount
                if (!amountOk) continue
                val ctx = CMatterPredicate.Context(recipeSlot, matter, item, view.materials, recipe, crafterId, asyncContext)
                if (!matter.predicatesResult(ctx)) continue
                validInputs.add(inputSlot)
            }
            if (validInputs.isNotEmpty()) relations[recipeSlot] = validInputs
        }

        return if (relations.isEmpty()) emptyList()
        else listOf(PartialShapelessResult(recipe, relations))
    }

    private fun packToLeftTop(coordinates: Collection<CoordinateComponent>): List<CoordinateComponent> {
        val dx: Int = coordinates.minOf { it.x }
        val dy: Int = coordinates.minOf { it.y }
        return coordinates.map { c -> CoordinateComponent(c.x - dx, c.y - dy) }
    }
}
package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.AsyncContext
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import java.util.UUID
import java.util.concurrent.CompletableFuture

object PartialSearch {
    class PartialSearchResult(
        val results: Map<CRecipe, List<PartialComponent>>
    ) {

        companion object {
            @JvmField
            val EMPTY = PartialSearchResult(emptyMap())
        }

        enum class MatchState {
            ALL,
            PARTIAL_NOT_ENOUGH
            ;

            fun isPartialMatch(): Boolean = this != ALL
        }

        enum class PartialMode {
            HOLD_SHAPE,
            FULL
        }

        class PartialComponent(
            val state: MatchState,
            private val matched: List<MappedRelationComponent>,
            private val unmatched: List<CoordinateComponent>
        ) {
            fun notEnough(): List<CoordinateComponent> {
                if (this.state != MatchState.PARTIAL_NOT_ENOUGH) {
                    return emptyList()
                }
                return this.unmatched
            }

            fun matched(): List<MappedRelationComponent> {
                return this.matched
            }
        }

        class PartialSearchQuery @JvmOverloads constructor(
            val searchMode: Search.SearchQuery.SearchMode,
            val partialMode: PartialMode,
            val asyncContext: AsyncContext? = null
        ) {
            companion object {
                @JvmField
                val DEFAULT = PartialSearchQuery(
                    searchMode = Search.SearchQuery.SearchMode.ALL,
                    partialMode = PartialMode.HOLD_SHAPE,
                    asyncContext = AsyncContext.ofTurnOff()
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @JvmOverloads
    fun asyncPartialSearch(
        crafterId: UUID,
        view: CraftView,
        query: PartialSearchResult.PartialSearchQuery = PartialSearchResult.PartialSearchQuery.DEFAULT,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): CompletableFuture<PartialSearchResult> {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }
//
//        val futures: List<CompletableFuture<List<Pair<CRecipe, PartialSearchResult.PartialComponent>>>> =
//            sourceRecipes.filter { it !is UnPartialSearchableRecipe }
//                .map { recipe ->
//                    CompletableFuture.supplyAsync({
//                        when (recipe.type) {
//                            CRecipe.Type.SHAPED -> shaped(view, recipe, crafterId, query.asyncContext ?: AsyncContext.ofTurnOff())
//                            else -> null // ToDo: Impl shapeless recipe search
//                        }?.map { recipe to it }
//                    }, InternalAPI.executor)
//                }
//
//        if (query.searchMode == Search.SearchQuery.SearchMode.ONLY_FIRST) {
//            return (CompletableFuture.anyOf(*futures.toTypedArray()) as CompletableFuture<Pair<CRecipe, PartialSearchResult.PartialComponent>?>)
//                .thenApply { nullableResult ->
//                    query.asyncContext?.interrupt() // sends a close signal to a shared async context
//                    futures.filterNot { it.isDone }.forEach { f -> f.cancel(true) }
//                    nullableResult?.let { PartialSearchResult(mapOf(it)) }
//                        ?: PartialSearchResult.EMPTY
//                }
            // ToDo: Fix here
//        }
//
//        return CompletableFuture.allOf(*futures.toTypedArray())
//            .thenApply {
//                PartialSearchResult(futures.mapNotNull { it.join() }
//                        .flatten()
//                        .toMap()
//                )
//            }

        return CompletableFuture.completedFuture(PartialSearch.PartialSearchResult.EMPTY) // ToDo: Remove this
    }

    private fun shaped(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        partialQuery: PartialSearchResult.PartialSearchQuery
    ): List<PartialSearchResult.PartialComponent> {
        val normalCheckResult: Search.SearchResult = Search.search(
            crafterId,
            view,
            Search.SearchQuery.ASYNC_DEFAULT,
            listOf(recipe)
        )

        val merged: List<Pair<CRecipe, MappedRelation>> = normalCheckResult.getMergedResults(view)

        if (normalCheckResult.size() > 0) {
            return merged.map { (_, relation) ->
                PartialSearchResult.PartialComponent(
                    state = PartialSearchResult.MatchState.ALL,
                    matched = relation.components.toList(),
                    unmatched = emptyList()
                )
            }
        }

        return slidePatterns(view, recipe, partialQuery.partialMode).filter {
            getMatterTestResult(
                view,
                recipe,
                crafterId,
                partialQuery.asyncContext ?: AsyncContext.ofTurnOff(),
                it
            )
        }
    }

    private fun getMatterTestResult(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        asyncContext: AsyncContext,
        component: PartialSearchResult.PartialComponent
    ): Boolean {
        return component.matched().all { relation ->
            val recipeCoordinate: CoordinateComponent = relation.recipe
            val matter: CMatter = recipe.items.getValue(recipeCoordinate)
            val predicateContext = CMatterPredicate.Context(
                coordinate = recipeCoordinate,
                matter = matter,
                input = view.materials.getValue(relation.input),
                mapped = view.materials,
                recipe = recipe,
                crafterID = crafterId,
                asyncContext = asyncContext
            )
            matter.predicatesResult(predicateContext)
                    && matter.candidate.contains(predicateContext.input.type)
                    && if (matter.anyAmount) predicateContext.input.amount >= 1 else predicateContext.input.amount >= matter.amount
        }
    }

    private fun slidePatterns(
        view: CraftView,
        recipe: CRecipe,
        mode: PartialSearchResult.PartialMode
    ): List<PartialSearchResult.PartialComponent> {
        return when(mode) {
            PartialSearchResult.PartialMode.HOLD_SHAPE -> getHoldShapeRelations(view, recipe)
            PartialSearchResult.PartialMode.FULL -> emptyList() // ToDo: Impl here
        }.filterNot { it.state == PartialSearchResult.MatchState.ALL } // false positive (Search#search: false -> not matched)
    }

    private fun getHoldShapeRelations(view: CraftView, recipe: CRecipe): List<PartialSearchResult.PartialComponent> {
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

        val result: MutableList<PartialSearchResult.PartialComponent> = mutableListOf()
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
            val unmatched: MutableList<CoordinateComponent> = mutableListOf()
            var state: PartialSearchResult.MatchState
            if (packedRecipe.size == movedByDiff.size) {
                state = PartialSearchResult.MatchState.ALL
                for (r in packedRecipe) {
                    matched.add(MappedRelationComponent(recipePackedOriginalMap.getValue(r), movedToOriginalInput.getValue(r)))
                }
            } else {
                state = PartialSearchResult.MatchState.PARTIAL_NOT_ENOUGH
                for (r in packedRecipe) {
                    if (r in movedByDiff) {
                        matched.add(MappedRelationComponent(recipePackedOriginalMap.getValue(r), movedToOriginalInput.getValue(r)))
                    } else {
                        unmatched.add(recipePackedOriginalMap.getValue(r))
                    }
                }
            }
            result.add(PartialSearchResult.PartialComponent(state, matched, unmatched))
        }
        return result
    }

    private fun packToLeftTop(coordinates: Collection<CoordinateComponent>): List<CoordinateComponent> {
        val dx: Int = coordinates.minOf { it.x }
        val dy: Int = coordinates.minOf { it.y }
        return coordinates.map { c -> CoordinateComponent(c.x - dx, c.y - dy) }
    }

    private fun shapeless(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        asyncContext: AsyncContext
    ): List<PartialSearchResult.PartialComponent> {
        // ToDo: Impl here
        return emptyList()
    }
}
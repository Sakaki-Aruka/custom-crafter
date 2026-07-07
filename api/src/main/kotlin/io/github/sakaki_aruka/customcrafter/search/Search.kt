package io.github.sakaki_aruka.customcrafter.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.objects.AsyncContext
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.recipe.CVanillaRecipe
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
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

        companion object {
            @JvmField
            val EMPTY = SearchResult(null, emptyList())
        }

        /**
         * Returns a nullable vanilla recipe.
         *
         * [vanilla] is null in the following situations:
         * - [vanillaSearchMode][SearchQuery.VanillaSearchMode] is [SearchQuery.VanillaSearchMode.IF_CUSTOMS_NOT_FOUND] and [customs] is not empty.
         * - The input does not match any registered vanilla recipe.
         * @return[Recipe] PaperMC's [Recipe]. This is NOT a [CRecipe].
         */
        fun vanilla() = this.vanilla

        /**
         * Returns found custom recipes with their coordinate relations.
         *
         * @return[List] List of pairs of matched [CRecipe] and its [MappedRelation]
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

        /**
         * Returns all recipes and their relations, with vanilla recipes resolved against [view].
         *
         * Unlike [getMergedResults], this overload always includes the vanilla recipe with a concrete [MappedRelation].
         * @param[view] The crafting view used to compute the vanilla recipe's relation
         * @return[List] List of pairs of [CRecipe] and [MappedRelation]
         * @since 5.0.11
         */
        fun getMergedResults(view: CraftView): List<Pair<CRecipe, MappedRelation>> {
            val result: MutableList<Pair<CRecipe, MappedRelation>> = mutableListOf()
            this.vanilla?.let { v ->
                CVanillaRecipe.fromVanilla(v as CraftingRecipe)?.let { r ->
                    result.add(r to r.relateWith(view))
                }
            }
            this.customs.forEach { (recipe, relation) ->
                result.add(recipe to relation)
            }
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

    /**
     * Query of searching
     * @param[searchMode] Search mode
     * @param[vanillaSearchMode] Vanilla recipe search mode
     * @param[asyncContext] Async search context (default = null)
     * @since 5.0.20
     */
    class SearchQuery @JvmOverloads constructor(
        val searchMode: SearchMode,
        val vanillaSearchMode: VanillaSearchMode,
        val asyncContext: AsyncContext? = null
    ) {
        /**
         * Mode of searching
         * - Only-first: Only first match recipe
         * - All (Default): All recipes
         * @since 5.0.20
         */
        enum class SearchMode {
            ONLY_FIRST,
            ALL;
        }

        /**
         * Mode of vanilla recipe searching
         * - Force: Force search vanilla recipe
         * - If-customs-not-found (Default): if custom recipes not found
         * @since 5.0.20
         */
        enum class VanillaSearchMode {
            FORCE,
            IF_CUSTOMS_NOT_FOUND
        }

        companion object {
            /**
             * Default search query setting
             * @since 5.0.20
             */
            @JvmField
            val DEFAULT = SearchQuery(
                searchMode = SearchMode.ALL,
                vanillaSearchMode = VanillaSearchMode.IF_CUSTOMS_NOT_FOUND,
                asyncContext = null
            )

            /**
             * Default search query setting with async context enabled
             * @since 5.0.20
             */
            @JvmField
            val ASYNC_DEFAULT = SearchQuery(
                searchMode = SearchMode.ALL,
                vanillaSearchMode = VanillaSearchMode.IF_CUSTOMS_NOT_FOUND,
                asyncContext = AsyncContext.ofTurnOff()
            )

            /**
             * Default search query setting with a given async context
             * @since 5.0.20
             */
            @JvmStatic
            @JvmOverloads
            fun defaultModeOf(asyncContext: AsyncContext? = null): SearchQuery {
                return SearchQuery(
                    searchMode = DEFAULT.searchMode,
                    vanillaSearchMode = DEFAULT.vanillaSearchMode,
                    asyncContext = asyncContext
                )
            }
        }
    }

    // one: Boolean

    /**
     * Returns a CompletableFuture that performs asynchronous searches using the input items and recipes.
     *
     * Since almost all processing in this search is done asynchronously, exceptions will occur when accessing the world or entities (due to Bukkit API's asynchronous processing limitations).
     *
     * @param[crafterId] Crafter UUID
     * @param[view] View of input slots
     * @param[searchQuery] Query of searching (since 5.0.20)
     * @param[sourceRecipes] Search target recipes (default = [CustomCrafterAPI.getRecipes])
     * @return[CompletableFuture] Future task of a search result
     * @throws[IllegalArgumentException] If [view] materials is empty or size exceeds 36
     * @since 5.0.17
     */
    @JvmStatic
    @JvmOverloads
    fun asyncSearch(
        crafterId: UUID,
        view: CraftView,
        searchQuery: SearchQuery = SearchQuery.ASYNC_DEFAULT,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): CompletableFuture<SearchResult> {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }

        val world: World = Bukkit.getPlayer(crafterId)
            ?.world
            ?: Bukkit.getWorlds().first()
        val vanilla: Recipe? = VanillaSearch.search(world, view)

        val mapped: Map<CoordinateComponent, ItemStack> = view.materials

        val recipes: List<CRecipe> = sourceRecipes.filter { recipe ->
            mapped.size in recipe.requiresInputItemAmountMin()..recipe.requiresInputItemAmountMax()
        }

        val tasks = recipes.map { recipe ->
            CompletableFuture.supplyAsync({
                when (recipe.type) {
                    CRecipe.Type.SHAPED -> shaped(view, recipe, crafterId, searchQuery.asyncContext)
                    CRecipe.Type.SHAPELESS -> shapeless(view, recipe, crafterId, searchQuery.asyncContext)
                }?.let { mapped -> recipe to mapped }
            }, InternalAPI.executor)
        }

        if (searchQuery.searchMode == SearchQuery.SearchMode.ONLY_FIRST) {
            val findFirst: CompletableFuture<Pair<CRecipe, MappedRelation>?> = CompletableFuture()
            val derived = tasks.map { task ->
                task.thenAccept { pair ->
                    if (pair != null && findFirst.complete(pair)) {
                        searchQuery.asyncContext?.interrupt()
                    }
                }
            }
            CompletableFuture.allOf(*derived.toTypedArray()).thenRun {
                findFirst.complete(null)
            }
            return findFirst.thenApply { result ->
                SearchResult(vanilla, result?.let { listOf(it) } ?: emptyList())
            }
        }

        return CompletableFuture.allOf(*tasks.toTypedArray())
            .thenApply { SearchResult(vanilla, tasks.mapNotNull { it.join() }) }
    }

    /**
     * We will perform recipe searches using a synchronous process.
     *
     * If there are many recipes to search or if recipes with complex search conditions exist, it may cause delays in the server's game loop and a decrease in TPS.
     *
     * @param[crafterId] a crafter's UUID
     * @param[view] input crafting gui's view
     * @param[searchQuery] Query that controls search behaviour (search mode and vanilla search mode). (default = [SearchQuery.DEFAULT])
     * @param[sourceRecipes] A list of searched recipes. (default = CustomCrafterAPI.getRecipes() / since 5.0.10)
     * @return[SearchResult] A result of a search.
     * @throws[IllegalArgumentException] Throws when 'view.materials' is empty or their size out of range 1 to 36.
     */
    @JvmStatic
    @JvmOverloads
    fun search(
        crafterId: UUID,
        view: CraftView,
        searchQuery: SearchQuery = SearchQuery.DEFAULT,
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes(),
    ): SearchResult {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }
        val mapped: Map<CoordinateComponent, ItemStack> = view.materials

        val customs: MutableList<Pair<CRecipe, MappedRelation>> = mutableListOf()
        for (recipe in sourceRecipes.filter { r -> mapped.size in r.requiresInputItemAmountMin()..r.requiresInputItemAmountMax() }) {
            when (recipe.type) {
                CRecipe.Type.SHAPED -> shaped(view, recipe, crafterId)
                CRecipe.Type.SHAPELESS -> shapeless(view, recipe, crafterId)
            }?.let { customs.add(recipe to it) }

            if (searchQuery.searchMode == SearchQuery.SearchMode.ONLY_FIRST && customs.isNotEmpty()) {
                break
            }
        }

        val vanilla: Recipe? =
            if (searchQuery.vanillaSearchMode != SearchQuery.VanillaSearchMode.FORCE && customs.isNotEmpty()) null
            else {
                val world: World = Bukkit.getPlayer(crafterId)
                    ?.world
                    ?: Bukkit.getWorlds().first()
                VanillaSearch.search(world, view)
            }

        return SearchResult(vanilla, customs)
    }

    private fun shaped(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        asyncContext: AsyncContext? = null
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
                if (matter.anyAmount && input.amount < 1) {
                    return null
                } else if (!matter.anyAmount && input.amount < matter.amount) {
                    return null
                }
            }

            val matterPredicateContext = CMatterPredicate.Context(recipeCoordinate, matter, input, view.materials, recipe, crafterId,
                asyncContext = asyncContext
            )
            if (!matter.predicatesResult(matterPredicateContext)) {
                return null
            }
            components.add(MappedRelationComponent(recipeCoordinate, inputCoordinate))
        }

        val relation = MappedRelation(components)

        val recipePredicateContext = CRecipePredicate.Context(view, crafterId, recipe, relation, asyncContext)
        if (!recipe.getRecipePredicateResults(recipePredicateContext)) {
            return null
        }

        return relation
    }


    private fun shapeless(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        asyncContext: AsyncContext? = null
    ): MappedRelation? {
        // Shapeless matching is a bipartite perfect matching problem:
        // every recipe slot must be assigned a distinct input slot whose item
        // passes the slot's candidate, amount and matter-predicate checks.
        // It is solved with Kuhn's augmenting path algorithm.
        val recipeEntries: List<Map.Entry<CoordinateComponent, CMatter>> = recipe.items.entries.toList()
        val inputEntries: List<Map.Entry<CoordinateComponent, ItemStack>> = view.materials.entries.toList()
        val recipeSlots: Int = recipeEntries.size
        val inputSlots: Int = inputEntries.size
        if (recipeSlots == 0 || inputSlots < recipeSlots) {
            // fewer inputs than recipe slots can never form a full assignment
            return null
        }

        // Cheap edge mask per recipe slot: bit i is set when input i passes
        // the candidate and amount checks. Input slot counts never exceed 36
        // (< 64), so a Long bitmask per slot is sufficient.
        val cheapEdges = LongArray(recipeSlots)
        for (r in 0..<recipeSlots) {
            val matter: CMatter = recipeEntries[r].value
            var mask = 0L
            for (i in 0..<inputSlots) {
                val item: ItemStack = inputEntries[i].value
                if (item.type !in matter.candidate) {
                    continue
                }
                val amountResult: Boolean =
                    if (matter.anyAmount) {
                        item.amount > 0
                    } else {
                        item.amount >= matter.amount
                    }
                if (amountResult) {
                    mask = mask or (1L shl i)
                }
            }
            if (mask == 0L) {
                // a recipe slot without any usable input can never be assigned
                return null
            }
            cheapEdges[r] = mask
        }

        // Matter predicates are user code and may be expensive, so they run
        // lazily: only for edges that pass the cheap checks and are actually
        // probed by the matching. Results are memoized per (recipe slot,
        // input slot) pair. 0 = not evaluated, 1 = passed, 2 = failed.
        val predicateStates = Array(recipeSlots) { ByteArray(inputSlots) }

        fun edgeAllowed(r: Int, i: Int): Boolean {
            if (cheapEdges[r] and (1L shl i) == 0L) {
                return false
            }
            val matter: CMatter = recipeEntries[r].value
            if (!matter.hasPredicates()) {
                return true
            }
            when (predicateStates[r][i].toInt()) {
                1 -> return true
                2 -> return false
            }
            val ctx = CMatterPredicate.Context(
                recipeEntries[r].key,
                matter,
                inputEntries[i].value,
                view.materials,
                recipe,
                crafterId,
                asyncContext
            )
            val passed: Boolean = matter.predicatesResult(ctx)
            predicateStates[r][i] = if (passed) 1 else 2
            return passed
        }

        val inputToRecipe = IntArray(inputSlots) { -1 }
        val recipeToInput = IntArray(recipeSlots) { -1 }

        fun tryAssign(r: Int, visited: BooleanArray): Boolean {
            for (i in 0..<inputSlots) {
                if (visited[i] || !edgeAllowed(r, i)) {
                    continue
                }
                visited[i] = true
                if (inputToRecipe[i] == -1 || tryAssign(inputToRecipe[i], visited)) {
                    inputToRecipe[i] = r
                    recipeToInput[r] = i
                    return true
                }
            }
            return false
        }

        // most-constrained slots first: fewer cheap edges means fewer options
        val order: List<Int> = (0..<recipeSlots).sortedBy { cheapEdges[it].countOneBits() }
        for (r in order) {
            if (asyncContext?.isInterrupted() == true) {
                return null
            }
            if (!tryAssign(r, BooleanArray(inputSlots))) {
                return null
            }
        }

        val relationComponents: Set<MappedRelationComponent> = (0..<recipeSlots).map { r ->
            MappedRelationComponent(
                recipe = recipeEntries[r].key,
                input = inputEntries[recipeToInput[r]].key
            )
        }.toSet()

        val relation = MappedRelation(relationComponents)
        val recipePredicateContext = CRecipePredicate.Context(view, crafterId, recipe, relation, asyncContext)
        if (!recipe.getRecipePredicateResults(recipePredicateContext)) {
            return null
        }

        return relation
    }
}
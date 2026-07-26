package io.github.sakaki_aruka.customcrafter.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.debug.Explainer
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
import io.github.sakaki_aruka.customcrafter.recipe.MatchGroup
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
     * @param[explainer] Logs container instance
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
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes(),
        explainer: Explainer? = null
    ): CompletableFuture<SearchResult> {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }

        val world: World = Bukkit.getPlayer(crafterId)
            ?.world
            ?: Bukkit.getWorlds().first()
        val vanilla: Recipe? = VanillaSearch.search(world, view, explainer)
        explainer?.writeLog(Explainer.Loglevel.INFO, "[asyncSearch/vanillaSearch] vanilla recipe search done: found=${vanilla != null}")

        val mapped: Map<CoordinateComponent, ItemStack> = view.materials

        val (recipes: List<CRecipe>, excluded: List<CRecipe>) = sourceRecipes.partition { recipe ->
            mapped.size in recipe.requiresInputItemAmountMin()..recipe.requiresInputItemAmountMax()
        }

        // write basic data to an explainer
        explainer?.let { e -> logCandidateFilter(e, "asyncSearch", recipes, excluded, mapped.size) }

        val tasks = recipes.map { recipe ->
            CompletableFuture.supplyAsync({
                when (recipe.type) {
                    CRecipe.Type.SHAPED -> shaped(view, recipe, crafterId, searchQuery.asyncContext, explainer)
                    CRecipe.Type.SHAPELESS -> shapeless(view, recipe, crafterId, searchQuery.asyncContext, explainer)
                }?.let { mapped -> recipe to mapped }
            }, InternalAPI.executor)
        }

        if (searchQuery.searchMode == SearchQuery.SearchMode.ONLY_FIRST) {
            val findFirst: CompletableFuture<Pair<CRecipe, MappedRelation>?> = CompletableFuture()
            val derived = tasks.map { task ->
                task.thenAccept { pair ->
                    if (pair != null && findFirst.complete(pair)) {
                        explainer?.writeLog(Explainer.Loglevel.INFO, "[asyncSearch/onlyFirst] first match found: recipe=${pair.first.name}, remaining tasks interrupted")
                        searchQuery.asyncContext?.interrupt()
                    }
                }
            }
            CompletableFuture.allOf(*derived.toTypedArray()).thenRun {
                findFirst.complete(null)
            }
            return findFirst.thenApply { result ->
                explainer?.writeLog(Explainer.Loglevel.INFO, "[asyncSearch/onlyFirst] search finished: matched=${result != null}, vanillaFound=${vanilla != null}")
                SearchResult(vanilla, result?.let { listOf(it) } ?: emptyList())
            }
        }

        return CompletableFuture.allOf(*tasks.toTypedArray())
            .thenApply {
                val matched = tasks.mapNotNull { it.join() }
                explainer?.writeLog(Explainer.Loglevel.INFO, "[asyncSearch/all] search finished: matchedCustoms=${matched.size}, vanillaFound=${vanilla != null}")
                SearchResult(vanilla, matched)
            }
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
        explainer: Explainer? = null
    ): SearchResult {
        if (view.materials.isEmpty() || view.materials.size > 36) {
            throw IllegalArgumentException("'view#materials' size must be in range of 1 to 36. (current: ${view.materials.size})")
        }
        val mapped: Map<CoordinateComponent, ItemStack> = view.materials

        val (filteredRecipes: List<CRecipe>, excludedRecipes: List<CRecipe>) = sourceRecipes.partition { r ->
            mapped.size in r.requiresInputItemAmountMin()..r.requiresInputItemAmountMax()
        }
        explainer?.let { e -> logCandidateFilter(e, "search", filteredRecipes, excludedRecipes, mapped.size) }

        val customs: MutableList<Pair<CRecipe, MappedRelation>> = mutableListOf()
        for (recipe in filteredRecipes) {
            when (recipe.type) {
                CRecipe.Type.SHAPED -> shaped(view, recipe, crafterId, explainer = explainer)
                CRecipe.Type.SHAPELESS -> shapeless(view, recipe, crafterId, explainer = explainer)
            }?.let { customs.add(recipe to it) }

            if (searchQuery.searchMode == SearchQuery.SearchMode.ONLY_FIRST && customs.isNotEmpty()) {
                explainer?.writeLog(Explainer.Loglevel.INFO, "[search/onlyFirst] first match found: recipe=${recipe.name}, stop searching")
                break
            }
        }

        val vanilla: Recipe? =
            if (searchQuery.vanillaSearchMode != SearchQuery.VanillaSearchMode.FORCE && customs.isNotEmpty()) {
                explainer?.writeLog(Explainer.Loglevel.DEBUG, "[search/vanillaSearch] vanilla search skipped: vanillaSearchMode=${searchQuery.vanillaSearchMode}, matchedCustoms=${customs.size}")
                null
            } else {
                val world: World = Bukkit.getPlayer(crafterId)
                    ?.world
                    ?: Bukkit.getWorlds().first()
                VanillaSearch.search(world, view, explainer)
            }
        explainer?.writeLog(Explainer.Loglevel.INFO, "[search] search finished: matchedCustoms=${customs.size}, vanillaFound=${vanilla != null}")

        return SearchResult(vanilla, customs)
    }

    private fun shaped(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        asyncContext: AsyncContext? = null,
        explainer: Explainer? = null
    ): MappedRelation? {
        if (recipe.items.size < view.materials.size) {
            explainer?.writeLog(Explainer.Loglevel.DEBUG, "[shaped/sizeCheck] recipe=${recipe.name}, recipeSize=${recipe.items.size}, inputSize=${view.materials.size}")
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
                explainer?.writeLog(Explainer.Loglevel.DEBUG, "[shaped/typeCheck] recipe=${recipe.name}, coordinate=${recipeCoordinate.short()}, inputType=${input.type}, candidate=${matter.candidate}")
                return null
            }

            if (!input.type.isAir) {
                if (matter.anyAmount && input.amount < 1) {
                    explainer?.writeLog(Explainer.Loglevel.DEBUG, "[shaped/amountCheck] recipe=${recipe.name}, coordinate=${recipeCoordinate.short()}, anyAmount=true, inputAmount=${input.amount}")
                    return null
                } else if (!matter.anyAmount && input.amount < matter.amount) {
                    explainer?.writeLog(Explainer.Loglevel.DEBUG, "[shaped/amountCheck] recipe=${recipe.name}, coordinate=${recipeCoordinate.short()}, requiredAmount=${matter.amount}, inputAmount=${input.amount}")
                    return null
                }
            }

            val matterPredicateContext = CMatterPredicate.Context(recipeCoordinate, matter, input, view.materials, recipe, crafterId,
                asyncContext = asyncContext,
                explainer = explainer
            )
            if (explainer == null) {
                if (!matter.predicatesResult(matterPredicateContext)) {
                    return null
                }
            } else {
                matter.firstFailedPredicate(matterPredicateContext)?.let { (index, predicate) ->
                    explainer.writeLog(Explainer.Loglevel.DEBUG, "[shaped/matterPredicateCheck] recipe=${recipe.name}, coordinate=${recipeCoordinate.short()}, failed=${predicateLabel(predicate.name(), index, matter.predicates?.size ?: 0)}")
                    return null
                }
            }
            components.add(MappedRelationComponent(recipeCoordinate, inputCoordinate))
        }

        val relation = MappedRelation(components)

        val recipePredicateContext = CRecipePredicate.Context(view, crafterId, recipe, relation, asyncContext, explainer)
        if (explainer == null) {
            if (!recipe.getRecipePredicateResults(recipePredicateContext)) {
                return null
            }
        } else {
            recipe.firstFailedRecipePredicate(recipePredicateContext)?.let { (index, predicate) ->
                explainer.writeLog(Explainer.Loglevel.DEBUG, "[shaped/recipePredicateCheck] recipe=${recipe.name}, failed=${predicateLabel(predicate.name(), index, recipe.predicates?.size ?: 0)}")
                return null
            }
        }

        explainer?.writeLog(Explainer.Loglevel.INFO, "[shaped] recipe matched: name=${recipe.name}, relation=${relationString(relation)}")
        return relation
    }

    /**
     * Records which recipes became candidates and, for those that did not, why the input size ruled them out.
     *
     * A recipe that never reaches matching is otherwise invisible in the logs, which makes "my recipe
     * is never found" impossible to diagnose from the candidate list alone.
     */
    private fun logCandidateFilter(
        explainer: Explainer,
        caller: String,
        candidates: List<CRecipe>,
        excluded: List<CRecipe>,
        inputSize: Int
    ) {
        explainer.writeLog(Explainer.Loglevel.INFO, "[$caller/candidateFilter] candidates filtered by input size: inputSize=$inputSize, sourceRecipes=${candidates.size + excluded.size}, candidates=${candidates.size}, shaped=${candidates.count { it.type == CRecipe.Type.SHAPED }}, shapeless=${candidates.count { it.type == CRecipe.Type.SHAPELESS }}")
        candidates.withIndex().forEach { (index, recipe) ->
            explainer.writeLog(Explainer.Loglevel.DEBUG, "[$caller/candidateFilter] candidate($index): name=${recipe.name}, type=${recipe.type}, requires=${recipe.requiresInputItemAmountMin()}..${recipe.requiresInputItemAmountMax()}")
        }
        excluded.forEach { recipe ->
            explainer.writeLog(Explainer.Loglevel.DEBUG, "[$caller/candidateFilter] excluded: name=${recipe.name}, type=${recipe.type}, inputSize=$inputSize not in requires=${recipe.requiresInputItemAmountMin()}..${recipe.requiresInputItemAmountMax()}")
        }
    }

    /**
     * Renders a predicate for a diagnostic log, falling back to its index when it carries no name.
     */
    private fun predicateLabel(name: String, index: Int, total: Int): String {
        val anonymous: Boolean = name == CMatterPredicate.ANONYMOUS || name == CRecipePredicate.ANONYMOUS
        return if (anonymous) "predicate#$index/$total" else "$name (predicate#$index/$total)"
    }

    /**
     * Renders a relation as `recipeCoordinate->inputCoordinate` pairs on one line.
     */
    private fun relationString(relation: MappedRelation): String {
        return relation.components
            .sortedBy { it.recipe.toIndex() }
            .joinToString(", ") { c -> "${c.recipe.short()}->${c.input.short()}" }
    }

    /**
     * Renders a coordinate as `(x,y)` to keep diagnostic lines readable.
     */
    private fun CoordinateComponent.short(): String = "($x,$y)"


    private fun shapeless(
        view: CraftView,
        recipe: CRecipe,
        crafterId: UUID,
        asyncContext: AsyncContext? = null,
        explainer: Explainer? = null
    ): MappedRelation? {
        // Shapeless matching is generalized from a bipartite perfect matching problem into a
        // min/max-bounded assignment problem: recipe slots are partitioned into CRecipe#matchGroups
        // groups, and each group only needs its member count matched within [min, max] (not every
        // member). Plain "every slot mandatory" recipes are just the degenerate case where every
        // group has exactly one member with min == max == 1.
        //
        // This is solved as a feasible-flow-with-lower-bounds problem (the standard reduction via a
        // super source/sink over a max-flow computation), which subsumes Kuhn's augmenting path
        // matching used previously. See FlowGraph for the underlying max-flow primitive.
        val recipeEntries: List<Map.Entry<CoordinateComponent, CMatter>> = recipe.items.entries.toList()
        val inputEntries: List<Map.Entry<CoordinateComponent, ItemStack>> = view.materials.entries.toList()
        val recipeSlots: Int = recipeEntries.size
        val inputSlots: Int = inputEntries.size
        if (recipeSlots == 0) {
            explainer?.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/emptyCheck] recipe=${recipe.name}, recipeSlots=0")
            return null
        }

        val groups: List<MatchGroup> = recipe.matchGroups()
        val requiredMin: Int = groups.sumOf { it.min }
        if (inputSlots < requiredMin) {
            // fewer inputs than the groups' combined minimum can never be satisfied
            explainer?.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/minCheck] recipe=${recipe.name}, requiredMin=$requiredMin, inputSlots=$inputSlots")
            return null
        }

        val coordinateToIndex: Map<CoordinateComponent, Int> =
            recipeEntries.withIndex().associate { (idx, entry) -> entry.key to idx }

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
                asyncContext,
                explainer
            )
            val passed: Boolean = matter.predicatesResult(ctx)
            predicateStates[r][i] = if (passed) 1 else 2
            return passed
        }

        // ---- build the flow network ----
        // ss/tt: super source/sink for the lower-bound-feasibility reduction.
        // s/t: the "real" source/sink of the underlying assignment problem.
        // groupNode(g): one per MatchGroup, capacity-ranged [min, max] from s.
        // memberIn(r)/memberOut(r): a recipe slot is split so that, even if a
        //   misbehaving custom CRecipe#matchGroups() lets the same coordinate
        //   appear in more than one group, the slot can still carry at most 1
        //   unit of flow overall.
        // inputNode(i): one per physical input slot, capacity 1 into t.
        var nextId = 0
        val ss = nextId++
        val tt = nextId++
        val s = nextId++
        val t = nextId++
        val groupNodes = IntArray(groups.size) { nextId++ }
        val memberIn = IntArray(recipeSlots) { nextId++ }
        val memberOut = IntArray(recipeSlots) { nextId++ }
        val inputNodes = IntArray(inputSlots) { nextId++ }

        val graph = FlowGraph(nextId)
        graph.addEdge(t, s, recipeSlots + inputSlots + 1)

        val memberActiveEdge: Array<FlowGraph.Edge> = Array(recipeSlots) { r ->
            graph.addEdge(memberIn[r], memberOut[r], 1)
        }

        for ((groupIndex, group) in groups.withIndex()) {
            graph.addEdge(ss, groupNodes[groupIndex], group.min)
            graph.addEdge(s, tt, group.min)
            graph.addEdge(s, groupNodes[groupIndex], group.max - group.min)
            for (coordinate in group.members) {
                val r: Int = coordinateToIndex.getValue(coordinate)
                graph.addEdge(groupNodes[groupIndex], memberIn[r], 1)
            }
        }

        val memberOutOwner = IntArray(nextId) { -1 }
        for (r in 0..<recipeSlots) {
            memberOutOwner[memberOut[r]] = r
            for (i in 0..<inputSlots) {
                if (cheapEdges[r] and (1L shl i) != 0L) {
                    graph.addEdge(memberOut[r], inputNodes[i], 1)
                }
            }
        }

        val inputOwner = IntArray(nextId) { -1 }
        for (i in 0..<inputSlots) {
            inputOwner[inputNodes[i]] = i
            graph.addEdge(inputNodes[i], t, 1)
        }

        val achieved: Int = graph.maxFlow(ss, tt) { from, to ->
            if (asyncContext?.isInterrupted() == true) {
                true
            } else {
                val r = memberOutOwner[from]
                val i = inputOwner[to]
                if (r == -1 || i == -1) false else !edgeAllowed(r, i)
            }
        }
        if (achieved < requiredMin) {
            explainer?.let { e ->
                e.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/flowCheck] recipe=${recipe.name}, achieved=$achieved, requiredMin=$requiredMin")
                if (asyncContext?.isInterrupted() == true) {
                    // every edge was reported blocked by the interrupt guard, so per-slot reasons would be bogus
                    e.writeLog(Explainer.Loglevel.INFO, "[shapeless/flowCheck] recipe=${recipe.name}, matching abandoned: async context interrupted")
                    return@let
                }
                for ((groupIndex, group) in groups.withIndex()) {
                    val memberIndices: List<Int> = group.members.map { coordinateToIndex.getValue(it) }
                    val matchedCount: Int = memberIndices.count { r -> memberActiveEdge[r].residual == 0 }
                    if (matchedCount >= group.min) {
                        continue
                    }
                    e.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/flowCheck] recipe=${recipe.name}, group#$groupIndex unsatisfied: matched=$matchedCount < min=${group.min}, members=${group.members.joinToString(",") { it.short() }}")
                    for (r in memberIndices) {
                        if (memberActiveEdge[r].residual == 0) {
                            continue
                        }
                        val matter: CMatter = recipeEntries[r].value
                        val coordinate: CoordinateComponent = recipeEntries[r].key
                        val amountRequirement: String = if (matter.anyAmount) "amount>=1" else "amount>=${matter.amount}"
                        val cheapPassed: List<Int> = (0..<inputSlots).filter { i -> cheapEdges[r] and (1L shl i) != 0L }
                        if (cheapPassed.isEmpty()) {
                            e.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/flowCheck] recipe=${recipe.name}, slot=${coordinate.short()} unmatched: no input passes candidate=${matter.candidate} $amountRequirement")
                            continue
                        }
                        val allowed: List<Int> = cheapPassed.filter { i -> edgeAllowed(r, i) }
                        if (allowed.isEmpty()) {
                            e.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/flowCheck] recipe=${recipe.name}, slot=${coordinate.short()} unmatched: ${cheapPassed.size} input(s) pass candidate/$amountRequirement but all rejected by matter predicates")
                        } else {
                            e.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/flowCheck] recipe=${recipe.name}, slot=${coordinate.short()} unmatched: ${allowed.size} acceptable input(s) ${allowed.map { i -> inputEntries[i].key.short() }} all taken by other slots")
                        }
                    }
                }
            }
            return null
        }

        val relationComponents: MutableSet<MappedRelationComponent> = mutableSetOf()
        for (r in 0..<recipeSlots) {
            if (memberActiveEdge[r].residual != 0) {
                // this member was not needed to satisfy its group's minimum
                continue
            }
            val matchedInput: Int = graph.edgesFrom(memberOut[r])
                .firstOrNull { edge -> !edge.isReverse && inputOwner[edge.to] != -1 && edge.residual == 0 }
                ?.let { inputOwner[it.to] }
                ?: continue
            relationComponents.add(MappedRelationComponent(recipeEntries[r].key, inputEntries[matchedInput].key))
        }

        val relation = MappedRelation(relationComponents)
        val recipePredicateContext = CRecipePredicate.Context(view, crafterId, recipe, relation, asyncContext, explainer)
        if (explainer == null) {
            if (!recipe.getRecipePredicateResults(recipePredicateContext)) {
                return null
            }
        } else {
            recipe.firstFailedRecipePredicate(recipePredicateContext)?.let { (index, predicate) ->
                explainer.writeLog(Explainer.Loglevel.DEBUG, "[shapeless/recipePredicateCheck] recipe=${recipe.name}, failed=${predicateLabel(predicate.name(), index, recipe.predicates?.size ?: 0)}")
                return null
            }
        }

        explainer?.writeLog(Explainer.Loglevel.INFO, "[shapeless] recipe matched: name=${recipe.name}, relation=${relationString(relation)}")
        return relation
    }
}
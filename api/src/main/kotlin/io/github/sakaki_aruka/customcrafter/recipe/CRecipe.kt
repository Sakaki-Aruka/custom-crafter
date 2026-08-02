package io.github.sakaki_aruka.customcrafter.recipe

import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

/**
 * This interface's implementing types can be used as recipes for CustomCrafter.
 *
 * @see[CMatter]
 * @see[ResultSupplier]
 * @see[CRecipe.Type]
 */
interface CRecipe {
    /** Name of this recipe. */
    val name: String
    /** Mapping of [CMatter] to their coordinates on the crafting slots. */
    val items: Map<CoordinateComponent, CMatter>
    /** List of [CRecipePredicate] that run during recipe matching. `null` or empty means no additional conditions. */
    val predicates: List<CRecipePredicate>?
    /** List of [ResultSupplier] that provide items to players. `null` or empty means no items are produced. */
    val results: List<ResultSupplier>?
    /** Type of this recipe. See [CRecipe.Type]. */
    val type: Type

    /**
     * Type of [CRecipe]
     */
    enum class Type(
        val type: String
    ) {
        SHAPED("SHAPED"),
        SHAPELESS("SHAPELESS");
    }

    /**
     * Validates this [CRecipe].
     *
     * CRecipe's default implementation checks below conditions.
     * - [CRecipe.items] size is in range 1 to 36
     * - All contained [CMatter] pass [CMatter.isValidMatter]
     *
     * ```kotlin
     * // (Usage)
     * val recipe: CRecipe = ~~~
     * recipe.isValidRecipe()
     * ```
     * @throws[IllegalStateException] If this recipe is invalid
     * @since 5.0.15
     */
    fun isValidRecipe() {
        if (this.items.isEmpty() || this.items.size > 36) {
            throw IllegalStateException("'items' must contain 1 to 36 valid CMatters.")
        }

        val builder = StringBuilder()
        for ((c, matter) in this.items.entries) {
            try {
                matter.isValidMatter()
            } catch (t: IllegalStateException) {
                builder.append("[items] x: ${c.x}, y: ${c.y}, ${t.message} ${System.lineSeparator()}")
            }
        }
        if (builder.isNotEmpty()) {
            throw IllegalStateException(builder.toString())
        }
    }

    /**
     * Groups of recipe slots used by [CRecipe.Type.SHAPELESS] matching.
     *
     * Each [MatchGroup] states how many of its members must actually be filled by an input
     * item; a slot whose coordinate is not covered is simply never assigned an input, no
     * `Material.AIR` placeholder candidate required (contrast [GroupRecipe]).
     *
     * The default implementation treats every entry of [items] as its own mandatory group
     * (`min == max == 1`), which reproduces plain full-matching SHAPELESS behaviour and keeps
     * [requiresInputItemAmountMin] / [requiresInputItemAmountMax] correct without any extra
     * overrides. Implementations that override this to express optional/aggregate slots
     * (e.g. [ShapelessGroupRecipe]) do not need to separately override those two methods.
     *
     * Ignored by [CRecipe.Type.SHAPED] matching, which stays purely coordinate-based.
     *
     * Contract: every [MatchGroup.members] coordinate returned here must be a key of [items];
     * violating this causes an exception during search.
     *
     * @return[List] Match groups covering (all or part of) [items].
     * @see[MatchGroup]
     * @since 5.3.0
     */
    fun matchGroups(): List<MatchGroup> = this.items.keys.map { MatchGroup.mandatory(it) }

    /**
     * Minimal requires input items amount
     *
     * Default implementation derives this from [matchGroups] (sum of each group's [MatchGroup.min]).
     *
     * @return[Int] Minimal requires input items amount
     * @since 5.0.15
     */
    fun requiresInputItemAmountMin(): Int = this.matchGroups().sumOf { it.min }

    /**
     * Maximum requires input items amount. Inclusive
     *
     * Default implementation derives this from [matchGroups] (sum of each group's [MatchGroup.max]).
     *
     * @return[Int] Maximum requires input items amount
     * @since 5.0.15
     */
    fun requiresInputItemAmountMax(): Int = this.matchGroups().sumOf { it.max }

    /**
     * Returns [CRecipePredicate] inspection result
     * @param[context] Context of inspection
     * @param[whenEmptyDefault] Result returned when [CRecipe.predicates] is null or empty (default = true)
     * @return[Boolean] Result of run tests
     * @since 5.0.17
     */
    fun getRecipePredicateResults(
        context: CRecipePredicate.Context,
        whenEmptyDefault: Boolean = true
    ): Boolean {
        return this.predicates?.let { it.all { predicate -> predicate.test(context) } } ?: whenEmptyDefault
    }

    /**
     * Returns the first [CRecipePredicate] that rejects [context], or `null` when all of them pass.
     *
     * Evaluates predicates in order and stops at the first failure, exactly as
     * [getRecipePredicateResults] does, so this can be used in its place when the caller needs to
     * report which predicate failed rather than only whether one did. The returned index is the
     * position within [predicates] and identifies a predicate that did not override
     * [CRecipePredicate.name].
     *
     * @param[context] Context of inspection
     * @return[Pair] The failing predicate's index paired with the predicate itself, or `null` when nothing failed
     * @since 5.3.0
     */
    fun firstFailedRecipePredicate(context: CRecipePredicate.Context): Pair<Int, CRecipePredicate>? {
        return this.predicates
            ?.withIndex()
            ?.firstOrNull { (_, predicate) -> !predicate.test(context) }
            ?.let { (i, predicate) -> i to predicate }
    }

    /**
     * Returns [CRecipePredicate] inspection result on async.
     *
     * Each predicate runs on a virtual thread (off the main thread).
     * Even if the given [CRecipePredicate.Context.asyncContext] is null, an async context is set internally before predicates run.
     * @param[context] Context of inspection
     * @param[whenEmptyDefault] Result returned when [CRecipe.predicates] is null or empty (default = true)
     * @return[CompletableFuture] Result of run tests
     * @since 5.0.17
     */
    fun asyncGetRecipePredicateResults(
        context: CRecipePredicate.Context,
        whenEmptyDefault: Boolean = true
    ): CompletableFuture<Boolean> {
        val modifiedContext = context.toAsync()
        val predicates: List<CRecipePredicate> = this.predicates.takeIf { !it.isNullOrEmpty() }
            ?: return CompletableFuture.completedFuture(whenEmptyDefault)

        val futures: List<CompletableFuture<Boolean>> = predicates.map { predicate ->
            CompletableFuture.supplyAsync({ predicate.test(modifiedContext) }, InternalAPI.executor)
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.all { it.join() } }
    }

    /**
     * Returns results of suppliers made
     *
     * @param[context] Context of ResultSupplier
     * @return[List] Generated items list. If no item supplier applied, returns an empty list.
     * @see[ResultSupplier]
     * @see[ResultSupplier.Context]
     */
    fun getResults(context: ResultSupplier.Context): List<ItemStack> {
        return results?.let { suppliers ->
            suppliers.flatMap { s -> s.supply(context) }.toList()
        } ?: emptyList()
    }

    /**
     * Returns results of suppliers made asynchronously.
     *
     * Each supplier runs on a virtual thread (off the main thread).
     * Even if [ResultSupplier.Context.asyncContext] is null, an async context is set internally before suppliers run.
     * @param[context] Context of ResultSupplier
     * @return[CompletableFuture] Future of generated items list. If no supplier applied, completes with an empty list.
     * @see[ResultSupplier]
     * @see[ResultSupplier.Context]
     */
    fun asyncGetResults(context: ResultSupplier.Context): CompletableFuture<List<ItemStack>> {
        val modifiedContext = context.toAsync()
        val suppliers: List<ResultSupplier> = this.results ?: return CompletableFuture.completedFuture(emptyList())

        val futures: List<CompletableFuture<List<ItemStack>>> = suppliers.map { supplier ->
            CompletableFuture.supplyAsync({ supplier.supply(modifiedContext) }, InternalAPI.executor)
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.flatMap { it.join() } }
    }

    /**
     * Returns the minimum craft count calculated from input item amounts and recipe requirements.
     *
     * @param[map] Input items mapping
     * @param[relation] Coordinate relations in input items and recipe matters
     * @param[shift] Use Shift-Key (Batch Crafting) or not
     * @param[withoutMass] Exclude [CMatter.anyAmount]-marked matters from the calculation or not
     * @return[Int] Minimum craft count. Returns 1 if no constraining matter exists.
     * @since 5.0.10
     */
    fun getTimes(
        map: Map<CoordinateComponent, ItemStack>,
        relation: MappedRelation,
        shift: Boolean,
        withoutMass: Boolean = true
    ): Int {
        var amount = Int.MAX_VALUE
        for ((c, matter) in this.items) {
            if (withoutMass && matter.anyAmount) {
                continue
            }

            val inputCoordinate: CoordinateComponent = relation.components.firstOrNull { it.recipe == c }
                ?.input
                ?: continue
            val item: ItemStack = map[inputCoordinate] ?: continue

            val q: Int =
                if (matter.anyAmount) 1
                else
                    if (shift) item.amount / matter.amount
                    else
                        if (item.amount / matter.amount > 0) 1
                        else 0
            if (q < amount) {
                amount = q
            }
        }
        return amount.takeIf { it != Int.MAX_VALUE } ?: 1
    }
}
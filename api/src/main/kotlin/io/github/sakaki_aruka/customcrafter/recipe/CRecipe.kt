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
     * Returns this [CRecipe] is a valid or not.
     *
     * CRecipe's default implementation checks below conditions.
     * - [CRecipe.items] size is in range 1 to 36
     * - All contained [CMatter] pass [CMatter.isValidMatter]
     *
     * ```kotlin
     * // (Usage)
     * val recipe: CRecipe = ~~~
     * recipe.isValidRecipe().exceptionOrNull()?.let{ throw it }
     * ```
     * @return[Result] Result of check
     * @since 5.0.15
     */
    fun isValidRecipe(): Result<Unit> {
        return if (this.items.isEmpty() || this.items.size > 36) {
            Result.failure(IllegalStateException("'items' must contain 1 to 36 valid CMatters."))
        } else if (this.items.values.any { matter -> matter.isValidMatter().isFailure }) {
            val builder = StringBuilder()
            for ((c, matter) in this.items.entries) {
                val t: Throwable = matter.isValidMatter().exceptionOrNull()
                    ?: continue
                builder.append("[items] x: ${c.x}, y: ${c.y}, ${t.message} ${System.lineSeparator()}")
            }
            Result.failure(IllegalStateException(builder.toString()))
        } else {
            Result.success(Unit)
        }
    }

    /**
     * Minimal requires input items amount
     *
     * Default implementation exists
     *
     * @return[Int] Minimal requires input items amount
     * @since 5.0.15
     */
    fun requiresInputItemAmountMin(): Int = this.items.size

    /**
     * Maximum requires input items amount. Inclusive
     *
     * Default implementation exists
     *
     * @return[Int] Maximum requires input items amount
     * @since 5.0.15
     */
    fun requiresInputItemAmountMax(): Int = this.items.size

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
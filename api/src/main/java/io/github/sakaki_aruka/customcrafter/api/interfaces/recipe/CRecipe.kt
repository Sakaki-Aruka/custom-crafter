package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

/**
 * This interface's implementing types can be used as recipes for CustomCrafter.
 *
 * @param[name] Name of this recipe
 * @param[items] Mapping of CMatter and those coordinates on crafting slots
 * @param[results] List of [ResultSupplier] what provide items to players
 * @param[type] Type of this recipe. See [CRecipe.Type]
 *
 * @see[CMatter]
 * @see[ResultSupplier]
 * @see[CRecipe.Type]
 */
interface CRecipe {
    val name: String
    val items: Map<CoordinateComponent, CMatter>
    val predicates: List<CRecipePredicate>?
    val results: List<ResultSupplier>?
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
     * - [CRecipe.items] size (in range 1 to 36 ?)
     * - contained [CMatter] are all valid (Do all [CRecipe.items] elements pass [CMatter.isValidMatter]?)
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
     * Even if the given [CRecipePredicate.Context.isAsync] is false, it is unexpectedly true at runtime and provided to the executed predicates.
     * @param[context] Context of inspection
     * @param[whenEmptyDefault] Result returned when [CRecipe.predicates] is null or empty (default = true)
     * @return[CompletableFuture] Result of run tests
     * @since 5.0.17
     */
    fun asyncGetRecipePredicateResults(
        context: CRecipePredicate.Context,
        whenEmptyDefault: Boolean = true
    ): CompletableFuture<Boolean> {
        val modifiedContext = context.copyWith(isAsync = true)
        val predicates: List<CRecipePredicate> = this.predicates.takeIf { !it.isNullOrEmpty() }
            ?: return CompletableFuture.completedFuture(whenEmptyDefault)

        val futures: List<CompletableFuture<Boolean>> = predicates.map { predicate ->
            CompletableFuture.supplyAsync { predicate.test(modifiedContext) }
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.all { it.join() } }
    }

    /**
     * Returns results of suppliers made
     *
     * @param[context] Context of ResultSupplier
     * @return[MutableList] Generated items list (`MutableList<ItemStack>`). If no item supplier applied, returns an empty list.
     * @see[ResultSupplier]
     * @see[ResultSupplier.Context]
     */
    fun getResults(context: ResultSupplier.Context): List<ItemStack> {
        return results?.let { suppliers ->
            suppliers.flatMap { s -> s.supply(context) }.toMutableList()
        } ?: mutableListOf()
    }

    fun asyncGetResults(context: ResultSupplier.Context): CompletableFuture<List<ItemStack>> {
        val modifiedContext = context.copyWith(isAsync = true)
        val suppliers: List<ResultSupplier> = this.results ?: return CompletableFuture.completedFuture(emptyList())

        val futures: List<CompletableFuture<List<ItemStack>>> = suppliers.map { supplier ->
            CompletableFuture.supplyAsync { supplier.supply(modifiedContext) }
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.flatMap { it.join() } }
    }

    /**
     * Returns min amount
     *
     * @param[map] Input items mapping
     * @param[relation] Coordinate relations in input items and recipe matters
     * @param[shift] Use Shift-Key (Batch Crafting) or not
     * @param[withoutMass] Use mass-marked CMatters to min amount calculation or not
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
            if (withoutMass && matter.mass) {
                continue
            }

            val inputCoordinate: CoordinateComponent = relation.components.firstOrNull { it.recipe == c }
                ?.input
                ?: continue
            val item: ItemStack = map[inputCoordinate] ?: continue

            val q: Int =
                if (matter.mass) 1
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
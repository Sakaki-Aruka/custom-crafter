package io.github.sakaki_aruka.customcrafter.recipe

import io.github.sakaki_aruka.customcrafter.debug.Explainer
import io.github.sakaki_aruka.customcrafter.objects.AsyncContext
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import java.util.UUID

/**
 * This interface is a SAM (Single Abstract Method Interface) and only has the function ([CRecipePredicate.test]) that is executed during recipe determination.
 *
 * Refer to the documentation of [CRecipePredicate.Context] for the provided context.
 * @since 5.0.17
 */

fun interface CRecipePredicate {
    /**
     * Context for [CRecipePredicate]
     *
     * @param[input] Inspection target
     * @param[crafterId] Crafter UUID
     * @param[recipe] Recipe
     * @param[relation] Pre-result of inspection
     * @param[asyncContext] Async context (since 5.0.20). When non-null, [test] implementations should periodically check [AsyncContext.isInterrupted] and return early if true.
     * @param[explainer] Diagnostic recorder of the running search (since 5.3.0). Non-null only when the caller passed one; write to it to record why this predicate rejected the input.
     * @since 5.0.17
     */
    class Context @JvmOverloads constructor(
        val input: CraftView,
        val crafterId: UUID,
        val recipe: CRecipe,
        val relation: MappedRelation,
        val asyncContext: AsyncContext? = null,
        val explainer: Explainer? = null
    ) {
        /**
         * Returns whether this inspection is async.
         * @return[Boolean] `true` if [asyncContext] is non-null
         * @since 5.0.20
         */
        fun isAsync(): Boolean = asyncContext != null

        /**
         * If no async context exists, returns a newly added one.
         * @return[CRecipePredicate.Context] Modified context
         * @since 5.0.20
         */
        fun toAsync(): Context {
            return if (isAsync()) {
                this
            } else {
                copyWith(AsyncContext.ofTurnOff())
            }
        }

        /**
         * Copy with a given parameter
         * @param[isAsync] Called async or not
         * @return[CRecipePredicate.Context] Modified context
         * @since 5.0.17
         */
        fun copyWith(asyncContext: AsyncContext? = null): Context {
            val newAsyncContext = asyncContext ?: this.asyncContext
            return Context(input, crafterId, recipe, relation, newAsyncContext, explainer)
        }
    }

    /**
     * Inspection method.
     * If [Context.asyncContext] is non-null, periodically check [AsyncContext.isInterrupted] and return early when true to support cooperative cancellation.
     *
     * When [Context.isAsync] returns `true`, this method is invoked off the main thread.
     * Avoid calling Bukkit API that requires the main thread directly; use a scheduler (e.g. FoliaLib) if main-thread access is needed.
     *
     * @param[ctx] Context of inspection
     * @return[Boolean] Result of inspection
     * @since 5.0.17
     */
    fun test(ctx: Context): Boolean

    /**
     * Returns a label identifying this predicate in diagnostic logs.
     *
     * A lambda carries no identity of its own, so the default is [ANONYMOUS] and an
     * [io.github.sakaki_aruka.customcrafter.debug.Explainer] falls back to reporting the
     * predicate's index within [CRecipe.predicates]. Override it, or wrap the lambda with
     * [CRecipePredicates.named], to get a readable name instead.
     *
     * Combinators in [CRecipePredicates] derive their name from the predicates they combine, so a
     * composed predicate reports its structure without any extra work.
     *
     * @return[String] Label of this predicate (default = [ANONYMOUS])
     * @since 5.3.0
     */
    fun name(): String = ANONYMOUS

    companion object {
        /**
         * The [name] of a predicate that has not been given one.
         * @since 5.3.0
         */
        const val ANONYMOUS: String = "anonymous"
    }
}
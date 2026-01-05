package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
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
     * @param[crafterID] Crafter UUID
     * @param[recipe] Recipe
     * @param[relation] Pre-result of inspection
     * @param[isAsync] Called from async or not (default = false)
     * @since 5.0.17
     */
    class Context @JvmOverloads constructor(
        val input: CraftView,
        val crafterID: UUID,
        val recipe: CRecipe,
        val relation: MappedRelation,
        val isAsync: Boolean = false
    ) {
        /**
         * Copy with a given parameter
         * @param[isAsync] Called async or not
         * @return[CRecipePredicate.Context] Modified context
         * @since 5.0.17
         */
        fun copyWith(isAsync: Boolean): Context {
            return Context(input, crafterID, recipe, relation, isAsync)
        }
    }

    /**
     * Inspection method
     * @param[ctx] Context of inspection
     * @return[Boolean] Result of inspection
     * @since 5.0.17
     */
    fun test(ctx: Context): Boolean
}
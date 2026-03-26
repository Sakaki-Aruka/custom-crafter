package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.AsyncContext
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A [CMatter] predicate function.
 *
 * @see[Context]
 * @see[CMatter.predicates]
 */
fun interface CMatterPredicate {

    /**
     * Runs this operation.
     *
     * @param[ctx] Context of operation
     * @return[Boolean] Result of operation
     * @since 5.0.17
     */
    fun test(ctx: Context): Boolean

    /**
     * CMatterPredicate context
     *
     * @param[coordinate] Inspection point on a recipe mapping
     * @param[matter] Inspector
     * @param[input] Inspection target
     * @param[mapped] User input items mapping
     * @param[recipe] A CRecipe what contains a CMatterPredicate who receives this
     * @param[crafterID] Crafter UUID
     * @param[asyncContext] Async context.
     * @see[CMatterPredicate]
     */
    class Context @JvmOverloads constructor(
        val coordinate: CoordinateComponent,
        val matter: CMatter,
        val input: ItemStack,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val recipe: CRecipe,
        val crafterID: UUID,
        val asyncContext: AsyncContext? = null
    ) {
        /**
         * Returns whether this inspection is async.
         * @return[Boolean] Async or not
         * @since 5.0.20
         */
        fun isAsync(): Boolean = asyncContext == null

        /**
         * If no async context exists, returns a newly added one.
         * @return[CMatterPredicate.Context] Modified context
         * @since 5.0.20
         */
        fun toAsync(): Context {
            return if (isAsync()) {
                this
            } else {
                copyWith(asyncContext = AsyncContext.ofTurnOff())
            }
        }

        /**
         * Copy with given parameters
         * @param[matter] CMatter
         * @param[asyncContext] Async context
         * @return[CMatterPredicate.Context] Modified context
         */
        @JvmOverloads
        fun copyWith(
            matter: CMatter = this.matter,
            asyncContext: AsyncContext? = null
        ): Context {
            return Context(
                coordinate = this.coordinate,
                matter = matter,
                input = this.input,
                mapped = this.mapped,
                recipe = this.recipe,
                crafterID = this.crafterID,
                asyncContext = asyncContext
            )
        }
    }
}
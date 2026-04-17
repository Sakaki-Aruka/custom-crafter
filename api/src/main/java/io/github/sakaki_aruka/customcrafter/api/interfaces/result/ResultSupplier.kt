package io.github.sakaki_aruka.customcrafter.api.interfaces.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.AsyncContext
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Result items supplier of [CRecipe].
 */
fun interface ResultSupplier {
    fun supply(ctx: Context): List<ItemStack>

    companion object {
        /**
         * Return a single item lambda (supplier) what not consider shift click.
         * If you want to consider shift click, use [timesSingle] instead of this.
         *
         * ```
         * return ResultSupplier { listOf(item) }
         * ```
         *
         * @param[items] Supplied items
         * @return[ResultSupplier] ResultSupplier what returns a given item only once
         */
        @JvmStatic
        fun single(vararg items: ItemStack): ResultSupplier {
            return ResultSupplier { items.toList() }
        }

        /**
         * Returns a single item lambda that considers shift click.
         * If a player does not shift chick, the following second expression will not execute (returns 'item' directly.).
         *
         * This calculates result item amount with following expressions.
         * - `minimumAmount = inputAmount / recipeRequiresAmount(=[CMatter].amount)`
         * - `amount = [items].amount * minimumAmount`
         *
         * @param[items] Supplied items
         * @return[ResultSupplier] ResultSupplier what returns amount modified item
         */
        @JvmStatic
        fun timesSingle(vararg items: ItemStack): ResultSupplier {
            return ResultSupplier { context ->
                items.map { item ->
                    val cloned: ItemStack = item.clone()
                    if (context.shiftClicked) {
                        cloned.amount *= context.calledTimes
                    }
                    cloned
                }
            }
        }
    }

    /**
     * This class contains ResultSupplier parameters.
     *
     * @param[recipe] A CRecipe instance what contains this
     * @param[crafterID] Crafter UUID
     * @param[relation] Coordinate mapping between a [CRecipe] and an input Inventory
     * @param[mapped] Coordinates and input items mapping
     * @param[shiftClicked] Shift-clicked or not
     * @param[calledTimes] Calculated minimum amount with [CMatter.amount]
     * @param[callMode] Indicates whether this invocation is a real craft or an icon generation for display (since 5.0.21)
     * @param[asyncContext] Async context (since 5.0.20)
     */
    class Context @JvmOverloads constructor(
        val recipe: CRecipe,
        val relation: MappedRelation,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val shiftClicked: Boolean,
        val calledTimes: Int,
        val crafterID: UUID,
        val callMode: CallMode,
        val asyncContext: AsyncContext? = null
    ) {

        /**
         * Indicates the purpose of a [supply] invocation.
         *
         * - [CRAFT]: The player performed an actual craft. Items should be delivered normally.
         * - [ICON]: The result is used only as a display icon (e.g. in AllCandidate feature).
         *   Heavy computation may be skipped; the returned items are not given to the player.
         *
         * @since 5.0.21
         */
        enum class CallMode {
            CRAFT,
            ICON
        }
        /**
         * Copy with a given parameter.
         *
         * @param[asyncContext] Async states context
         * @return[ResultSupplier.Context] Changes applied context
         * @since 5.0.20
         */
        fun copyWith(asyncContext: AsyncContext? = null): Context {
            return Context(recipe, relation, mapped, shiftClicked, calledTimes, crafterID, callMode, asyncContext)
        }

        /**
         * If no async context exists, returns a newly added one.
         * @return[ResultSupplier.Context] Modified context
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
         * Returns whether this inspection is async.
         * @return[Boolean] Async or not
         * @since 5.0.20
         */
        fun isAsync(): Boolean = asyncContext != null
    }
}
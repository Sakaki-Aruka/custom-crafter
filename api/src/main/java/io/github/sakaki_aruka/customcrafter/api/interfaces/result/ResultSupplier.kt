package io.github.sakaki_aruka.customcrafter.api.interfaces.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
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
         * @param[item] Supplied item
         * @return[ResultSupplier] ResultSupplier what returns a given item only once
         */
        @JvmStatic
        fun single(item: ItemStack): ResultSupplier {
            return ResultSupplier { listOf(item) }
        }

        /**
         * Returns a single item lambda that considers shift click.
         * If a player does not shift chick, the following second expression will not execute (returns 'item' directly.).
         *
         * This calculates result item amount with following expressions.
         * - `minimumAmount = inputAmount / recipeRequiresAmount(=[CMatter].amount)`
         * - `amount = [item].amount * minimumAmount`
         *
         * @param[item] Supplied item
         * @return[ResultSupplier] ResultSupplier what returns amount modified item
         */
        @JvmStatic
        fun timesSingle(item: ItemStack): ResultSupplier {
            return ResultSupplier { context ->
                val cloned: ItemStack = item.clone()
                if (context.shiftClicked) {
                    cloned.amount *= context.calledTimes
                }

                listOf(cloned)
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
     * @param[isMultipleDisplayCall] `invoke` called from multiple result display item collector or not
     */
    class Context (
        val recipe: CRecipe,
        val relation: MappedRelation,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val shiftClicked: Boolean,
        val calledTimes: Int,
        val crafterID: UUID,
        val isMultipleDisplayCall: Boolean
    )
}
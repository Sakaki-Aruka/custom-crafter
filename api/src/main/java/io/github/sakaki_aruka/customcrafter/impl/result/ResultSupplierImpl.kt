package io.github.sakaki_aruka.customcrafter.impl.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import org.bukkit.inventory.ItemStack

/**
 * Default implementation of [ResultSupplier].
 *
 * ```
 * // call example from Kotlin
 * val supplier = ResultSupplierImpl { ctx ->
 *     if (ctx.crafterID == UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")) {
 *         // Only for Notch
 *         listOf(ItemStack(Material.ENCHANTED_GOLDEN_APPLE))
 *     } else {
 *         emptyList()
 *     }
 * }
 * ```
 * @param[f] Lambda expression what receives a context, returns items list
 * @since 5.0.15
 * @see[ResultSupplier]
 * @see[ResultSupplier.Context]
 */
open class ResultSupplierImpl(
    override val f: (ResultSupplier.Context) -> List<ItemStack>
): ResultSupplier {
    companion object {
        /**
         * Return a single item lambda (supplier) what not consider shift click.
         * If you want to consider shift click, use [timesSingle] instead of this.
         *
         * ```
         * return ResultSupplierImpl { _ -> listOf(item) }
         * ```
         *
         * @param[item] Supplied item
         * @return[ResultSupplierImpl] ResultSupplier what returns a given item only once
         */
        fun single(item: ItemStack): ResultSupplierImpl {
            return ResultSupplierImpl { _ -> listOf(item) }
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
         * @return[ResultSupplierImpl] ResultSupplier what returns amount modified item
         */
        fun timesSingle(item: ItemStack): ResultSupplierImpl {
            return ResultSupplierImpl { ctx ->
                if (ctx.shiftClicked) {
                    val modified = ItemStack(item)
                    modified.amount *= ctx.calledTimes
                    listOf(modified)
                } else listOf(item)
            }
        }
    }
}
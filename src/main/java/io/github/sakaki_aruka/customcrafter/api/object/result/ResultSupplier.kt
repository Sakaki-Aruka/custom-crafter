package io.github.sakaki_aruka.customcrafter.api.`object`.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A result supplier of [CRecipe].
 *
 * Function parameters
 * - [UUID]: a crafter's uuid.
 * - [MappedRelation]: a coordinate mapping between a [CRecipe] and an input Inventory
 * - [Map]<[CoordinateComponent], [ItemStack]>: a coordinate and input items mapping
 * - [MutableList]<[ItemStack]>: result items that are made by a [CRecipe]
 * - [Boolean]: shift clicked or not
 * - [Int]: calculated minimum amount with [CMatter].amount
 * ```
 * // call example from Java
 * ResultSupplier supplier = new ResultSupplier ((crafterID, relate, mapped, list, shiftClicked, calledTimes) -> List.of(ItemStack.empty()));
 *
 * // call example from Kotlin
 * val supplier = ResultSupplier { crafterID, relate, mapped, list, shiftClicked, calledTimes -> listOf(ItemStack.empty()) }
 * ```
 *
 * @param[func] function.
 * @return[ResultSupplier]
 */
data class ResultSupplier (

    val func: Function6<
            UUID,
            MappedRelation,
            Map<CoordinateComponent, ItemStack>,
            MutableList<ItemStack>,
            Boolean, //  shift clicked
            Int, // calledTimes
            List<ItemStack>> // return
) {
    operator fun invoke(
        crafterID: UUID,
        relation: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        list: MutableList<ItemStack>,
        shiftClicked: Boolean,
        calledTimes: Int
    ): List<ItemStack> = func(crafterID, relation, mapped, list, shiftClicked, calledTimes)

    companion object {
        /**
         * return a single item lambda.
         *
         * NOTICE: returns a supplier what not consider shift click
         *
         * if you want to consider shift click, use [timesSingle] instead of this.
         *
         * ```
         * return ResultSupplier { _, _, _, _, _, _ -> listOf(item) }
         * ```
         *
         * @param[item] a supplied item
         */
        fun single(item: ItemStack): ResultSupplier {
            return ResultSupplier { _, _, _, _, _, _ -> listOf(item) }
        }

        /**
         * returns a single item lambda what considers shift click.
         *
         * if a player does not shift chick, the following second expression will not execute.
         *
         * this calculates result item amount with following expressions.
         * - `minimumAmount = inputAmount / recipeRequiresAmount(=[CMatter].amount)`
         * - `amount = [item].amount * minimumAmount`
         *
         * @param[item] a supplied item
         */
        fun timesSingle(item: ItemStack): ResultSupplier {
            return ResultSupplier { _, _, _, _, shift, called ->
                if (shift) {
                    val modified = ItemStack(item)
                    modified.amount *= called
                    listOf(modified)
                } else listOf(item)
            }
        }
    }
}

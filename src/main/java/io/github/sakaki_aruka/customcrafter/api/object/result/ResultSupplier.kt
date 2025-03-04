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
 * This requires a single argument that is [ResultSupplier.Config]
 *
 * ```
 * // call example from Java
 * ResultSupplier supplier = new ResultSupplier(
 *     new ResultSupplier.Config(crafterID, relate, mapped, list, shiftClicked, calledTimes, isMultipleCalled) ->
 *             List.of(ItemStack.empty()
 *         ));
 *
 * // call example from Kotlin
 * val supplier = ResultSupplier {
 *     ResultSupplier.Config(crafterID, relate, mapped, list, shiftClicked, calledTimes, isMultipleCalled) ->
 *         listOf(ItemStack.empty())
 * }
 * ```
 *
 * @param[func] function.
 * @return[ResultSupplier]
 */
data class ResultSupplier (
    val func: Function1<ResultSupplier.Config, List<ItemStack>>,
) {
    operator fun invoke(
        config: Config
    ): List<ItemStack> = func(config)

    //TODO add data class `ResultSupplier.Config` (includes all arguments)
    /**
     * ResultSupplier's arguments
     *
     * (This class's constructor is internal. You cannot call it from your project.)
     *
     * @param[crafterID] a crafter's uuid.
     * @param[relation] a coordinate mapping between a [CRecipe] and an input Inventory
     * @param[mapped] a coordinate and input items mapping
     * @param[list] result items that are made by a [CRecipe]
     * @param[shiftClicked] shift clicked or not
     * @param[calledTimes] calculated minimum amount with [CMatter].amount
     * @param[isMultipleDisplayCall] `invoke` called from multiple result display item collector or not
     * @since 5.0.8
     */
    data class Config(
        val crafterID: UUID,
        val relation: MappedRelation,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val list: MutableList<ItemStack>,
        val shiftClicked: Boolean,
        val calledTimes: Int,
        val isMultipleDisplayCall: Boolean
    )

    companion object {
        /**
         * return a single item lambda.
         *
         * NOTICE: returns a supplier what not consider shift click
         *
         * if you want to consider shift click, use [timesSingle] instead of this.
         *
         * ```
         * return ResultSupplier { _ -> listOf(item) }
         * ```
         *
         * @param[item] a supplied item
         */
        fun single(item: ItemStack): ResultSupplier {
            return ResultSupplier { _ -> listOf(item) }
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
            return ResultSupplier { config ->
                if (config.shiftClicked) {
                    val modified = ItemStack(item)
                    modified.amount *= config.calledTimes
                    listOf(modified)
                } else listOf(item)
            }
        }
    }
}

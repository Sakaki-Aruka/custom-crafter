package io.github.sakaki_aruka.customcrafter.api.objects.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplierConfig
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A result items supplier of [CRecipe].
 *
 * ```
 * // call example from Kotlin
 * val supplier = ResultSupplier { config ->
 *     if (config.crafterID == UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")) {
 *         // Only for Notch
 *         listOf(ItemStack(Material.ENCHANTED_GOLDEN_APPLE))
 *     } else emptyList()
 * }
 * ```
 */
data class ResultSupplier (
    val func: Function1<ResultSupplierConfig, List<ItemStack>>,
) {
    operator fun invoke(
        config: ResultSupplierConfig
    ): List<ItemStack> = func(config)

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
     * @param[calledTimes] calculated minimum amount with [CMatter.amount]
     * @param[isMultipleDisplayCall] `invoke` called from multiple result display item collector or not
     *
     * @see[ResultSupplierConfig]
     * @see[AutoCraftConfig]
     * @since 5.0.8
     */
    data class NormalConfig internal constructor(
        override val relation: MappedRelation,
        override val mapped: Map<CoordinateComponent, ItemStack>,
        override val shiftClicked: Boolean,
        override val calledTimes: Int,
        override val list: MutableList<ItemStack>,
        val crafterID: UUID,
        val isMultipleDisplayCall: Boolean
      ): ResultSupplierConfig

    /**
     * A class of implements [ResultSupplierConfig].
     *
     * (This class's constructor is internal. You cannot call it from your project.)
     *
     * @param[relation] A coordinate mapping between a [CRecipe] and an input Inventory
     * @param[mapped] A coordinate and input items mapping
     * @param[shiftClicked] Shift clicked or not. The default is true on AutoCraft.
     * @param[calledTimes] Calculated minimum amount with [CMatter.amount]
     * @param[autoCrafterBlock] A block of called
     * @param[list] Result items that are made by a [CRecipe]
     *
     * @see[ResultSupplierConfig]
     * @see[NormalConfig]
     * @since 5.0.10
     */
    data class AutoCraftConfig internal constructor(
        override val relation: MappedRelation,
        override val mapped: Map<CoordinateComponent, ItemStack>,
        override val shiftClicked: Boolean = true,
        override val calledTimes: Int,
        override val list: MutableList<ItemStack>,
        val autoCrafterBlock: Block
    ): ResultSupplierConfig

    companion object {
        /**
         * Return a single item lambda (supplier) what not consider shift click.
         * If you want to consider shift click, use [timesSingle] instead of this.
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
         * Returns a single item lambda that considers shift click.
         * If a player does not shift chick, the following second expression will not execute (returns 'item' directly.).
         *
         * This calculates result item amount with following expressions.
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

        /**
         * Empty ResultSupplier (for AutoCraft only recipes)
         * ```
         * val EMPTY = ResultSupplier { _ -> emptyList() }
         * ```
         * @since 5.0.10
         */
        val EMPTY = ResultSupplier { _ -> emptyList() }
    }
}

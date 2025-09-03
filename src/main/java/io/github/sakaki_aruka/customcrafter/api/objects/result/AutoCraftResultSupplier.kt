package io.github.sakaki_aruka.customcrafter.api.objects.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplierConfig
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * A result items supplier of [io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe]
 *
 * ```kotlin
 * // call example for Kotlin
 * val supplier = AutoCraftResultSupplier { config ->
 *     if (config.autoCrafterBlock.world.name == "Eden") {
 *         listOf(ItemStack(Material.ENCHANTED_GOLDEN_APPLE))
 *     } else {
 *         emptyList()
 *     }
 * }
 * ```
 * @since 5.0.13
 */
data class AutoCraftResultSupplier(
    val f: (Config) -> List<ItemStack>
) {
    operator fun invoke(
        config: Config
    ): List<ItemStack> = f(config)

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
     * @since 5.0.13
     */
    data class Config internal constructor(
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
         * return AutoCraftResultSupplier { _ -> listOf(item) }
         * ```
         *
         * @param[item] a supplied item
         */
        fun single(item: ItemStack): AutoCraftResultSupplier {
            return AutoCraftResultSupplier { _ -> listOf(item) }
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
        fun timesSingle(item: ItemStack): AutoCraftResultSupplier {
            return AutoCraftResultSupplier { config ->
                if (config.shiftClicked) {
                    listOf(item.clone().asQuantity(item.amount * config.calledTimes))
                } else listOf(item)
            }
        }
    }
}
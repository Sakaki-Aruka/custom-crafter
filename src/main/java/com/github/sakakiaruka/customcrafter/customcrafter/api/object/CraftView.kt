package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * A view of crafting gui.
 *
 * @param[materials] A mapping of materials what were placed by a player.
 * @param[result] An item that is placed the result item slot.
 */

data class CraftView internal constructor(
    val materials: Map<CoordinateComponent, ItemStack>,
    val result: ItemStack
) {
    companion object {
        /**
         * converting an [Inventory] to [CraftView].
         * ```
         * // an example from Java.
         * Inventory gui = CustomCrafterAPI.getCraftingGUI();
         * CraftView view = CraftView.fromInventory(gui, true);
         *
         * // an example from Kotlin
         * val gui = CustomCrafterAPI.getCraftingGUI()
         * val view = CraftView.fromInventory(gui)
         * ```
         *
         * @param[inventory] convert target
         * @param[paddingAir] padding empty slots with [ItemStack.empty] or not. default value is true. (default value can only use from Kotlin.)
         * @return[CraftView?] A result of converting. If a provided inventory is not custom crafter's gui, returns Null.
         */
        fun fromInventory(
            inventory: Inventory,
            paddingAir: Boolean = true
        ): CraftView? {
            val mapped: MutableMap<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory)?.toMutableMap() ?: return null
            if (paddingAir) {
                Converter.getAvailableCraftingSlotComponents()
                    .filter { !mapped.keys.contains(it) }
                    .forEach { coordinate ->
                        mapped[coordinate] = ItemStack.empty()
                    }
            }
            val result: ItemStack = inventory.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT) ?: ItemStack.empty()
            return CraftView(mapped, result)
        }

        /**
         * converts a view to Custom Crafter's gui.
         *
         * @param[view] input [CraftView]
         * @return[Inventory] Custom Crafter's gui
         */
        fun toCraftingGUI(view: CraftView): Inventory {
            val gui: Inventory = CustomCrafterAPI.getCraftingGUI()
            view.materials.entries.forEach { (c, item) ->
                gui.setItem(c.x + c.y * 9, item)
            }
            gui.setItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT, view.result)
            return gui
        }
    }
}

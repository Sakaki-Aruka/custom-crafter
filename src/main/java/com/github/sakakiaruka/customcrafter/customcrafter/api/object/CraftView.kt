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
        internal fun fromInventory(inventory: Inventory): CraftView? {
            val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory) ?: return null
            val result: ItemStack = inventory.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT) ?: ItemStack.empty()
            return CraftView(mapped, result)
        }
    }
}

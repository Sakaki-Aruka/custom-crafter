package com.github.sakakiaruka.customcrafter.customcrafter.api.processor

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CEnchantMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object Converter {
    fun inventoryToCMatters(inventory: Inventory): Set<CMatter>? {
        return null
    }

    fun getAvailableCraftingSlots(): List<CoordinateComponent> {
        val result: MutableList<CoordinateComponent> = mutableListOf()
        (0..<6).forEach { y ->
            (0..<6).forEach { x ->
                result.add(CoordinateComponent(x, y))
            }
        }
        return result
    }

    fun standardInputMapping(inventory: Inventory, noAir: Boolean = true): Map<CoordinateComponent, ItemStack>? {
        // CoordinateComponent: zero origin (x, y both)
        if (inventory.isEmpty) return null
        val result: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()

        for (coordinate in getAvailableCraftingSlots()) {
            val index: Int = coordinate.x + coordinate.y * 9
            val item: ItemStack = inventory.getItem(index)?.takeIf { if (noAir) it.type != Material.AIR else true } ?: continue
            result[coordinate] = item
        }
        return result
    }
    
}
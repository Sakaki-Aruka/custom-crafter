package io.github.sakaki_aruka.customcrafter.impl.util

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object Converter {

    /**
     * returns materials coordinate list
     *
     * @return[List] a list of [CoordinateComponent]
     */
    fun getAvailableCraftingSlotComponents(): List<CoordinateComponent> {
        val result: MutableList<CoordinateComponent> = mutableListOf()
        (0..<6).forEach { y ->
            (0..<6).forEach { x ->
                result.add(CoordinateComponent(x, y))
            }
        }
        return result
    }

    /**
     * returns materials index list
     *
     * @return[Set<Int>] a set of [Int]
     */
    fun getAvailableCraftingSlotIndices(): Set<Int> {
        return getAvailableCraftingSlotComponents().map { it.x + it.y * 9 }.toSet()
    }


    /**
     * returns coordinates and items mapping
     * if the provided inventory is not custom crafter gui, returns null.
     *
     * @param[inventory] custom crafter gui
     * @param[noAir] not contains air slots. default true.
     * @return[Map<CoordinateComponent, ItemStack>?] Nullable Map<CoordinateComponent, ItemStack>
     */
    fun standardInputMapping(inventory: Inventory, noAir: Boolean = true): Map<CoordinateComponent, ItemStack>? {
        // CoordinateComponent: zero origin (x, y both)
        if (inventory.isEmpty) return null
        val result: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()

        for (coordinate in getAvailableCraftingSlotComponents()) {
            val index: Int = coordinate.x + coordinate.y * 9
            val item: ItemStack = inventory.getItem(index)?.takeIf { if (noAir) it.type != Material.AIR else true } ?: continue
            result[coordinate] = item
        }
        return result
    }

    /**
     * Kotlin string extension. String to Component (net.kyori.adventure)
     *
     * Usage
     * ```kotlin
     * val component = "<aqua>This is a kyori component!".toComponent()
     * ```
     */
    fun String.toComponent(): Component = MiniMessage.miniMessage().deserialize(this)

}
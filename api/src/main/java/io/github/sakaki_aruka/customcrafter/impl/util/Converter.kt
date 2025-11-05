package io.github.sakaki_aruka.customcrafter.impl.util

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object Converter {

    private val components = listOf(
        CoordinateComponent(0, 0),
        CoordinateComponent(1, 0),
        CoordinateComponent(2, 0),
        CoordinateComponent(3, 0),
        CoordinateComponent(4, 0),
        CoordinateComponent(5, 0),
        CoordinateComponent(0, 1),
        CoordinateComponent(1, 1),
        CoordinateComponent(2, 1),
        CoordinateComponent(3, 1),
        CoordinateComponent(4, 1),
        CoordinateComponent(5, 1),
        CoordinateComponent(0, 2),
        CoordinateComponent(1, 2),
        CoordinateComponent(2, 2),
        CoordinateComponent(3, 2),
        CoordinateComponent(4, 2),
        CoordinateComponent(5, 2),
        CoordinateComponent(0, 3),
        CoordinateComponent(1, 3),
        CoordinateComponent(2, 3),
        CoordinateComponent(3, 3),
        CoordinateComponent(4, 3),
        CoordinateComponent(5, 3),
        CoordinateComponent(0, 4),
        CoordinateComponent(1, 4),
        CoordinateComponent(2, 4),
        CoordinateComponent(3, 4),
        CoordinateComponent(4, 4),
        CoordinateComponent(5, 4),
        CoordinateComponent(0, 5),
        CoordinateComponent(1, 5),
        CoordinateComponent(2, 5),
        CoordinateComponent(3, 5),
        CoordinateComponent(4, 5),
        CoordinateComponent(5, 5),
    )

    private val slots = setOf(0, 1, 2, 3, 4, 5, 9, 10, 11, 12, 13, 14, 18, 19, 20, 21, 22, 23, 27, 28, 29, 30, 31, 32, 36, 37, 38, 39, 40, 41, 45, 46, 47, 48, 49, 50)

    /**
     * returns materials coordinate list
     *
     * @return[List] a list of [CoordinateComponent]
     */
    fun getAvailableCraftingSlotComponents(): List<CoordinateComponent> {
        return components
    }

    /**
     * returns materials index list
     *
     * @return[Set<Int>] a set of [Int]
     */
    fun getAvailableCraftingSlotIndices(): Set<Int> {
        return slots
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
            val item: ItemStack = inventory.getItem(index)?.takeIf { if (noAir) !it.isEmpty else true } ?: continue
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
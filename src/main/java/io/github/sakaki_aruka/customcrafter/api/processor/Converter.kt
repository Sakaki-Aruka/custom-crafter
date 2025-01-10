package io.github.sakaki_aruka.customcrafter.api.processor

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object Converter {

    /*
     * Big-Endian
     * [0] = 0-7 bit
     * [1] = 8-15 bit
     * [2] = 16-23 bit
     * [3] = 24-31 bit
     */
    internal fun Int.toByteArray(): ByteArray {
        val array = ByteArray(4)
        array[0] = (this shr 24 and 0xFF).toByte()
        array[1] = (this shr 16 and 0xFF).toByte()
        array[2] = (this shr 8 and 0xFF).toByte()
        array[3] = (this and 0xFF).toByte()

        return array
    }

    /*
     * Big-Endian
     */
    internal fun ByteArray.toInt(): Int {
        if (this.size != 4) throw IllegalStateException("ByteArray size must be 4, but this is ${this.size}.")
        return (this[0].toInt() and 0xFF shl 24) or
            (this[1].toInt() and 0xFF shl 16) or
            (this[2].toInt() and 0xFF shl 8) or
            (this[3].toInt() and 0xFF)
    }

    /**
     * @param[padding] an item what is used to 'null' items (empty slot item). default = ItemStack.empty()
     * @since 5.0.8
     */
    fun Inventory.toByteArray(padding: ItemStack = ItemStack.empty()): ByteArray {
        val items: MutableList<ItemStack> = mutableListOf()
        (0..<this.size).forEach { index ->
            items.add(this.getItem(index) ?: padding)
        }
        return ItemStack.serializeItemsAsBytes(items)
    }

    fun inventoryFromByteArray(
        array: ByteArray,
        customCrafterGUI: Boolean = true
    ): Inventory {
        val inventory: Inventory =
            if (customCrafterGUI) CustomCrafterAPI.getCraftingGUI()
            else Bukkit.createInventory(null, 54)

        for ((index: Int, item: ItemStack) in ItemStack.deserializeItemsFromBytes(array).withIndex()) {
            if (customCrafterGUI && index == CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT) continue
            inventory.setItem(index, item)
        }

        return inventory
    }

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
        if (inventory.isEmpty || !CustomCrafterAPI.isCustomCrafterGUI(inventory)) return null
        val result: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()

        for (coordinate in getAvailableCraftingSlotComponents()) {
            val index: Int = coordinate.x + coordinate.y * 9
            val item: ItemStack = inventory.getItem(index)?.takeIf { if (noAir) it.type != Material.AIR else true } ?: continue
            result[coordinate] = item
        }
        return result
    }
    
}
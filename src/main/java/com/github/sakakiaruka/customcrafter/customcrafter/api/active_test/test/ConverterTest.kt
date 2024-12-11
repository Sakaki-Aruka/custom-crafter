package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * @suppress
 */
internal object ConverterTest {
    fun run() {
        slotTest()
        inputMappingTest()
    }

    private fun slotTest() {
        val list: List<CoordinateComponent> = Converter.getAvailableCraftingSlotComponents()
        val components: List<CoordinateComponent> = listOf(
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
        CAssert.assertTrue(list == components)

        val intList: Set<Int> = (0..54).filter { it % 9 < 6 }.take(36).toSet()
        val slots: Set<Int> = Converter.getAvailableCraftingSlotIndices()
        CAssert.assertTrue(slots.size == intList.size)
        CAssert.assertTrue(slots.containsAll(intList))

        val componentList = Converter.getAvailableCraftingSlotComponents()
        val mappedIndices = componentList.map { it.x + it.y*9 }
        val indices = Converter.getAvailableCraftingSlotIndices()

        CAssert.assertTrue(mappedIndices.size == indices.size)
        CAssert.assertTrue(indices.containsAll(mappedIndices))

    }

    private fun inputMappingTest() {
        val empty = Bukkit.createInventory(null, 54)
        CAssert.assertTrue(Converter.standardInputMapping(empty) == null)
        val gui = CustomCrafterAPI.getCraftingGUI()
        CAssert.assertTrue(Converter.standardInputMapping(gui, noAir = false)?.values?.all { it.type == Material.AIR } ?: true)

        val stones: Inventory = CustomCrafterAPI.getCraftingGUI()
        Converter.getAvailableCraftingSlotIndices().forEach { index ->
            stones.setItem(index, ItemStack(Material.STONE))
        }
        val mapping: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(stones)!!
        val components: Set<CoordinateComponent> = setOf(
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
        CAssert.assertTrue(mapping.keys == components)
        CAssert.assertTrue(mapping.values.size == 36)
        CAssert.assertTrue(mapping.values.toSet().size == 1)
        CAssert.assertTrue(mapping.values.toSet().first().isSimilar(ItemStack(Material.STONE)))
    }
}
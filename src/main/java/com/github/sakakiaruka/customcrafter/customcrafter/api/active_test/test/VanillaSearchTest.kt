package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import com.github.sakakiaruka.customcrafter.customcrafter.api.search.VanillaSearch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * @suppress
 */
internal object VanillaSearchTest {
    fun run() {
        vanillaSearchTest()
    }

    private fun clear(gui: Inventory) {
        Converter.getAvailableCraftingSlotIndices().forEach { i ->
            gui.setItem(i, ItemStack.empty())
        }
    }

    private fun vanillaSearchTest() {
        val gui = CustomCrafterAPI.getCraftingGUI()

        // furnace recipe
        /*
         * sss
         * s_s
         * sss
         */
        val stone = ItemStack(Material.COBBLESTONE)
        setOf(0, 1, 2, 9, 11, 18, 19, 20).forEach { i ->
            gui.setItem(i, stone)
        }

        val world = Bukkit.getWorlds().first()
        val result = VanillaSearch.search(world, gui)
        CAssert.assertTrue(result?.result?.type == Material.FURNACE)

        clear(gui)
        // empty input
        val emptyResult = VanillaSearch.search(world, gui)
        CAssert.assertTrue(emptyResult == null)

        // over nine size input
        // 36 full-size stone input
        Converter.getAvailableCraftingSlotIndices().forEach { i ->
            gui.setItem(i, ItemStack(Material.STONE))
        }

        val fullResult = VanillaSearch.search(world, gui)
        CAssert.assertTrue(fullResult == null)

    }
}
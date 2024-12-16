package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @suppress
 * This is an active test object.
 */
internal object APITest {
    fun run() {
        baseBlockTest()
        craftingGUITest()
        tooOldTest()
        randomCoordinatesTest()
    }

    private fun randomCoordinatesTest() {
        try {
            CustomCrafterAPI.getRandomNCoordinates(0)
        } catch (illegalArgument: IllegalArgumentException) {
            illegalArgument.cause?.let { CAssert.assertThrow(it, IllegalArgumentException::class) }
        }

        val n = 100
        val result = CustomCrafterAPI.getRandomNCoordinates(n)
        CAssert.assertTrue(result.size == n)
    }

    private fun baseBlockTest() {
        val base: Int = CustomCrafterAPI.getBaseBlockSideSize()
        CAssert.assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(-1))
        CAssert.assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(0))
        CAssert.assertTrue(CustomCrafterAPI.setBaseBlockSideSize(5))
        CustomCrafterAPI.setBaseBlockSideSize(3)
        CAssert.assertTrue(CustomCrafterAPI.getBaseBlockSideSize() == 3)

        CustomCrafterAPI.BASE_BLOCK_SIDE = base
    }

    private fun craftingGUITest() {
        val gui = CustomCrafterAPI.getCraftingGUI()
        CAssert.assertTrue(CustomCrafterAPI.isCustomCrafterGUI(gui))
        CAssert.assertTrue(!CustomCrafterAPI.isGUITooOld(gui))
        val dummy = Bukkit.createInventory(null, 54)
        CAssert.assertTrue(!CustomCrafterAPI.isCustomCrafterGUI(dummy))
        try {
            CustomCrafterAPI.isGUITooOld(dummy)
        } catch (illegalArgument: IllegalArgumentException) {
            illegalArgument.cause?.let { CAssert.assertThrow(it, IllegalArgumentException::class) }
        }
    }

    private fun tooOldTest() {
        val gui = CustomCrafterAPI.getCraftingGUI()
        val (key, type, _) = CustomCrafterAPI.genCCKey()
        val item = ItemStack(Material.ANVIL).apply {
            itemMeta = itemMeta.apply {
                persistentDataContainer.set(key, type, 0L)
            }
        }
        gui.setItem(CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT, item)

        CAssert.assertTrue(CustomCrafterAPI.isGUITooOld(gui))
    }
}
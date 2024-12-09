package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import org.bukkit.Bukkit

/**
 * @suppress
 * This is an active test object.
 */
internal object APITest {
    fun run() {
        baseBlockTest()
        craftingGUI()
    }
    private fun baseBlockTest() {
        CAssert.assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(-1))
        CAssert.assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(0))
        CAssert.assertTrue(CustomCrafterAPI.setBaseBlockSideSize(5))
        CustomCrafterAPI.setBaseBlockSideSize(3)
        CAssert.assertTrue(CustomCrafterAPI.getBaseBlockSideSize() == 3)
    }

    private fun craftingGUI() {
        val gui = CustomCrafterAPI.getCraftingGUI()
        CAssert.assertTrue(CustomCrafterAPI.isCustomCrafterGUI(gui))
        CAssert.assertTrue(!CustomCrafterAPI.isGUITooOld(gui))
        val dummy = Bukkit.createInventory(null, 54)
        CAssert.assertTrue(!CustomCrafterAPI.isCustomCrafterGUI(dummy))
        CAssert.assertThrow(CustomCrafterAPI.isGUITooOld(dummy), IllegalArgumentException::class)
    }
}
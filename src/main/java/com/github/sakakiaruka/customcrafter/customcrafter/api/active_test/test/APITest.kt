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
        val base: Int = CustomCrafterAPI.getBaseBlockSideSize()
        CAssert.assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(-1))
        CAssert.assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(0))
        CAssert.assertTrue(CustomCrafterAPI.setBaseBlockSideSize(5))
        CustomCrafterAPI.setBaseBlockSideSize(3)
        CAssert.assertTrue(CustomCrafterAPI.getBaseBlockSideSize() == 3)

        CustomCrafterAPI.BASE_BLOCK_SIDE = base
    }

    private fun craftingGUI() {
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
}
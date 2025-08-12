package io.github.sakaki_aruka.customcrafter.impl.test

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.active_test.CAssert
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @suppress
 * This is an active test objects.
 */
internal object APITest {
    fun run() {
        baseBlockTest()
        randomCoordinatesTest()
        allCandidateTest()
    }

    private fun randomCoordinatesTest() {
        CAssert.assertThrow(IllegalArgumentException::class) {
            CustomCrafterAPI.getRandomNCoordinates(0)
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

        CAssert.assertThrow(IllegalArgumentException::class) {
            CustomCrafterAPI.setBaseBlock(Material.DIAMOND)
        }

        CustomCrafterAPI.setBaseBlockSideSize(base)
    }

    private fun allCandidateTest() {
        CAssert.assertThrow(IllegalArgumentException::class) {
            CustomCrafterAPI.setAllCandidateNotDisplayableItem(ItemStack(Material.WATER)) { _ -> null }
        }
    }
}
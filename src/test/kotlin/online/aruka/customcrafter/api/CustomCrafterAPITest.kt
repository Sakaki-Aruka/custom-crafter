package online.aruka.customcrafter.api

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.lang.IllegalArgumentException

internal object CustomCrafterAPITest {

    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun randomCoordinatesTest() {
        assertTrue(CustomCrafterAPI.getRandomNCoordinates(100).size == 100)

        assertThrows<IllegalArgumentException> {
            CustomCrafterAPI.getRandomNCoordinates(0)
        }
    }

    @Test
    fun baseBlockSizeTest() {
        assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(-1))
        assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(0))
        assertTrue(CustomCrafterAPI.setBaseBlockSideSize(5))

        CustomCrafterAPI.setBaseBlockSideSize(5)
        assertTrue(CustomCrafterAPI.getBaseBlockSideSize() == 5)

        CustomCrafterAPI.setBaseBlockSideSizeDefault()
        assertTrue(CustomCrafterAPI.getBaseBlockSideSize() == 3)
    }

    @Test
    fun baseBlockTypeTest() {
        assertThrows<IllegalArgumentException> {
            CustomCrafterAPI.setBaseBlock(Material.AIR)
        }

        assertThrows<IllegalArgumentException> {
            CustomCrafterAPI.setBaseBlock(Material.GOLD_INGOT)
        }

        CustomCrafterAPI.setBaseBlock(Material.DIAMOND_BLOCK)
        assertTrue(CustomCrafterAPI.getBaseBlock() == Material.DIAMOND_BLOCK)

        CustomCrafterAPI.setBaseBlockDefault()
        assertTrue(CustomCrafterAPI.getBaseBlock() == Material.GOLD_BLOCK)
    }

    @Test
    fun autoCraftBaseBlockTypeTest() {
        assertThrows<IllegalArgumentException> {
            CustomCrafterAPI.setAutoCraftingBaseBlock(Material.AIR)
        }

        assertThrows<kotlin.IllegalArgumentException> {
            CustomCrafterAPI.setAutoCraftingBaseBlock(Material.GOLD_INGOT)
        }

        CustomCrafterAPI.setAutoCraftingBaseBlock(Material.DIAMOND_BLOCK)
        assertTrue(CustomCrafterAPI.getAutoCraftingBaseBlock() == Material.DIAMOND_BLOCK)

        CustomCrafterAPI.setAutoCraftingBaseBlockDefault()
        assertTrue(CustomCrafterAPI.getAutoCraftingBaseBlock() == Material.GOLD_BLOCK)
    }

    // MockBukkit error
//    @Test
//    fun allCandidateTest() {
//        assertThrows<IllegalArgumentException> {
//            CustomCrafterAPI.setAllCandidateNotDisplayableItem(ItemStackMock(Material.WATER)) { _ -> null }
//        }
//    }
}
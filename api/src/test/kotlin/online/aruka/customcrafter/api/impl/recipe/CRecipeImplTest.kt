package online.aruka.customcrafter.api.impl.recipe

import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock

object CRecipeImplTest {

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
    fun detectEmptyItemsOnShapelessTest() {
        assertThrows<IllegalArgumentException> {
            CRecipeImpl.shapeless(
                name = "",
                items = emptyList()
            )
        }
    }

    @Test
    fun detectOverTheLimitItemsOnShapelessTest() {
        assertThrows<IllegalArgumentException> {
            CRecipeImpl.shapeless(
                name = "",
                items = List(37) { CMatterImpl.single(Material.STONE) }
            )
        }
    }

    @Test
    fun detectAirMatterOnShapelessTest() {
        val air = CMatterImpl(
            name = "",
            candidate = setOf(Material.AIR)
        )
        assertThrows<IllegalStateException> {
            CRecipeImpl.shapeless(
                name = "",
                items = listOf(air)
            )
        }
    }

    @Test
    fun detectNotItemMatterOnShapelessTest() {
        val notItem = CMatterImpl(
            name = "",
            candidate = setOf(Material.WATER)
        )
        assertThrows<IllegalStateException> {
            CRecipeImpl.shapeless(
                name = "",
                items = listOf(notItem)
            )
        }
    }

    @Test
    fun shapelessSuccessfulOnShapelessTest() {
        val matter = CMatterImpl.single(Material.STONE)
        assertDoesNotThrow {
            CRecipeImpl.shapeless(
                name = "",
                listOf(matter)
            )
        }
    }
}
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
    fun detectEmptyItemsOnAmorphousTest() {
        assertThrows<IllegalArgumentException> {
            CRecipeImpl.amorphous(
                name = "",
                items = emptyList()
            )
        }
    }

    @Test
    fun detectOverTheLimitItemsOnAmorphousTest() {
        assertThrows<IllegalArgumentException> {
            CRecipeImpl.amorphous(
                name = "",
                items = List(37) { CMatterImpl.single(Material.STONE) }
            )
        }
    }

    @Test
    fun detectAirMatterOnAmorphousTest() {
        val air = CMatterImpl(
            name = "",
            candidate = setOf(Material.AIR)
        )
        assertThrows<IllegalStateException> {
            CRecipeImpl.amorphous(
                name = "",
                items = listOf(air)
            )
        }
    }

    @Test
    fun detectNotItemMatterOnAmorphousTest() {
        val notItem = CMatterImpl(
            name = "",
            candidate = setOf(Material.WATER)
        )
        assertThrows<IllegalStateException> {
            CRecipeImpl.amorphous(
                name = "",
                items = listOf(notItem)
            )
        }
    }

    @Test
    fun amorphousSuccessfulOnAmorphousTest() {
        val matter = CMatterImpl.single(Material.STONE)
        assertDoesNotThrow {
            CRecipeImpl.amorphous(
                name = "",
                listOf(matter)
            )
        }
    }
}
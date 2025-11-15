package online.aruka.customcrafter.api.impl.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.GroupRecipe
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertTrue

object GroupMatterTest {
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
    fun matterOfSuccessTest() {
        assertDoesNotThrow {
            GroupRecipe.Matter.of(
                matter = CMatterImpl.of(Material.STONE)
            )
        }
    }

    @Test
    fun matterOfFailWithClassTest() {
        val matter: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE)
        )

        assertThrows<IllegalArgumentException> {
            GroupRecipe.Matter.of(matter = matter)
        }
    }

    @Test
    fun isValidMatterDetectCandidateEmpty() {
        val matter: CMatter = CMatterImpl(
            name = "",
            candidate = setOf()
        )

        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(matter = matter, includeAir = false)
        }
    }

    @Test
    fun isValidMatterDetectNotItemMaterialTest() {
        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(name = "", candidate = setOf(Material.WATER))
            )
        }
    }

    @Test
    fun isValidMatterDetectZeroOrNegativeAmountTest() {
        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(
                    name = "",
                    candidate = setOf(Material.STONE),
                    amount = 0
                )
            )
        }

        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(
                    name = "",
                    candidate = setOf(Material.STONE),
                    amount = -1
                )
            )
        }
    }

    @Test
    fun isValidMatterDetectOnlyAirCandidateTest() {
        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(
                    name = "",
                    candidate = emptySet()
                ),
                includeAir = true
            )
        }
    }
}
package online.aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertTrue

object CMatterTest {

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
    fun detectEmptyCandidateTest() {
        val matter = CMatterImpl(
            name = "",
            candidate = emptySet()
        )
        assertTrue(matter.isValidMatter().isFailure)
        assertTrue(matter.isValidMatter().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun detectAirCandidateTest() {
        val air = CMatterImpl(
            name = "",
            candidate = setOf(Material.AIR)
        )
        assertTrue(air.isValidMatter().isFailure)
        assertTrue(air.isValidMatter().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun detectNotItemCandidateTest() {
        val noItem = CMatterImpl(
            name = "",
            candidate = setOf(Material.WATER)
        )
        assertTrue(noItem.isValidMatter().isFailure)
        assertTrue(noItem.isValidMatter().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun detectNegativeAmountTest() {
        val minus = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            amount = -1
        )
        assertTrue(minus.isValidMatter().isFailure)
        assertTrue(minus.isValidMatter().exceptionOrNull() is IllegalStateException)
    }
}
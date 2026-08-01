package online.aruka.custom_crafter.api.`object`

import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock

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
        assertThrows<IllegalStateException> { matter.isValidMatter() }
    }

    @Test
    fun detectAirCandidateTest() {
        val air = CMatterImpl(
            name = "",
            candidate = setOf(Material.AIR)
        )
        assertThrows<IllegalStateException> { air.isValidMatter() }
    }

    @Test
    fun detectNotItemCandidateTest() {
        val noItem = CMatterImpl(
            name = "",
            candidate = setOf(Material.WATER)
        )
        assertThrows<IllegalStateException> { noItem.isValidMatter() }
    }

    @Test
    fun detectNegativeAmountTest() {
        val minus = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            amount = -1
        )
        assertThrows<IllegalStateException> { minus.isValidMatter() }
    }

    @Test
    fun validCMatterTest() {
        val valid = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            amount = 1
        )
        assertDoesNotThrow { valid.isValidMatter() }
    }
}
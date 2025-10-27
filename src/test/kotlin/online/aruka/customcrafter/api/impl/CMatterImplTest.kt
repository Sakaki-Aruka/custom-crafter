package online.aruka.customcrafter.api.impl

import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock

object CMatterImplTest {

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
    fun detectAirOnSingleTest() {
        assertThrows<IllegalArgumentException> { CMatterImpl.single(Material.AIR) }
    }

    @Test
    fun detectNotItemOnSingleTest() {
        assertThrows<IllegalArgumentException> { CMatterImpl.single(Material.WATER) }
    }

    @Test
    fun validSingleTest() {
        assertDoesNotThrow { CMatterImpl.single(Material.STONE) }
    }

    @Test
    fun detectAirOnMultiTest() {
        assertThrows<IllegalStateException> { CMatterImpl.multi(Material.AIR) }
    }

    @Test
    fun detectNotItemOnMultiTest() {
        assertThrows<IllegalStateException> { CMatterImpl.multi(Material.WATER) }
    }

    @Test
    fun validMultiTest() {
        assertDoesNotThrow { CMatterImpl.multi(Material.STONE) }
    }
}
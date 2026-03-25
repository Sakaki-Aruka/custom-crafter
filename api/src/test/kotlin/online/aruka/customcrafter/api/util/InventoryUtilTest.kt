package online.aruka.customcrafter.api.util

import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.giveItems
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object InventoryUtilTest {

    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
        server.addPlayer()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun giveItemsAddsItemToInventoryTest() {
        val player: PlayerMock = server.getPlayer(0)
        player.giveItems(saveLimit = true, ItemStack.of(Material.STONE, 32))
        assertTrue(player.inventory.contains(Material.STONE))
        assertEquals(32, player.inventory.all(Material.STONE).values.sumOf { it.amount })
    }

    @Test
    fun giveItemsExceedingMaxStackSizeSplitsIntoMultipleStacksTest() {
        val player: PlayerMock = server.getPlayer(0)
        player.giveItems(saveLimit = true, ItemStack.of(Material.STONE, 128))
        assertEquals(128, player.inventory.all(Material.STONE).values.sumOf { it.amount })
        assertTrue(player.inventory.all(Material.STONE).values.all { it.amount <= Material.STONE.maxStackSize })
    }

    @Test
    fun giveItemsFullInventoryDropsOverflowToWorldTest() {
        val player: PlayerMock = server.getPlayer(0)
        // Fill all inventory slots using contents array (covers main + armor + offhand)
        val fullContents = Array(player.inventory.size) { ItemStack.of(Material.DIRT, 64) }
        player.inventory.contents = fullContents

        player.giveItems(saveLimit = true, ItemStack.of(Material.STONE, 1))

        assertFalse(player.inventory.contains(Material.STONE))
        assertTrue(
            server.worlds.first().entities
                .filterIsInstance<Item>()
                .any { it.itemStack.type == Material.STONE }
        )
    }

    @Test
    fun giveItemsSaveLimitFalseAddsItemWithoutSplittingTest() {
        val player: PlayerMock = server.getPlayer(0)
        player.giveItems(saveLimit = false, ItemStack.of(Material.STONE, 16))
        assertTrue(player.inventory.contains(Material.STONE))
        assertEquals(16, player.inventory.all(Material.STONE).values.sumOf { it.amount })
    }
}
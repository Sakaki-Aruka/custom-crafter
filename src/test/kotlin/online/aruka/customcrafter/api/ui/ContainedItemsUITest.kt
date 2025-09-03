package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.AutoCraftRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.ContainedItemsUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object ContainedItemsUITest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
        server.addPlayer()

        CustomCrafterAPI.setAutoCraftingBaseBlock(Material.GOLD_BLOCK)

        server.worlds.first().let { world ->
            val base = world.getBlockAt(0, 64, 0)
            base.type = Material.CRAFTER

            for (dx in (-1..1)) {
                for (dz in (-1..1)) {
                    if (dx == 0 && dz == 0) {
                        continue
                    }
                    base.getRelative(dx, -1, dz).type = Material.GOLD_BLOCK
                }
            }
        }

        CBlockDB.initTables(useInMemoryDatabase = true)
        try {
            CBlockDB.unlink(server.worlds.first().getBlockAt(0, 64, 0))
        } catch (_: Exception) {
            //
        }
    }

    @AfterEach
    fun tearDown() {
        val recipes = CustomCrafterAPI.getRecipes()
        recipes.forEach { r -> CustomCrafterAPI.unregisterRecipe(r) }
        try {
            CBlockDB.unlink(server.worlds.first().getBlockAt(0, 64, 0))
        } catch (_: Exception) {
            //
        }

        MockBukkit.unmock()
    }

    @Test
    fun getSameViewTest() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )
        CustomCrafterAPI.registerRecipe(recipe)

        val ui1 = ContainedItemsUI.of(cBlock)
        val ui2 = ContainedItemsUI.of(cBlock)
        assertTrue(ui1 == ui2)
    }

    @Test
    fun itemMergeTest() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )
        CustomCrafterAPI.registerRecipe(recipe)

        val ui = ContainedItemsUI.of(cBlock)
        assertTrue(ui != null)
        val maxStackStone = ItemStack.of(Material.STONE, 64)
        // recipe shape
        // ###
        // # #
        // ### -> the limit is 8 slots
        assertTrue(ui.merge(maxStackStone).isEmpty)
        assertTrue(ui.merge(maxStackStone).isEmpty)
        assertTrue(ui.merge(maxStackStone).isEmpty)
        assertTrue(ui.merge(maxStackStone).isEmpty)
        assertTrue(ui.merge(maxStackStone).isEmpty)
        assertTrue(ui.merge(maxStackStone).isEmpty)
        assertTrue(ui.merge(maxStackStone).isEmpty)
        assertTrue(ui.merge(maxStackStone).isEmpty)

        // there is no 9th slot. So, 'merge' method returns an item what same with a given.
        assertTrue(ui.merge(maxStackStone).isSimilar(maxStackStone))

        assertTrue(ui.getNoBlankItems().size == 8)
        assertTrue(ui.getNoBlankItems().all { item -> item.isSimilar(maxStackStone) })
    }

    @Test
    fun itemMergeTest2() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )
        CustomCrafterAPI.registerRecipe(recipe)

        val ui = ContainedItemsUI.of(cBlock)
        assertTrue(ui != null)

        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)
        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)
        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)
        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)
        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)
        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)
        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)
        assertTrue(ui.merge(ItemStack.of(Material.STONE, 63)).isEmpty)

        // over 1 item
        val over = ui.merge(ItemStack.of(Material.STONE, 9))
        assertFalse(over.isEmpty)
        assertTrue(over.amount == 1)
    }

    @Test
    fun detectContainedItemTest() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )
        CustomCrafterAPI.registerRecipe(recipe)

        val ui = ContainedItemsUI.of(cBlock)
        assertTrue(ui != null)
        assertTrue(ContainedItemsUI.contains(block.location))
    }

    @Test
    fun deleteCacheOnPlayerCloseInventory() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )
        CustomCrafterAPI.registerRecipe(recipe)

        val ui = ContainedItemsUI.of(cBlock)
        assertTrue(ui != null)
        val player: Player = server.getPlayer(0)
        player.openInventory(ContainedItemsUI.of(cBlock)!!.inventory)
        val closeEvent = InventoryCloseEvent(player.openInventory)
        ui.onClose(closeEvent)
        assertFalse(ContainedItemsUI.contains(block.location))
    }
}
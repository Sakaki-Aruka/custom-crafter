package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.AutoCraftRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.AutoCraftUI
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal object AutoCraftUITest {
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

        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        repeat(50) {
            CustomCrafterAPI.registerRecipe(recipe)
        }
    }

    @AfterEach
    fun tearDown() {
        val recipes = CustomCrafterAPI.getRecipes()
        recipes.forEach { recipe ->
            CustomCrafterAPI.unregisterRecipe(recipe)
        }

        MockBukkit.unmock()
    }

    @Test
    fun triggerTest() {
        val player: Player = server.getPlayer(0)
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)

        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            block,
            BlockFace.UP
        )

        CBlockDB.initTables(useInMemoryDatabase = true)
        CustomCrafterAPI.setUseAutoCraftingFeature(true)

        assertTrue(AutoCraftUI.isTrigger(event))
    }

    @Test
    fun triggerFailWithAPIPropertyTest() {
        val player: Player = server.getPlayer(0)
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)

        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            block,
            BlockFace.UP
        )

        assertFalse(AutoCraftUI.isTrigger(event))
    }

    @Test
    fun triggerFailWithHopperInMainHandTest() {
        val player: Player = server.getPlayer(0)
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)

        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            ItemStack(Material.HOPPER),
            block,
            BlockFace.UP
        )

        assertFalse(AutoCraftUI.isTrigger(event))
    }

    @Test
    fun openTest() {
        val player: Player = server.getPlayer(0)
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)

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
            slots = recipe.items.keys.map { c -> c.toIndex() }.sorted(),
            block = block
        )
        val autoCraftUI = AutoCraftUI.of(cBlock = cBlock, player = player)

        player.openInventory(autoCraftUI.inventory)
        assertTrue(player.openInventory.topInventory.holder is AutoCraftUI)
    }

    @Test
    fun linkedSetSlotDefaultDisplayItemTest() {
        val player: Player = server.getPlayer(0)
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)

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
            slots = recipe.items.keys.map { c -> c.toIndex() }.sorted(),
            block = block
        )
        val autoCraftUI = AutoCraftUI.of(cBlock = cBlock, player = player)

        player.openInventory(autoCraftUI.inventory)
        assertTrue(player.openInventory.topInventory.holder is AutoCraftUI)

        val setSlotItem = autoCraftUI.inventory.getItem(AutoCraftUI.SET)
        assertTrue(setSlotItem != null)
        assertTrue(setSlotItem.isSimilar(AutoCraftRecipe.getDefaultDisplayItemProvider(recipe.name)(player, block)))
    }

    @Test
    fun linkedSetSlotNotFoundDisplayItemTest() {
        val player: Player = server.getPlayer(0)
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)

        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = "0.0.0", // <- Diff with the Default
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.sorted(),
            block = block
        )
        val autoCraftUI = AutoCraftUI.of(cBlock = cBlock, player = player)

        player.openInventory(autoCraftUI.inventory)
        assertTrue(player.openInventory.topInventory.holder is AutoCraftUI)

        val setSlotItem = autoCraftUI.inventory.getItem(AutoCraftUI.SET)
        assertTrue(setSlotItem != null)
        assertTrue(setSlotItem.isSimilar(AutoCraftUI.NOT_FOUND))
    }

    // MockBukkit error at 1.21.4
    // Future fix
//    @Test
//    fun unlinkedSetSlotTest() {
//        val player: Player = server.getPlayer(0)
//        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)
//        val ui = AutoCraftUI.of(block, player)
//
//        player.openInventory(ui.inventory)
//        assertTrue(player.openInventory.topInventory.holder is AutoCraftUI)
//
//        val setSlotItem = ui.inventory.getItem(AutoCraftUI.SET)
//        assertTrue(setSlotItem != null)
//        assertTrue(setSlotItem.isSimilar(AutoCraftUI.UNDEFINED))
//    }


}
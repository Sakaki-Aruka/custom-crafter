package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.AutoCraftRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.AutoCraftUI
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.RecipeSetUI
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object RecipeSetUITest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
        server.addPlayer()

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
    fun forceFlipTest() {
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)
        val player: Player = server.getPlayer(0)
        val ui = RecipeSetUI(block, player)

        assertTrue(ui.currentPage == 0)
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 45)
        assertTrue(ui.canFlipPage())

        ui.flipPage()
        assertTrue(ui.currentPage == 1)
        assertFalse(ui.canFlipPage())
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 5)
    }

    @Test
    fun playerFlipTest() {
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)
        val player: Player = server.getPlayer(0)
        val ui = RecipeSetUI(block, player)

        assertTrue(ui.currentPage == 0)
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 45)
        assertTrue(ui.canFlipPage())

        player.openInventory(ui.inventory)

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            RecipeSetUI.NEXT,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )
        ui.onClick(ui.inventory, clickEvent)

        assertTrue(ui.currentPage == 1)
        assertFalse(ui.canFlipPage())
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 5)
    }

    @Test
    fun forceFlipBackTest() {
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)
        val player: Player = server.getPlayer(0)
        val ui = RecipeSetUI(block, player)

        assertTrue(ui.canFlipPage())
        assertFalse(ui.canFlipBackPage())
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 45)

        ui.flipPage()
        assertTrue(ui.currentPage == 1)
        assertFalse(ui.canFlipPage())
        assertTrue(ui.canFlipBackPage())
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 5)

        ui.flipBackPage()
        assertTrue(ui.currentPage == 0)
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 45)
    }

    @Test
    fun playerFlipBackTest() {
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)
        val player: Player = server.getPlayer(0)
        val ui = RecipeSetUI(block, player)

        assertTrue(ui.canFlipPage())
        assertFalse(ui.canFlipBackPage())
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 45)

        player.openInventory(ui.inventory)

        ui.flipPage()
        assertTrue(ui.currentPage == 1)
        assertFalse(ui.canFlipPage())
        assertTrue(ui.canFlipBackPage())
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 5)

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            RecipeSetUI.PREVIOUS,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )
        ui.onClick(ui.inventory, clickEvent)

        assertTrue(ui.currentPage == 0)
        assertTrue(ui.canFlipPage())
        assertFalse(ui.canFlipBackPage())
        assertTrue(ui.inventory.contents.count { i -> i?.type == Material.COMMAND_BLOCK } == 45)
    }

    // Future test (MockBukkit implements Crafter (org.bukkit.block))
//    @Test
//    fun playerBackToAutoCraftTest() {
//        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)
//        val player: Player = server.getPlayer(0)
//        val ui = RecipeSetUI(block, player)
//        player.openInventory(ui.inventory)
//
//        val clickEvent = InventoryClickEvent(
//            player.openInventory,
//            InventoryType.SlotType.CONTAINER,
//            RecipeSetUI.BACK_TO_AUTO_CRAFT,
//            ClickType.SHIFT_RIGHT,
//            InventoryAction.NOTHING
//        )
//        ui.onClick(ui.inventory, clickEvent)
//
//        assertTrue(player.openInventory.topInventory.holder is AutoCraftUI)
//    }
}
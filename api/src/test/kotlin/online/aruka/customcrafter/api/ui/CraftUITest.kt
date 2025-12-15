package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals

internal object CraftUITest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
        server.addPlayer()

        server.worlds.first().let { world ->
            val base = world.getBlockAt(0, 64, 0)
            base.type = Material.CRAFTING_TABLE

            for (dx in (-1..1)) {
                for (dz in (-1..1)) {
                    base.getRelative(dx, -1, dz).type = Material.GOLD_BLOCK
                }
            }
        }

        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { _ -> matter }
        val recipe = CRecipeImpl(
            name = "",
            items = items,
            type = CRecipe.Type.SHAPED
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
        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            server.worlds.first().getBlockAt(0, 64, 0),
            BlockFace.UP
        )

        CraftUI.open(event)
        assertTrue(player.openInventory.topInventory.holder != null)
        assertTrue(player.openInventory.topInventory.holder is CraftUI)

        player.openInventory.close()
    }

    @Test
    fun onClickCraftCustomItemTest() {
        val player: Player = server.getPlayer(0)
        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            server.worlds.first().getBlockAt(0, 64, 0),
            BlockFace.UP
        )

        CraftUI.open(event)

        (player.openInventory.topInventory.holder as CraftUI).inventory.let { inv ->
            CoordinateComponent.square(3, dx = 1).forEach { c ->
                inv.setItem(c.toIndex(), ItemStack(Material.STONE))
            }
        }

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            CraftUI.MAKE_BUTTON, //CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )

        (player.openInventory.topInventory.holder as CraftUI).onClick(
            clicked = player.openInventory.topInventory,
            event = clickEvent
        )

        val placedItems = CoordinateComponent.square(3, dx = 1).map { c ->
            (player.openInventory.topInventory.holder as CraftUI).inventory.getItem(c.toIndex())
        }

        assertTrue(placedItems.all { item -> item == null || item.isEmpty })

        player.openInventory.close()
    }

    @Test
    fun onClickFailedCraftCustomItemTest() {
        val player: Player = server.getPlayer(0)
        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            server.worlds.first().getBlockAt(0, 64, 0),
            BlockFace.UP
        )

        CraftUI.open(event)

        (player.openInventory.topInventory.holder as CraftUI).inventory.let { inv ->
            CoordinateComponent.square(3).forEach { c ->
                inv.setItem(c.toIndex(), ItemStack(Material.OBSIDIAN))
            }
        }

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            CraftUI.MAKE_BUTTON ,//CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )

        (player.openInventory.topInventory.holder as CraftUI).onClick(
            clicked = player.openInventory.topInventory,
            event = clickEvent
        )

        val placedItems = CoordinateComponent.square(3).map { c ->
            (player.openInventory.topInventory.holder as CraftUI).inventory.getItem(c.toIndex())
        }

        assertTrue(placedItems.all { item -> item != null && item.isSimilar(ItemStack(Material.OBSIDIAN)) })

        player.openInventory.close()
    }

    @Test
    fun defaultDesignerHasValidBlankSlotsTest() {
        val baked = CraftUI().bakedDesigner
        assertEquals(16, baked.blankSlots.size)
        assertEquals(36, baked.craftSlots().size)
        assertTrue((0..<54).filter { it % 9 < 6 }.map { CoordinateComponent.fromIndex(it) }.containsAll(CraftUI().bakedDesigner.craftSlots()))
    }

    @Test
    fun designerIsValidTest() {
        assertTrue(CraftUI().bakedDesigner.isValid().isSuccess)
    }
}
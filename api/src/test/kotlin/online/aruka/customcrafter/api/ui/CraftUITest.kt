package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate.AllCandidateUI
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
        CustomCrafterAPI.unregisterAllRecipes()
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
    fun decrementOneItemFromCraftSlotsTest() {
        val player = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)

        CoordinateComponent.square(3).forEach {
            ui.inventory.setItem(it.toIndex(), ItemStack.of(Material.STONE, 64))
        }

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            CraftUI.MAKE_BUTTON,
            ClickType.RIGHT,
            InventoryAction.NOTHING
        )
        ui.onClick(clicked = ui.inventory, event = clickEvent)

        assertTrue(
            CoordinateComponent.square(3).all {
                ui.inventory.getItem(it.toIndex())?.isSimilar(ItemStack.of(Material.STONE, 63)) ?: false
            }
        )
    }

    @Test
    fun decrementOneItemFromCraftSlotsWithMassItemTest() {
        val noMass = CMatterImpl.of(Material.STONE)
        val mass = CMatterImpl("", setOf(Material.WATER_BUCKET), mass = true)
        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        CoordinateComponent.square(3).forEach { items[it] = noMass }
        items[CoordinateComponent(1, 1)] = mass
        // #: Stone, =: WaterBucket (Mass)
        // ###
        // #=#
        // ###
        val recipe = CRecipeImpl("", items, CRecipe.Type.SHAPED)
        CustomCrafterAPI.registerRecipe(recipe)

        val player = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)

        CoordinateComponent.square(3).forEach { ui.inventory.setItem(it.toIndex(), ItemStack.of(Material.STONE, 64)) }
        ui.inventory.setItem(CoordinateComponent(1, 1).toIndex(), ItemStack.of(Material.WATER_BUCKET))

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            CraftUI.MAKE_BUTTON,
            ClickType.RIGHT,
            InventoryAction.NOTHING
        )
        ui.onClick(clicked = ui.inventory, event = clickEvent)

        assertTrue(
            CoordinateComponent.square(3).all {
                ui.inventory.getItem(it.toIndex())?.let { item -> item.type == Material.STONE && item.amount == 63 } ?: false
            }
        )
        assertTrue(ui.inventory.getItem(CoordinateComponent(1, 1).toIndex())?.type?.isAir ?: true)

        CustomCrafterAPI.unregisterAllRecipes()
    }

    @Test
    fun decrementItemsFromCraftSlotsWithShiftTest() {
        val player = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)

        CoordinateComponent.square(3).forEach {
            ui.inventory.setItem(it.toIndex(), ItemStack.of(Material.STONE, 64))
        }

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            CraftUI.MAKE_BUTTON ,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )
        ui.onClick(clicked = ui.inventory, event = clickEvent)

        assertTrue(
            CoordinateComponent.square(3).all {
                ui.inventory.getItem(it.toIndex())?.type?.isAir ?: true
            }
        )
    }

    @Test
    fun decrementItemsFromCraftSlotsWithShiftAndMassItemTest() {
        val noMass = CMatterImpl.of(Material.STONE)
        val mass = CMatterImpl("", setOf(Material.WATER_BUCKET), mass = true)
        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        CoordinateComponent.square(3).forEach { items[it] = noMass }
        items[CoordinateComponent(1, 1)] = mass
        // #: Stone, =: WaterBucket (Mass)
        // ###
        // #=#
        // ###
        val recipe = CRecipeImpl("", items, CRecipe.Type.SHAPED)
        CustomCrafterAPI.registerRecipe(recipe)

        val player = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)

        CoordinateComponent.square(3).forEach { ui.inventory.setItem(it.toIndex(), ItemStack.of(Material.STONE, 64)) }
        ui.inventory.setItem(CoordinateComponent(1, 1).toIndex(), ItemStack.of(Material.WATER_BUCKET))

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            CraftUI.MAKE_BUTTON,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )
        ui.onClick(clicked = ui.inventory, event = clickEvent)

        assertTrue(CoordinateComponent.square(3).all { ui.inventory.getItem(it.toIndex())?.type?.isAir ?: true })
        assertTrue(ui.inventory.getItem(CoordinateComponent(1, 1).toIndex())?.type?.isAir ?: true)

        CustomCrafterAPI.unregisterAllRecipes()
    }

    @Test
    fun grabBlankSlotsIgnoreTest() {
        val player = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)

        assertTrue(
            ui.bakedDesigner.blankSlots.all { c ->
                val event = InventoryClickEvent(
                    player.openInventory,
                    InventoryType.SlotType.CONTAINER,
                    c.key.toIndex(),
                    ClickType.SHIFT_RIGHT,
                    InventoryAction.NOTHING
                )
                ui.onClick(clicked = ui.inventory, event = event)
                event.isCancelled
            }
        )
    }

    @Test
    fun avoidToPlaceItemsOnTheResultSlotTest() {
        val player = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)

        player.setItemOnCursor(ItemStack.of(Material.STONE))

        ui.onClick(
            clicked = ui.inventory,
            event = InventoryClickEvent(
                player.openInventory,
                InventoryType.SlotType.CONTAINER,
                ui.bakedDesigner.resultInt(),
                ClickType.LEFT,
                InventoryAction.PLACE_ONE
            )
        )

        assertTrue(ui.inventory.getItem(ui.bakedDesigner.resultInt())?.type?.isAir ?: true)
    }

    @Test
    fun openAllCandidateUITest() {
        val player = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)
        ui.inventory.setItem(ui.bakedDesigner.craftSlots().first().toIndex(), ItemStack.of(Material.DIRT))

        val dirt = CMatterImpl.of(Material.DIRT)
        repeat(5) { CustomCrafterAPI.registerRecipe(CRecipeImpl.shapeless("", listOf(dirt))) }

        CustomCrafterAPI.setUseMultipleResultCandidateFeature(true)

        ui.onClick(
            clicked = ui.inventory,
            event = InventoryClickEvent(
                player.openInventory,
                InventoryType.SlotType.CONTAINER,
                ui.bakedDesigner.makeButton.first.toIndex(),
                ClickType.SHIFT_RIGHT,
                InventoryAction.NOTHING
            )
        )

        assertTrue(player.openInventory.topInventory.holder is AllCandidateUI)

        CustomCrafterAPI.setUseMultipleResultCandidateFeatureDefault()
        CustomCrafterAPI.unregisterAllRecipes()
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

    @Test
    fun toViewTest() {
        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.STONE))

        assertEquals(1, ui.toView(noAir = true).materials.size)
        assertTrue(ui.toView(noAir = true).result.type.isAir)
    }

    @Test
    fun uiOpenIgnoreByPermissionNotEnoughTest() {
        val player: Player = server.getPlayer(0)
        val plugin = MockBukkit.createMockPlugin()

        player.addAttachment(plugin, "cc.craftui.click.open", false)
        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            server.worlds.first().getBlockAt(0, 64, 0),
            BlockFace.UP
        )
        assertFalse(CraftUI.isTrigger(event))
    }

    @Test
    fun uiOpenSuccessWithValidPermissionTest() {
        val player: Player = server.getPlayer(0)
        val plugin = MockBukkit.createMockPlugin()

        player.addAttachment(plugin, "cc.craftui.click.open", true)
        val event = PlayerInteractEvent(
            player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            server.worlds.first().getBlockAt(0, 64, 0),
            BlockFace.UP
        )
        assertTrue(CraftUI.isTrigger(event))
    }

    @Test
    fun uiCloseItemDropTest() {
        val player: PlayerMock = server.getPlayer(0)
        val ui = CraftUI(caller = player)
        player.openInventory(ui.inventory)
        ui.inventory.setItem(ui.bakedDesigner.craftSlots().first().toIndex(), ItemStack.of(Material.STONE))
        ui.inventory.setItem(ui.bakedDesigner.resultInt(), ItemStack.of(Material.DIRT))
        ui.onClose(InventoryCloseEvent(player.openInventory))

        assertTrue(player.inventory.contains(Material.STONE))
        assertTrue(player.inventory.contains(Material.DIRT))
    }
}
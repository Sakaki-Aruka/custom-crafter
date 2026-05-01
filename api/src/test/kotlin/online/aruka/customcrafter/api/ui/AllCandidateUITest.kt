package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.search.Search
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate.AllCandidateUI
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object AllCandidateUITest {

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
    fun forceFlipTest() {
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }

        val view = craftUI.toView()
        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view
        )

        assertEquals(50, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        assertTrue(allCandidateUI.canFlipPage())
        assertEquals(0, allCandidateUI.currentPage.get())

        val firstPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(45, firstPageDisplayedItems.size)

        // 50 recipes -> 2 pages (index=[0, 1])
        // set last page
        allCandidateUI.flipPage()
        assertEquals(1, allCandidateUI.currentPage.get())
        assertFalse(allCandidateUI.canFlipPage())

        val secondPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(5, secondPageDisplayedItems.size)
    }

    @Test
    fun playerFlipTest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }

        val view = craftUI.toView()
        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view
        )

        assertEquals(50, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        assertTrue(allCandidateUI.canFlipPage())
        assertEquals(0, allCandidateUI.currentPage.get())

        val firstPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(45, firstPageDisplayedItems.size)

        player.openInventory(allCandidateUI.inventory)

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            AllCandidateUI.NEXT,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )

        allCandidateUI.onClick(allCandidateUI.inventory, clickEvent)

        val secondPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(1, allCandidateUI.currentPage.get())
        assertEquals(5, secondPageDisplayedItems.size)
    }

    @Test
    fun forceFlipBackTest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }

        val view = craftUI.toView()
        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view
        )

        assertEquals(50, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        assertEquals(0, allCandidateUI.currentPage.get())
        assertFalse(allCandidateUI.canFlipBackPage())

        allCandidateUI.flipPage()

        val secondPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(5, secondPageDisplayedItems.size)

        assertTrue(allCandidateUI.canFlipBackPage())

        allCandidateUI.flipBackPage()
        assertEquals(0, allCandidateUI.currentPage.get())

        val firstPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(45, firstPageDisplayedItems.size)
    }

    @Test
    fun playerFlipBackTest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }

        val view = craftUI.toView()
        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view
        )

        assertEquals(50, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        player.openInventory(allCandidateUI.inventory)
        assertEquals(0, allCandidateUI.currentPage.get())

        allCandidateUI.flipPage()

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            AllCandidateUI.PREVIOUS,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )

        allCandidateUI.onClick(allCandidateUI.inventory, clickEvent)

        val firstPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(0, allCandidateUI.currentPage.get())
        assertEquals(45, firstPageDisplayedItems.size)
    }

    @Test
    fun backToCraftUITest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view
        )

        assertEquals(50, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        player.openInventory(allCandidateUI.inventory)
        assertEquals(0, allCandidateUI.currentPage.get())
        assertTrue(allCandidateUI.inventory.getItem(AllCandidateUI.BACK_TO_CRAFT) != null)
        assertTrue(allCandidateUI.inventory.getItem(AllCandidateUI.BACK_TO_CRAFT)!!.isSimilar(AllCandidateUI.BACK_TO_CRAFT_BUTTON))

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            AllCandidateUI.BACK_TO_CRAFT,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )
        allCandidateUI.onClick(allCandidateUI.inventory, clickEvent)

        assertTrue(player.openInventory.topInventory.holder is CraftUI)

        val backedCraftUI = player.openInventory.topInventory.holder as CraftUI
        assertTrue(CoordinateComponent.square(3).all { c ->
            backedCraftUI.inventory.getItem(c.toIndex()) != null
                    && backedCraftUI.inventory.getItem(c.toIndex())!!.isSimilar(ItemStack(Material.STONE))
        })
    }

    @Test
    fun backToCraftUIResultSlotTakeOverTest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view
        )

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        player.openInventory(allCandidateUI.inventory)

        val clickEvent = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            AllCandidateUI.BACK_TO_CRAFT,
            ClickType.SHIFT_RIGHT,
            InventoryAction.NOTHING
        )
        allCandidateUI.onClick(allCandidateUI.inventory, clickEvent)


        val backedCraftUI = player.openInventory.topInventory.holder as CraftUI
        assertTrue(CoordinateComponent.square(3).all { c ->
            backedCraftUI.inventory.getItem(c.toIndex()) != null
                    && backedCraftUI.inventory.getItem(c.toIndex())!!.isSimilar(ItemStack(Material.STONE))
        })
    }

    @Test
    fun pageContentsAtFirstPageHasNoPreviousButtonTest() {
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        val page0 = allCandidateUI.pageContentsAt(0)
        assertFalse(page0.containsKey(AllCandidateUI.PREVIOUS))
        assertTrue(page0.containsKey(AllCandidateUI.NEXT))
        assertTrue(page0.containsKey(AllCandidateUI.BACK_TO_CRAFT))
    }

    @Test
    fun pageContentsAtLastPageHasNoNextButtonTest() {
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        val page1 = allCandidateUI.pageContentsAt(1)
        assertTrue(page1.containsKey(AllCandidateUI.PREVIOUS))
        assertFalse(page1.containsKey(AllCandidateUI.NEXT))
        assertTrue(page1.containsKey(AllCandidateUI.BACK_TO_CRAFT))
    }

    @Test
    fun pageContentsAtMiddlePageHasBothButtonsTest() {
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { _ -> matter }
        val extraRecipe = CRecipeImpl("", items, CRecipe.Type.SHAPED)
        repeat(41) { CustomCrafterAPI.registerRecipe(extraRecipe) }

        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)
        assertEquals(91, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        val page1 = allCandidateUI.pageContentsAt(1)
        assertTrue(page1.containsKey(AllCandidateUI.PREVIOUS))
        assertTrue(page1.containsKey(AllCandidateUI.NEXT))
    }

    @Test
    fun pageContentsAtOutOfRangeThrowsTest() {
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        assertFailsWith<IllegalArgumentException> {
            allCandidateUI.pageContentsAt(2)
        }
        assertFailsWith<IllegalArgumentException> {
            allCandidateUI.pageContentsAt(-1)
        }
    }

    @Test
    fun nextClickAtLastPageDoesNotFlipTest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        allCandidateUI.flipPage()
        assertEquals(1, allCandidateUI.currentPage.get())
        player.openInventory(allCandidateUI.inventory)

        val event = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            AllCandidateUI.NEXT,
            ClickType.LEFT,
            InventoryAction.NOTHING
        )
        allCandidateUI.onClick(allCandidateUI.inventory, event)

        assertEquals(1, allCandidateUI.currentPage.get())
    }

    @Test
    fun previousClickAtFirstPageDoesNotFlipTest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        assertEquals(0, allCandidateUI.currentPage.get())
        player.openInventory(allCandidateUI.inventory)

        val event = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            AllCandidateUI.PREVIOUS,
            ClickType.LEFT,
            InventoryAction.NOTHING
        )
        allCandidateUI.onClick(allCandidateUI.inventory, event)

        assertEquals(0, allCandidateUI.currentPage.get())
    }

    @Test
    fun emptyRangeSlotClickDoesNothingTest() {
        val player: Player = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        player.openInventory(allCandidateUI.inventory)

        // Slot 46 is in the empty range (PREVIOUS+1 .. NEXT-1 = 46..52)
        val event = InventoryClickEvent(
            player.openInventory,
            InventoryType.SlotType.CONTAINER,
            46,
            ClickType.LEFT,
            InventoryAction.NOTHING
        )
        allCandidateUI.onClick(allCandidateUI.inventory, event)

        assertEquals(0, allCandidateUI.currentPage.get())
        assertTrue(event.isCancelled)
    }

    @Test
    fun allCandidateUIOnCloseDropsItemsTest() {
        val player: PlayerMock = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        player.openInventory(allCandidateUI.inventory)
        allCandidateUI.onClose(InventoryCloseEvent(player.openInventory))

        assertTrue(player.inventory.contains(Material.STONE))
    }

    @Test
    fun allCandidateUIOnCloseDoesNotDropItemsWhenDropOnCloseIsFalseTest() {
        val player: PlayerMock = server.getPlayer(0)
        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner,
            dropOnClose = AtomicBoolean(false)
        )

        player.openInventory(allCandidateUI.inventory)
        allCandidateUI.onClose(InventoryCloseEvent(player.openInventory))

        assertFalse(player.inventory.contains(Material.STONE))
    }

    @Test
    fun exactlyOnePageAllCandidateUITest() {
        CustomCrafterAPI.unregisterAllRecipes()
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { _ -> matter }
        val recipe = CRecipeImpl("", items, CRecipe.Type.SHAPED)
        repeat(45) { CustomCrafterAPI.registerRecipe(recipe) }

        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)
        assertEquals(45, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        assertFalse(allCandidateUI.canFlipPage())
        assertFalse(allCandidateUI.canFlipBackPage())

        val page0 = allCandidateUI.pageContentsAt(0)
        assertFalse(page0.containsKey(AllCandidateUI.NEXT))
        assertFalse(page0.containsKey(AllCandidateUI.PREVIOUS))
        assertTrue(page0.containsKey(AllCandidateUI.BACK_TO_CRAFT))
    }

    @Test
    fun singleRecipeAllCandidateUITest() {
        CustomCrafterAPI.unregisterAllRecipes()
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { _ -> matter }
        val recipe = CRecipeImpl("", items, CRecipe.Type.SHAPED)
        CustomCrafterAPI.registerRecipe(recipe)

        val craftUI = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            craftUI.inventory.setItem(c.toIndex(), ItemStack(Material.STONE))
        }
        val view = craftUI.toView()
        val result = Search.search(crafterID = UUID.randomUUID(), view = view)
        assertEquals(1, result.size())

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false,
            bakedCraftUIDesigner = craftUI.bakedDesigner
        )

        assertFalse(allCandidateUI.canFlipPage())
        assertFalse(allCandidateUI.canFlipBackPage())

        val page0 = allCandidateUI.pageContentsAt(0)
        assertFalse(page0.containsKey(AllCandidateUI.NEXT))
        assertFalse(page0.containsKey(AllCandidateUI.PREVIOUS))
        assertEquals(1, (0..<45).count { page0.containsKey(it) })
    }
}
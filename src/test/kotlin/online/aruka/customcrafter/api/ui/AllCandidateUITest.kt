package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate.AllCandidateUI
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID
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
            type = CRecipeType.NORMAL
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

        assertEquals(result.size(), 50)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = server.getPlayer(0),
            result = result,
            useShift = false
        )

        assertTrue(allCandidateUI.canFlipPage())
        assertEquals(allCandidateUI.currentPage, 0)

        val firstPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(firstPageDisplayedItems.size, 45)

        // 50 recipes -> 2 pages (index=[0, 1])
        // set last page
        allCandidateUI.flipPage()
        assertEquals(allCandidateUI.currentPage, 1)
        assertFalse(allCandidateUI.canFlipPage())

        val secondPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(secondPageDisplayedItems.size, 5)
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

        assertEquals(result.size(), 50)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false
        )

        assertTrue(allCandidateUI.canFlipPage())
        assertEquals(allCandidateUI.currentPage, 0)

        val firstPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(firstPageDisplayedItems.size, 45)

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
        assertEquals(allCandidateUI.currentPage, 1)
        assertEquals(secondPageDisplayedItems.size, 5)
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

        assertEquals(result.size(), 50)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false
        )

        assertEquals(allCandidateUI.currentPage, 0)
        assertFalse(allCandidateUI.canFlipBackPage())

        allCandidateUI.flipPage()

        val secondPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(secondPageDisplayedItems.size, 5)

        assertTrue(allCandidateUI.canFlipBackPage())

        allCandidateUI.flipBackPage()
        assertEquals(allCandidateUI.currentPage, 0)

        val firstPageDisplayedItems = (0..<45).mapNotNull { i ->
            allCandidateUI.inventory.getItem(i)
        }.filter { item -> !item.isEmpty }
        assertEquals(firstPageDisplayedItems.size, 45)
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

        assertEquals(result.size(), 50)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false
        )

        player.openInventory(allCandidateUI.inventory)
        assertEquals(allCandidateUI.currentPage, 0)

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
        assertEquals(allCandidateUI.currentPage, 0)
        assertEquals(firstPageDisplayedItems.size, 45)
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

        assertEquals(result.size(), 50)

        val allCandidateUI = AllCandidateUI(
            view = view,
            player = player,
            result = result,
            useShift = false
        )

        player.openInventory(allCandidateUI.inventory)
        assertEquals(allCandidateUI.currentPage, 0)
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
}
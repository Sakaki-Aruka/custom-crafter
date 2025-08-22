package online.aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.inventory.ItemStackMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID

internal object SearchTest {
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
    fun shapedTest1() {
        /*
         * xxx
         * x_x
         * xxx
         *
         * x = stone
         * _ = air
         */

        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = CRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL
        )

        val ui = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            ui.inventory.setItem(c.toIndex(), ItemStackMock(Material.STONE))
        }

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = CraftView.fromInventory(ui.inventory)!!,
            sourceRecipes = listOf(recipe)
        )

        assertTrue(result != null)
        assertTrue(result!!.size() == 1)
        assertTrue(result.customs().size == 1)
        assertTrue(result.vanilla() == null)

        ui.inventory.clear()
        CoordinateComponent.square(3).forEach { c ->
            ui.inventory.setItem(c.toIndex(), ItemStackMock(Material.DIRT))
        }

        val result2 = Search.search(
            crafterID = UUID.randomUUID(),
            view = CraftView.fromInventory(ui.inventory)!!,
            sourceRecipes = listOf(recipe)
        )

        assertTrue(result2 != null)
        assertTrue(result2!!.size() == 0)
    }

    @Test
    fun vanillaTest1() {
        val ui = CraftUI().inventory
        ui.setItem(0, ItemStack(Material.STONE))

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = CraftView.fromInventory(ui)!!
        )

        assertTrue(result != null)
        assertTrue(result!!.size() == 1)
        assertTrue(result.customs().isEmpty())
        assertTrue(result.vanilla() != null)
        assertTrue(result.vanilla()!!.result.type == Material.STONE_BUTTON)
    }
}
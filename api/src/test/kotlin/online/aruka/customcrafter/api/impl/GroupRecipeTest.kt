package online.aruka.customcrafter.api.impl

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.GroupRecipe
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.EnchantFilter
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object GroupRecipeTest {

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
    fun createCorrectFilter() {
        val filters = GroupRecipe.createFilters(
            setOf(EnchantFilter)
        )

        assertEquals(1, filters.size)
        assertTrue(filters.first() is GroupRecipe.Filter)
        val groupFilter = filters.first() as GroupRecipe.Filter
        assertEquals(1, groupFilter.filterMapping.size)
        assertEquals(EnchantFilter::class, groupFilter.filterMapping.keys.first())
    }

    @Test
    fun createCorrectGroups() {
        val stone: CMatter = CMatterImpl.of(Material.STONE)
        val context = GroupRecipe.Context.of(
            members = setOf(CoordinateComponent(0, 0)),
            min = 1
        )
        val set: Set<GroupRecipe.Context> = GroupRecipe.createGroups(
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to stone
            ),
            missingGroups = setOf(context)
        )

        assertEquals(2, set.size)

        assertTrue(set.any { ctx ->
            ctx.members.size == 1
                    && ctx.members.containsAll(setOf(CoordinateComponent(0, 0)))
                    && ctx.min == 1
        })

        assertTrue(set.any { ctx ->
            ctx.members.size == 1
                    && ctx.members.containsAll(setOf(CoordinateComponent(1, 0)))
                    && ctx.min == 1
        })
    }

    @Test
    fun fullSizeSearchSuccessTest() {
        /*
         * # -> stone
         * + -> calcite
         *
         * #++++#
         * ++++++
         * ++++++
         * ++++++
         * ++++++
         * #++++#
         *
         * calcite -> min = 1
         */

        val stone: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE),
            includeAir = false
        )

        val calcite: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.CALCITE),
            includeAir = true
        )

        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        for (c in Converter.getAvailableCraftingSlotComponents()) {
            items[c] = calcite
        }
        items[CoordinateComponent(0, 0)] = stone
        items[CoordinateComponent(5, 0)] = stone
        items[CoordinateComponent(0, 5)] = stone
        items[CoordinateComponent(5, 5)] = stone

        val calciteGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.CALCITE in m.candidate }.keys,
            min = 4
        )
        val stoneGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.STONE in m.candidate }.keys,
            min = 4
        )

        val recipe: CRecipe = GroupRecipe(
            name = "Marble",
            items = items,
            filters = GroupRecipe.createFilters(CRecipeImpl.getDefaultFilters()),
            groups = setOf(calciteGroup, stoneGroup)
        )

        val view = CraftView(
            materials = items.entries.associate { (c, matter) ->
                c to ItemStack.of(matter.candidate.first())
            },
            result = ItemStack.empty()
        )

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view,
            sourceRecipes = listOf(recipe)
        )

        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
        assertEquals(null, result.vanilla())
    }

    @Test
    fun size35SearchSuccessTest() {
        val stone: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE),
            includeAir = false
        )

        val calcite: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.CALCITE),
            includeAir = true
        )

        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        for (c in Converter.getAvailableCraftingSlotComponents()) {
            items[c] = calcite
        }
        items[CoordinateComponent(0, 0)] = stone
        items[CoordinateComponent(5, 0)] = stone
        items[CoordinateComponent(0, 5)] = stone
        items[CoordinateComponent(5, 5)] = stone

        val calciteGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.CALCITE in m.candidate }.keys,
            min = 4
        )
        val stoneGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.STONE in m.candidate }.keys,
            min = 4
        )

        val recipe: CRecipe = GroupRecipe(
            name = "Marble",
            items = items,
            filters = GroupRecipe.createFilters(CRecipeImpl.getDefaultFilters()),
            groups = setOf(calciteGroup, stoneGroup)
        )

        val view = CraftView(
            materials = items.entries.associate { (c, matter) ->
                if (c.x != 1 || c.y != 0) {
                    c to ItemStack.of(matter.candidate.first())
                } else c to ItemStack.empty()
            },
            result = ItemStack.empty()
        )

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view,
            sourceRecipes = listOf(recipe)
        )

        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
        assertEquals(null, result.vanilla())
    }

    @Test
    fun size4SearchSuccessTest() {
        val stone: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE),
            includeAir = false
        )

        val calcite: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.CALCITE),
            includeAir = true
        )

        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        for (c in Converter.getAvailableCraftingSlotComponents()) {
            items[c] = calcite
        }
        items[CoordinateComponent(0, 0)] = stone
        items[CoordinateComponent(5, 0)] = stone
        items[CoordinateComponent(0, 5)] = stone
        items[CoordinateComponent(5, 5)] = stone

        val calciteGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.CALCITE in m.candidate }.keys,
            min = 4
        )
        val stoneGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.STONE in m.candidate }.keys,
            min = 4
        )

        val recipe: CRecipe = GroupRecipe(
            name = "Marble",
            items = items,
            filters = GroupRecipe.createFilters(CRecipeImpl.getDefaultFilters()),
            groups = setOf(calciteGroup, stoneGroup)
        )

        val calciteCoordinates: Set<CoordinateComponent> = items.entries.filter { (_, matter) ->
            matter.candidate.first() == Material.CALCITE
        }.map { (c, _) -> c }.take(4).toSet()

        val view = CraftView(
            materials = items.entries.associate { (c, matter) ->
                if (matter.candidate.first() == Material.STONE) {
                    c to ItemStack.of(Material.STONE)
                } else if (c !in calciteCoordinates) {
                    c to ItemStack.of(Material.CALCITE)
                } else c to ItemStack.empty()
            },
            result = ItemStack.empty()
        )

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view,
            sourceRecipes = listOf(recipe)
        )

        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
        assertEquals(null, result.vanilla())
    }
}
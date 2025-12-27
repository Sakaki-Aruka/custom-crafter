package online.aruka.customcrafter.api.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.GroupRecipe
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun contextDefaultSuccessTest() {
        val context: GroupRecipe.Context = GroupRecipe.Context.default(
            coordinate = CoordinateComponent(0, 0)
        )

        assertEquals(1, context.members.size)
        assertEquals(1, context.min)
    }

    @Test
    fun contextOfSuccessTest() {
        val name = "Context of success test"
        val context: GroupRecipe.Context = GroupRecipe.Context.of(
            members = setOf(CoordinateComponent(0, 0)),
            min = 1,
            name = name
        )

        assertEquals(CoordinateComponent(0, 0), context.members.first())
        assertEquals(1, context.min)
        assertEquals(name, context.name)
    }

    @Test
    fun contextOfDetectEmptyMembersTest() {
        assertThrows<IllegalArgumentException> {
            GroupRecipe.Context.of(members = emptySet(), min = 1)
        }
    }

    @Test
    fun contextOfDetectNegativeMinTest() {
        assertThrows<IllegalArgumentException> {
            GroupRecipe.Context.of(members = setOf(CoordinateComponent(0, 0)), min = -1)
        }
    }

    @Test
    fun contextIsValidGroupsDetectEmptyTest() {
        assertTrue(GroupRecipe.Context.isValidGroups(
            groups = emptySet(),
            items = emptyMap()
        ).isSuccess)
    }

    @Test
    fun contextIsValidGroupsDetectContainEmptyMembersTest() {
        assertTrue(GroupRecipe.Context.isValidGroups(
            groups = setOf(GroupRecipe.Context(members = emptySet(), min = 1)),
            items = mapOf()
        ).isFailure)
    }

    @Test
    fun contextIsValidGroupsDetectItemsNotAllMembersContainedTest() {
        assertTrue(GroupRecipe.Context.isValidGroups(
            groups = setOf(GroupRecipe.Context.of(
                members = setOf(
                    CoordinateComponent(0, 0),
                    CoordinateComponent(1, 0)
                ),
                min = 1
            )),
            items = mapOf(
                CoordinateComponent(0, 1) to CMatterImpl.of(Material.STONE),
            )
        ).isFailure)
    }

    @Test
    fun contextIsValidGroupsDetectContextMembersDuplicateTest() {
        assertTrue(GroupRecipe.Context.isValidGroups(
            groups = setOf(
                GroupRecipe.Context.of(
                    members = setOf(
                        CoordinateComponent(0, 1)
                    ),
                    min = 1),

                GroupRecipe.Context.of(
                    members = setOf(
                        CoordinateComponent(0, 1)
                    ),
                    min = 1)
            ),
            items = mapOf(
                CoordinateComponent(0, 0) to CMatterImpl.of(Material.STONE),
            )
        ).isFailure)
    }

    @Test
    fun contextIsValidGroupsDetectAirContainableNotEnoughTest() {
        val stoneAir: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE),
            includeAir = true
        )
        val groups: Set<GroupRecipe.Context> = setOf(
            GroupRecipe.Context.of(
                members = setOf(
                    CoordinateComponent(0, 0),
                    CoordinateComponent(0, 1),
                    CoordinateComponent(0, 2)
                ),
                min = 3
            )
        )

        val items: Map<CoordinateComponent, CMatter> = mapOf(
            CoordinateComponent(0, 0) to stoneAir,
            CoordinateComponent(0, 1) to stoneAir,
            CoordinateComponent(0, 2) to CMatterImpl.of(Material.STONE)
        )

        assertTrue(GroupRecipe.Context.isValidGroups(groups, items).isFailure)
    }

    @Test
    fun contextIsValidGroupsDetectMinCoordinateAirContainableTest() {
        val stoneAir: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE),
            includeAir = true
        )
        val groups: Set<GroupRecipe.Context> = setOf(
            GroupRecipe.Context.of(
                members = setOf(
                    CoordinateComponent(0, 0),
                    CoordinateComponent(0, 1),
                    CoordinateComponent(0, 2)
                ),
                min = 3
            )
        )

        val items: Map<CoordinateComponent, CMatter> = mapOf(
            CoordinateComponent(0, 0) to stoneAir,
            CoordinateComponent(0, 1) to stoneAir,
            CoordinateComponent(0, 2) to stoneAir
        )

        assertTrue(GroupRecipe.Context.isValidGroups(groups, items).isFailure)
    }

    private fun marbleRecipe(): CRecipe {
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
        for (c in CoordinateComponent.squareFill(6)) {
            items[c] = calcite
        }
        items[CoordinateComponent(0, 0)] = stone
        items[CoordinateComponent(5, 0)] = stone
        items[CoordinateComponent(0, 5)] = stone
        items[CoordinateComponent(5, 5)] = stone

        val calciteGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.CALCITE in m.candidate }.keys,
            min = 4,
            name = "Calcite Group Context"
        )
        val stoneGroup = GroupRecipe.Context.of(
            members = items.filter { (_, m) -> Material.STONE in m.candidate }.keys,
            min = 4,
            name = "Stone Group Context"
        )

        return GroupRecipe(
            name = "Marble",
            items = items,
            //filters = GroupRecipe.createFilters(CRecipeImpl.getDefaultFilters()),
            groups = setOf(calciteGroup, stoneGroup)
        )
    }

    @Test
    fun fullSizeSearchSuccessTest() {
        val recipe: CRecipe = marbleRecipe()
        val view = CraftView(
            materials = recipe.items.entries.associate { (c, matter) ->
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
        val recipe: CRecipe = marbleRecipe()
        val view = CraftView(
            materials = recipe.items.entries.associate { (c, matter) ->
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
        val recipe: CRecipe = marbleRecipe()
        val calciteCoordinates: Set<CoordinateComponent> = recipe.items.entries.filter { (_, matter) ->
            matter.candidate.first() == Material.CALCITE
        }.map { (c, _) -> c }.take(4).toSet()

        val view = CraftView(
            materials = recipe.items.entries.associate { (c, matter) ->
                if (matter.candidate.first() == Material.STONE) {
                    c to ItemStack.of(Material.STONE)
                } else if (c in calciteCoordinates) {
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

    @Test
    fun size3SearchFailTest() {
        val recipe: CRecipe = marbleRecipe()
        val calciteCoordinates: Set<CoordinateComponent> = recipe.items.entries.filter { (_, matter) ->
            matter.candidate.first() == Material.CALCITE
        }.map { (c, _) -> c }.take(3).toSet()

        val view = CraftView(
            materials = recipe.items.entries.associate { (c, matter) ->
                if (matter.candidate.first() == Material.STONE) {
                    c to ItemStack.of(Material.STONE)
                } else if (c in calciteCoordinates) {
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

        assertEquals(0, result.size())
    }

    @Test
    fun requiresInputItemAmountMinMaxTest() {
        val cobblestoneAir: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.COBBLESTONE),
            includeAir = true
        )

        val stone: CMatter = CMatterImpl.of(Material.STONE)

        val items: Map<CoordinateComponent, CMatter> = mapOf(
            CoordinateComponent(0, 0) to cobblestoneAir,
            CoordinateComponent(0, 1) to cobblestoneAir,
            CoordinateComponent(0, 2) to cobblestoneAir,
            CoordinateComponent(1, 0) to stone,
            CoordinateComponent(1, 1) to stone,
            CoordinateComponent(1, 2) to stone
        )

        val groups: Set<GroupRecipe.Context> = setOf(
            GroupRecipe.Context.of(
                members = setOf(
                    CoordinateComponent(0, 0),
                    CoordinateComponent(0, 1),
                    CoordinateComponent(0, 2)
                ),
                min = 1
            )
        )

        val recipe: CRecipe = GroupRecipe(
            name = "",
            items = items,
            groups = groups
        )

        assertEquals(4, recipe.requiresInputItemAmountMin())
        assertEquals(6, recipe.requiresInputItemAmountMax())
    }
}
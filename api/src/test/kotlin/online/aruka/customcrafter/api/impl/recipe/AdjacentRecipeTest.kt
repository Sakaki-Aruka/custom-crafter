package online.aruka.customcrafter.api.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.AdjacentRecipe
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object AdjacentRecipeTest {

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

    private fun stone(): CMatter = CMatterImpl.of(Material.STONE)

    private fun checkerContext(
        recipe: AdjacentRecipe,
        coordinates: Set<CoordinateComponent>
    ): CRecipePredicate.Context = CRecipePredicate.Context(
        input = CraftView(coordinates.associateWith { ItemStack.of(Material.STONE) }),
        crafterID = UUID.randomUUID(),
        recipe = recipe,
        relation = MappedRelation(emptySet())
    )

    // -------------------------------------------------------------------------
    // RelationType.CROSS
    // -------------------------------------------------------------------------

    @Test
    fun crossTwoHorizontalAdjacentPassTest() {
        // # #   (0,0)-(1,0): no diagonal neighbours
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.CROSS)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 0)))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.CROSS).test(ctx))
    }

    @Test
    fun crossTwoVerticalAdjacentPassTest() {
        // #
        // #   (0,0)-(0,1): no diagonal neighbours
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.CROSS)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(0, 1)))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.CROSS).test(ctx))
    }

    @Test
    fun crossThreeLinearPassTest() {
        // # # #   (0,0)-(1,0)-(2,0): no diagonal neighbours in the row
        val recipe = AdjacentRecipe("test", listOf(stone(), stone(), stone()), AdjacentRecipe.RelationType.CROSS)
        val ctx = checkerContext(recipe, setOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(1, 0),
            CoordinateComponent(2, 0)
        ))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.CROSS).test(ctx))
    }

    @Test
    fun crossDiagonalNeighbourFailTest() {
        // (0,0) and (1,1) are diagonally adjacent — forbidden for CROSS
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.CROSS)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 1)))
        assertFalse(AdjacentRecipe.checker(AdjacentRecipe.RelationType.CROSS).test(ctx))
    }

    @Test
    fun crossLShapeFailTest() {
        // # _        (0,0) and (1,1) are diagonal neighbours → forbidden for CROSS
        // # #   (0,0), (0,1), (1,1)
        val recipe = AdjacentRecipe("test", listOf(stone(), stone(), stone()), AdjacentRecipe.RelationType.CROSS)
        val ctx = checkerContext(recipe, setOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(0, 1),
            CoordinateComponent(1, 1)
        ))
        assertFalse(AdjacentRecipe.checker(AdjacentRecipe.RelationType.CROSS).test(ctx))
    }

    // -------------------------------------------------------------------------
    // RelationType.DIAGONAL
    // -------------------------------------------------------------------------

    @Test
    fun diagonalTwoAdjacentPassTest() {
        // (0,0) and (1,1): diagonally adjacent, no cross neighbours
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.DIAGONAL)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 1)))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.DIAGONAL).test(ctx))
    }

    @Test
    fun diagonalThreeLinePassTest() {
        // (0,0)-(1,1)-(2,2): diagonal line, no cross neighbours between any pair
        val recipe = AdjacentRecipe("test", listOf(stone(), stone(), stone()), AdjacentRecipe.RelationType.DIAGONAL)
        val ctx = checkerContext(recipe, setOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(1, 1),
            CoordinateComponent(2, 2)
        ))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.DIAGONAL).test(ctx))
    }

    @Test
    fun diagonalCrossNeighbourFailTest() {
        // (0,0) and (1,0) are cross-adjacent — forbidden for DIAGONAL
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.DIAGONAL)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 0)))
        assertFalse(AdjacentRecipe.checker(AdjacentRecipe.RelationType.DIAGONAL).test(ctx))
    }

    @Test
    fun diagonalCrossNeighbourInGroupFailTest() {
        // (1,1) has both a diagonal neighbour (0,0) and a cross neighbour (2,1) → forbidden for DIAGONAL
        val recipe = AdjacentRecipe("test", listOf(stone(), stone(), stone()), AdjacentRecipe.RelationType.DIAGONAL)
        val ctx = checkerContext(recipe, setOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(1, 1),
            CoordinateComponent(2, 1)
        ))
        assertFalse(AdjacentRecipe.checker(AdjacentRecipe.RelationType.DIAGONAL).test(ctx))
    }

    // -------------------------------------------------------------------------
    // RelationType.BOTH
    // -------------------------------------------------------------------------

    @Test
    fun bothCrossAdjacentPassTest() {
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.BOTH)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 0)))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.BOTH).test(ctx))
    }

    @Test
    fun bothDiagonalAdjacentPassTest() {
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.BOTH)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 1)))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.BOTH).test(ctx))
    }

    @Test
    fun bothMixedCrossAndDiagonalPassTest() {
        // (0,0)-(1,0) cross, (1,0)-(2,1) diagonal: BOTH allows all 8 directions
        val recipe = AdjacentRecipe("test", listOf(stone(), stone(), stone()), AdjacentRecipe.RelationType.BOTH)
        val ctx = checkerContext(recipe, setOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(1, 0),
            CoordinateComponent(2, 1)
        ))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.BOTH).test(ctx))
    }

    @Test
    fun bothFarApartPassTest() {
        // (0,0) and (2,2): distance > 1, not neighbours — BOTH imposes no constraint so passes
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()), AdjacentRecipe.RelationType.BOTH)
        val ctx = checkerContext(recipe, setOf(CoordinateComponent(0, 0), CoordinateComponent(2, 2)))
        assertTrue(AdjacentRecipe.checker(AdjacentRecipe.RelationType.BOTH).test(ctx))
    }

    // -------------------------------------------------------------------------
    // isValidRecipe
    // -------------------------------------------------------------------------

    @Test
    fun isValidRecipeRejectsSingleItemTest() {
        val recipe = AdjacentRecipe("test", listOf(stone()))
        assertTrue(recipe.isValidRecipe().isFailure)
    }

    @Test
    fun isValidRecipeAcceptsTwoItemsTest() {
        val recipe = AdjacentRecipe("test", listOf(stone(), stone()))
        assertTrue(recipe.isValidRecipe().isSuccess)
    }

    // -------------------------------------------------------------------------
    // Search integration
    // -------------------------------------------------------------------------

    @Test
    fun searchCrossLinearSuccessTest() {
        val recipe = AdjacentRecipe(
            name = "CrossTest",
            matters = listOf(stone(), stone(), stone()),
            relationType = AdjacentRecipe.RelationType.CROSS
        )
        // Straight horizontal line: no diagonal neighbours anywhere
        val view = CraftView(mapOf(
            CoordinateComponent(0, 0) to ItemStack.of(Material.STONE),
            CoordinateComponent(1, 0) to ItemStack.of(Material.STONE),
            CoordinateComponent(2, 0) to ItemStack.of(Material.STONE)
        ))
        val result = Search.search(UUID.randomUUID(), view, sourceRecipes = listOf(recipe))
        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
    }

    @Test
    fun searchCrossDiagonalNeighbourFailTest() {
        val recipe = AdjacentRecipe(
            name = "CrossTest",
            matters = listOf(stone(), stone()),
            relationType = AdjacentRecipe.RelationType.CROSS
        )
        // (0,0) and (1,1): diagonally adjacent — forbidden for CROSS
        val view = CraftView(mapOf(
            CoordinateComponent(0, 0) to ItemStack.of(Material.STONE),
            CoordinateComponent(1, 1) to ItemStack.of(Material.STONE)
        ))
        val result = Search.search(UUID.randomUUID(), view, sourceRecipes = listOf(recipe))
        assertEquals(0, result.size())
    }

    @Test
    fun searchDiagonalAdjacentSuccessTest() {
        val recipe = AdjacentRecipe(
            name = "DiagonalTest",
            matters = listOf(stone(), stone()),
            relationType = AdjacentRecipe.RelationType.DIAGONAL
        )
        val view = CraftView(mapOf(
            CoordinateComponent(0, 0) to ItemStack.of(Material.STONE),
            CoordinateComponent(1, 1) to ItemStack.of(Material.STONE)
        ))
        val result = Search.search(UUID.randomUUID(), view, sourceRecipes = listOf(recipe))
        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
    }

    @Test
    fun searchDiagonalCrossNeighbourFailTest() {
        val recipe = AdjacentRecipe(
            name = "DiagonalTest",
            matters = listOf(stone(), stone()),
            relationType = AdjacentRecipe.RelationType.DIAGONAL
        )
        // (0,0) and (1,0): cross-adjacent — forbidden for DIAGONAL
        val view = CraftView(mapOf(
            CoordinateComponent(0, 0) to ItemStack.of(Material.STONE),
            CoordinateComponent(1, 0) to ItemStack.of(Material.STONE)
        ))
        val result = Search.search(UUID.randomUUID(), view, sourceRecipes = listOf(recipe))
        assertEquals(0, result.size())
    }
}

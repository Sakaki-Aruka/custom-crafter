package online.aruka.custom_crafter.api.impl.recipe

import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.recipe.MatchGroup
import io.github.sakaki_aruka.customcrafter.recipe.ShapelessGroupRecipe
import org.bukkit.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object ShapelessGroupRecipeTest {

    private val stone: CMatter = CMatterImpl.of(Material.STONE)

    @Test
    fun createGroupsFillsUncoveredCoordinates() {
        val items = mapOf(
            CoordinateComponent(0, 0) to stone,
            CoordinateComponent(1, 0) to stone
        )
        val explicit = MatchGroup.of(members = setOf(CoordinateComponent(0, 0)), min = 1, max = 1)

        val groups = ShapelessGroupRecipe.createGroups(items, setOf(explicit))

        assertEquals(2, groups.size)
        assertTrue(groups.any { it.members == setOf(CoordinateComponent(1, 0)) && it.min == 1 && it.max == 1 })
    }

    @Test
    fun isValidGroupsFailsOnDuplicateCoordinate() {
        val items = mapOf(CoordinateComponent(0, 0) to stone, CoordinateComponent(1, 0) to stone)
        val g1 = MatchGroup.of(setOf(CoordinateComponent(0, 0)), 1, 1)
        val g2 = MatchGroup.of(setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 0)), 1, 2)

        val result = ShapelessGroupRecipe.isValidGroups(setOf(g1, g2), items)
        assertTrue(result.isFailure)
    }

    @Test
    fun isValidGroupsFailsOnUncoveredCoordinate() {
        val items = mapOf(CoordinateComponent(0, 0) to stone, CoordinateComponent(1, 0) to stone)
        val g1 = MatchGroup.of(setOf(CoordinateComponent(0, 0)), 1, 1)

        val result = ShapelessGroupRecipe.isValidGroups(setOf(g1), items)
        assertTrue(result.isFailure)
    }

    @Test
    fun isValidGroupsFailsOnCoordinateOutsideItems() {
        val items = mapOf(CoordinateComponent(0, 0) to stone)
        val g1 = MatchGroup.of(setOf(CoordinateComponent(0, 0), CoordinateComponent(5, 5)), 1, 2)

        val result = ShapelessGroupRecipe.isValidGroups(setOf(g1), items)
        assertTrue(result.isFailure)
    }

    @Test
    fun isValidGroupsSucceedsOnFullCoverage() {
        val items = mapOf(CoordinateComponent(0, 0) to stone, CoordinateComponent(1, 0) to stone)
        val g1 = MatchGroup.of(setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 0)), 1, 2)

        val result = ShapelessGroupRecipe.isValidGroups(setOf(g1), items)
        assertTrue(result.isSuccess)
    }

    @Test
    fun isValidRecipeSucceedsForCoveredRecipe() {
        val items = mapOf(CoordinateComponent(0, 0) to stone, CoordinateComponent(1, 0) to stone)
        val recipe = ShapelessGroupRecipe(
            name = "test",
            items = items,
            groups = setOf(MatchGroup.of(items.keys, min = 1, max = 2))
        )
        assertTrue(recipe.isValidRecipe().isSuccess)
    }

    @Test
    fun matchGroupsReturnsConfiguredGroups() {
        val items = mapOf(CoordinateComponent(0, 0) to stone, CoordinateComponent(1, 0) to stone)
        val group = MatchGroup.of(items.keys, min = 1, max = 2)
        val recipe = ShapelessGroupRecipe(name = "test", items = items, groups = setOf(group))

        assertEquals(listOf(group), recipe.matchGroups())
        assertEquals(1, recipe.requiresInputItemAmountMin())
        assertEquals(2, recipe.requiresInputItemAmountMax())
    }
}

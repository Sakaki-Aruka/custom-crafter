package online.aruka.custom_crafter.api.search

import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.recipe.MatchGroup
import io.github.sakaki_aruka.customcrafter.recipe.ShapelessGroupRecipe
import io.github.sakaki_aruka.customcrafter.search.Search
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

/**
 * Behavioural tests for [ShapelessGroupRecipe] / [MatchGroup]-aware SHAPELESS matching.
 *
 * These specifically target the two bugs this feature fixes:
 * - A recipe whose physical input count is below `items.size` (but still meets each group's
 *   min) was previously rejected before `CRecipePredicate` ever ran.
 * - A greedy/priority-ordered matching can under-serve a group when its members' candidate
 *   materials overlap with another group's, even though a valid assignment exists.
 */
internal object ShapelessGroupSearchTest {
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

    private fun search(view: CraftView, recipe: ShapelessGroupRecipe): Search.SearchResult {
        return Search.search(
            crafterId = UUID.randomUUID(),
            view = view,
            sourceRecipes = listOf(recipe)
        )
    }

    private fun viewOf(vararg entries: Pair<Int, ItemStack>): CraftView {
        return CraftView(
            materials = entries.associate { (index, item) -> CoordinateComponent.fromIndex(index) to item }
        )
    }

    @Test
    fun fewerPhysicalInputsThanItemsSizeStillMatches() {
        // 1 mandatory slot + a 2-member optional group (min=1, max=2).
        // items.size == 3, but only 2 physical inputs are provided.
        val mandatory: CMatter = CMatterImpl.single(Material.STONE)
        val optionalA: CMatter = CMatterImpl.of(Material.STONE, Material.DIRT)
        val optionalB: CMatter = CMatterImpl.single(Material.DIRT)

        val cMandatory = CoordinateComponent(0, 0)
        val cA = CoordinateComponent(1, 0)
        val cB = CoordinateComponent(2, 0)

        val recipe = ShapelessGroupRecipe(
            name = "fewer-inputs",
            items = mapOf(cMandatory to mandatory, cA to optionalA, cB to optionalB),
            groups = setOf(
                MatchGroup.of(setOf(cMandatory), min = 1, max = 1),
                MatchGroup.of(setOf(cA, cB), min = 1, max = 2)
            )
        )

        assertEquals(2, recipe.requiresInputItemAmountMin())
        assertEquals(3, recipe.requiresInputItemAmountMax())

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        val relation: MappedRelation = result.customs().first().second
        assertEquals(2, relation.components.size)
        assertTrue(cMandatory in relation.components.map { it.recipe })
        // exactly one of the optional group's members must be matched
        assertEquals(1, relation.components.map { it.recipe }.count { it == cA || it == cB })
    }

    @Test
    fun tooFewInputsForGroupMinimumFailsToMatch() {
        val cMandatory = CoordinateComponent(0, 0)
        val cA = CoordinateComponent(1, 0)
        val cB = CoordinateComponent(2, 0)

        val recipe = ShapelessGroupRecipe(
            name = "not-enough",
            items = mapOf(
                cMandatory to CMatterImpl.single(Material.STONE),
                cA to CMatterImpl.single(Material.DIRT),
                cB to CMatterImpl.single(Material.DIRT)
            ),
            groups = setOf(
                MatchGroup.of(setOf(cMandatory), min = 1, max = 1),
                MatchGroup.of(setOf(cA, cB), min = 2, max = 2)
            )
        )

        // group needs both of its members; only 1 DIRT provided alongside the mandatory STONE.
        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)
        assertEquals(0, result.customs().size)
    }

    @Test
    fun overlappingCandidatesAcrossGroupsStillFindsValidAssignment() {
        // Counterexample to a naive "mandatory-first, then greedily maximize each group"
        // strategy: a greedy matcher can over-fill group1 (matching BOTH of its members using
        // the only two STONE inputs), starving group2 of the STONE it needs, even though a
        // valid assignment exists that satisfies everyone by using only one of group1's members.
        val mandatory: CMatter = CMatterImpl.single(Material.STONE)      // M: STONE only
        val g1a: CMatter = CMatterImpl.of(Material.STONE, Material.DIRT) // G1a: STONE or DIRT
        val g1b: CMatter = CMatterImpl.single(Material.DIRT)             // G1b: DIRT only
        val g2a: CMatter = CMatterImpl.single(Material.STONE)            // G2a: STONE only

        val cM = CoordinateComponent(0, 0)
        val cG1a = CoordinateComponent(1, 0)
        val cG1b = CoordinateComponent(2, 0)
        val cG2a = CoordinateComponent(3, 0)

        val recipe = ShapelessGroupRecipe(
            name = "overlap-counterexample",
            items = mapOf(cM to mandatory, cG1a to g1a, cG1b to g1b, cG2a to g2a),
            groups = setOf(
                MatchGroup.of(setOf(cM), min = 1, max = 1),
                MatchGroup.of(setOf(cG1a, cG1b), min = 1, max = 2),
                MatchGroup.of(setOf(cG2a), min = 1, max = 1)
            )
        )

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.STONE),
            2 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        val relation: MappedRelation = result.customs().first().second
        val matchedRecipeCoords = relation.components.map { it.recipe }.toSet()

        assertEquals(3, relation.components.size)
        assertTrue(cM in matchedRecipeCoords)
        assertTrue(cG2a in matchedRecipeCoords)
        assertEquals(1, matchedRecipeCoords.count { it == cG1a || it == cG1b })

        // whichever group-1 member matched must actually be compatible with the input it got.
        for (component in relation.components) {
            val matter: CMatter = recipe.items.getValue(component.recipe)
            val input: ItemStack = view.materials.getValue(component.input)
            assertTrue(input.type in matter.candidate)
        }
    }

    @Test
    fun groupMaxCapsHowManyMembersAreMatched() {
        val cA = CoordinateComponent(0, 0)
        val cB = CoordinateComponent(1, 0)
        val cC = CoordinateComponent(2, 0)
        val cD = CoordinateComponent(3, 0) // overflow absorber, min=0

        val stone = { CMatterImpl.single(Material.STONE) }

        val recipe = ShapelessGroupRecipe(
            name = "max-cap",
            items = mapOf(cA to stone(), cB to stone(), cC to stone(), cD to stone()),
            groups = setOf(
                MatchGroup.of(setOf(cA, cB, cC), min = 1, max = 2),
                MatchGroup.of(setOf(cD), min = 0, max = 1)
            )
        )
        assertEquals(1, recipe.requiresInputItemAmountMin())
        assertEquals(3, recipe.requiresInputItemAmountMax())

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.STONE),
            2 to ItemStack(Material.STONE)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        val relation: MappedRelation = result.customs().first().second
        val cappedGroupMatches = relation.components.map { it.recipe }.count { it in setOf(cA, cB, cC) }
        assertTrue(cappedGroupMatches in 1..2, "capped group must never exceed its max (2), was $cappedGroupMatches")
    }
}

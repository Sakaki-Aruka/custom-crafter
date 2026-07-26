package online.aruka.custom_crafter.api.debug

import io.github.sakaki_aruka.customcrafter.debug.Explainer
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicates
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicates
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
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
 * Verifies the diagnostics [Search] records into an [Explainer] are specific enough to explain
 * why a recipe did not match.
 */
internal object SearchExplainerTest {
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

    private fun viewOf(vararg entries: Pair<Int, ItemStack>): CraftView {
        return CraftView(
            materials = entries.associate { (index, item) -> CoordinateComponent.fromIndex(index) to item }
        )
    }

    private fun search(view: CraftView, recipes: List<CRecipe>, explainer: Explainer): Search.SearchResult {
        return Search.search(
            crafterId = UUID.randomUUID(),
            view = view,
            sourceRecipes = recipes,
            explainer = explainer
        )
    }

    private fun assertAnyLineContains(explainer: Explainer, vararg fragments: String) {
        val lines: List<String> = explainer.getStringList()
        fragments.forEach { fragment ->
            assertTrue(
                lines.any { line -> line.contains(fragment) },
                "no log line contains '$fragment'. lines=\n${lines.joinToString("\n")}"
            )
        }
    }

    @Test
    fun excludedRecipeReportsWhyItNeverBecameACandidate() {
        // a 2-slot recipe can never be reached by a 1-item input, and that must be visible
        val recipe = CRecipeImpl.shapeless(
            "two_stones",
            listOf(CMatterImpl.single(Material.STONE), CMatterImpl.single(Material.STONE))
        )
        val view = viewOf(0 to ItemStack(Material.STONE))
        val explainer = Explainer("t", Explainer.Loglevel.DEBUG)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(0, result.customs().size)
        assertAnyLineContains(explainer, "candidateFilter", "excluded", "two_stones", "inputSize=1", "not in requires=2..2")
    }

    @Test
    fun shapelessReportsTheSlotThatNoInputSatisfies() {
        // IRON_INGOT slot has no possible input, so the flow cannot reach the required minimum
        val recipe = CRecipeImpl.shapeless(
            "stone_and_iron",
            listOf(CMatterImpl.single(Material.STONE), CMatterImpl.single(Material.IRON_INGOT))
        )
        val view = viewOf(0 to ItemStack(Material.STONE), 1 to ItemStack(Material.DIRT))
        val explainer = Explainer("t", Explainer.Loglevel.DEBUG)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(0, result.customs().size)
        assertAnyLineContains(explainer, "flowCheck", "unsatisfied", "no input passes candidate=[IRON_INGOT]")
    }

    @Test
    fun shapelessReportsWhenMatterPredicatesRejectEveryInput() {
        // the input passes candidate/amount, so only a predicate can be responsible
        val recipe = CRecipeImpl.shapeless(
            "picky_stone",
            listOf(
                CMatterImpl(
                    name = "picky",
                    candidate = setOf(Material.STONE),
                    amount = 1,
                    anyAmount = false,
                    predicates = listOf(CMatterPredicate { false })
                )
            )
        )
        val view = viewOf(0 to ItemStack(Material.STONE))
        val explainer = Explainer("t", Explainer.Loglevel.DEBUG)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(0, result.customs().size)
        assertAnyLineContains(explainer, "flowCheck", "all rejected by matter predicates")
    }

    @Test
    fun shapedReportsTheFailingMatterPredicateByName() {
        val recipe = CRecipeImpl(
            name = "named_matter_predicate",
            type = CRecipe.Type.SHAPED,
            items = mapOf(
                CoordinateComponent(0, 0) to CMatterImpl(
                    name = "picky",
                    candidate = setOf(Material.STONE),
                    amount = 1,
                    anyAmount = false,
                    predicates = listOf(CMatterPredicates.named("alwaysReject") { false })
                )
            )
        )
        val view = viewOf(0 to ItemStack(Material.STONE))
        val explainer = Explainer("t", Explainer.Loglevel.DEBUG)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(0, result.customs().size)
        assertAnyLineContains(explainer, "matterPredicateCheck", "alwaysReject", "predicate#0/1")
    }

    @Test
    fun shapedReportsAnUnnamedRecipePredicateByIndex() {
        val recipe = CRecipeImpl(
            name = "unnamed_recipe_predicate",
            type = CRecipe.Type.SHAPED,
            items = mapOf(CoordinateComponent(0, 0) to CMatterImpl.single(Material.STONE)),
            predicates = listOf(CRecipePredicate { true }, CRecipePredicate { false })
        )
        val view = viewOf(0 to ItemStack(Material.STONE))
        val explainer = Explainer("t", Explainer.Loglevel.DEBUG)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(0, result.customs().size)
        assertAnyLineContains(explainer, "recipePredicateCheck", "predicate#1/2")
    }

    @Test
    fun combinedRecipePredicateReportsItsStructure() {
        val predicate = CRecipePredicates.run {
            CRecipePredicates.named("hasStone") { true }
                .and(CRecipePredicates.named("neverPasses") { false })
        }
        val recipe = CRecipeImpl(
            name = "combined_predicate",
            type = CRecipe.Type.SHAPED,
            items = mapOf(CoordinateComponent(0, 0) to CMatterImpl.single(Material.STONE)),
            predicates = listOf(predicate)
        )
        val view = viewOf(0 to ItemStack(Material.STONE))
        val explainer = Explainer("t", Explainer.Loglevel.DEBUG)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(0, result.customs().size)
        assertAnyLineContains(explainer, "recipePredicateCheck", "(hasStone and neverPasses)")
    }

    @Test
    fun predicateCanRecordItsOwnReasonThroughTheContext() {
        val recipe = CRecipeImpl(
            name = "self_reporting",
            type = CRecipe.Type.SHAPED,
            items = mapOf(
                CoordinateComponent(0, 0) to CMatterImpl(
                    name = "picky",
                    candidate = setOf(Material.STONE),
                    amount = 1,
                    anyAmount = false,
                    predicates = listOf(
                        CMatterPredicate { ctx ->
                            ctx.explainer?.writeLog(Explainer.Loglevel.DEBUG, "custom check: amount=${ctx.input.amount} required>=64")
                            ctx.input.amount >= 64
                        }
                    )
                )
            )
        )
        val view = viewOf(0 to ItemStack(Material.STONE))
        val explainer = Explainer("t", Explainer.Loglevel.DEBUG)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(0, result.customs().size)
        assertAnyLineContains(explainer, "custom check: amount=1 required>=64")
    }

    @Test
    fun matchedRecipeReportsItsRelation() {
        val recipe = CRecipeImpl.shapeless("ok", listOf(CMatterImpl.single(Material.STONE)))
        val view = viewOf(0 to ItemStack(Material.STONE))
        val explainer = Explainer("t", Explainer.Loglevel.INFO)

        val result = search(view, listOf(recipe), explainer)

        assertEquals(1, result.customs().size)
        assertAnyLineContains(explainer, "recipe matched: name=ok", "relation=")
    }
}

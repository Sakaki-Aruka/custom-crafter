package online.aruka.custom_crafter.api.search

import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.result.ResultSupplier
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
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Behavioural contract tests for the shapeless matching engine ([Search]).
 *
 * These tests are implementation-agnostic: they must pass with any correct
 * bipartite-assignment implementation (the original choco-solver based one
 * and its replacements alike).
 */
internal object ShapelessSearchTest {
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

    // ---- helpers -----------------------------------------------------------

    private fun search(view: CraftView, recipe: CRecipe): Search.SearchResult {
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

    /**
     * Asserts that [relation] is a valid full assignment of [recipe]'s slots
     * onto distinct input slots of [view], respecting candidate materials
     * and amount requirements.
     */
    private fun assertValidRelation(recipe: CRecipe, view: CraftView, relation: MappedRelation) {
        assertEquals(recipe.items.size, relation.components.size,
            "every recipe slot must be mapped exactly once")
        assertEquals(recipe.items.keys, relation.components.map { it.recipe }.toSet(),
            "mapped recipe slots must equal the recipe's slot set")

        val usedInputs = relation.components.map { it.input }
        assertEquals(usedInputs.size, usedInputs.toSet().size,
            "input slots must not be assigned twice")

        for (component in relation.components) {
            val matter: CMatter = recipe.items.getValue(component.recipe)
            val input: ItemStack? = view.materials[component.input]
            assertTrue(input != null, "mapped input slot ${component.input} must exist in the view")
            assertTrue(
                input.type in matter.candidate,
                "input ${input.type} must be a candidate of matter '${matter.name}'")
            if (matter.anyAmount) {
                assertTrue(input.amount >= 1)
            } else {
                assertTrue(input.amount >= matter.amount,
                    "input amount ${input.amount} must satisfy required ${matter.amount}")
            }
        }
    }

    // ---- assignment / backtracking patterns --------------------------------

    @Test
    fun singleSlotSingleInputMatches() {
        val recipe = CRecipeImpl.shapeless("single", listOf(CMatterImpl.single(Material.STONE)))
        val view = viewOf(0 to ItemStack(Material.STONE))

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun backtrackingRequiredTwoSlots() {
        // X accepts {STONE, DIRT}, Y accepts {STONE} only.
        // Inputs: STONE, DIRT.
        // A greedy assignment may give STONE to X and leave Y unsatisfiable;
        // a correct engine must re-assign (X -> DIRT, Y -> STONE).
        val x = CMatterImpl.of(Material.STONE, Material.DIRT)
        val y = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl.shapeless("backtrack2", listOf(x, y))

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun backtrackingRequiredChainOfThree() {
        // M1 accepts {SAND, GRAVEL}, M2 accepts {GRAVEL, CLAY_BALL}, M3 accepts {CLAY_BALL} only.
        // Inputs: SAND, GRAVEL, CLAY_BALL.
        // The only valid assignment is M1 -> SAND, M2 -> GRAVEL, M3 -> CLAY_BALL,
        // which may require augmenting through the whole chain.
        val m1 = CMatterImpl.of(Material.SAND, Material.GRAVEL)
        val m2 = CMatterImpl.of(Material.GRAVEL, Material.CLAY_BALL)
        val m3 = CMatterImpl.single(Material.CLAY_BALL)
        val recipe = CRecipeImpl.shapeless("backtrack3", listOf(m1, m2, m3))

        val view = viewOf(
            0 to ItemStack(Material.GRAVEL),
            1 to ItemStack(Material.CLAY_BALL),
            2 to ItemStack(Material.SAND)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun noPerfectMatchingWhenTwoSlotsCompeteForOneInput() {
        // Both slots accept only STONE, but only one STONE is provided.
        // Every slot has a non-empty candidate list, yet no all-different
        // assignment exists.
        val stone = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl.shapeless("compete", listOf(stone, stone))

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)

        assertTrue(result.customs().isEmpty())
    }

    @Test
    fun duplicateMaterialMultiplicityExactMatches() {
        // Recipe requires STONE x2 slots + DIRT x1 slot.
        val stone = CMatterImpl.single(Material.STONE)
        val dirt = CMatterImpl.single(Material.DIRT)
        val recipe = CRecipeImpl.shapeless("multiplicity-ok", listOf(stone, stone, dirt))

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.STONE),
            2 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun duplicateMaterialMultiplicityMismatchFails() {
        // Recipe requires STONE x2 slots + DIRT x1 slot, but input provides
        // STONE x1 + DIRT x2.
        val stone = CMatterImpl.single(Material.STONE)
        val dirt = CMatterImpl.single(Material.DIRT)
        val recipe = CRecipeImpl.shapeless("multiplicity-ng", listOf(stone, stone, dirt))

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.DIRT),
            2 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)

        assertTrue(result.customs().isEmpty())
    }

    // ---- amount handling ----------------------------------------------------

    @Test
    fun amountBoundaryExactMatches() {
        val matter = CMatterImpl("triple stone", setOf(Material.STONE), amount = 3)
        val recipe = CRecipeImpl.shapeless("amount-eq", listOf(matter))
        val view = viewOf(0 to ItemStack(Material.STONE, 3))

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun amountBelowRequirementFails() {
        val matter = CMatterImpl("triple stone", setOf(Material.STONE), amount = 3)
        val recipe = CRecipeImpl.shapeless("amount-lt", listOf(matter))
        val view = viewOf(0 to ItemStack(Material.STONE, 2))

        val result = search(view, recipe)

        assertTrue(result.customs().isEmpty())
    }

    @Test
    fun amountAboveRequirementMatches() {
        val matter = CMatterImpl("triple stone", setOf(Material.STONE), amount = 3)
        val recipe = CRecipeImpl.shapeless("amount-gt", listOf(matter))
        val view = viewOf(0 to ItemStack(Material.STONE, 64))

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
    }

    @Test
    fun anyAmountAcceptsLargeStack() {
        val matter = CMatterImpl("any stone", setOf(Material.STONE), anyAmount = true)
        val recipe = CRecipeImpl.shapeless("any-amount", listOf(matter))
        val view = viewOf(0 to ItemStack(Material.STONE, 64))

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
    }

    @Test
    fun mixedAmountsResolveToCorrectSlots() {
        // Slot A requires STONE x5, slot B requires STONE x1.
        // Inputs: STONE x1 and STONE x5. A must take the 5-stack.
        val a = CMatterImpl("five", setOf(Material.STONE), amount = 5)
        val b = CMatterImpl("one", setOf(Material.STONE), amount = 1)
        val recipe = CRecipeImpl.shapeless("mixed-amount", listOf(a, b))

        val view = viewOf(
            0 to ItemStack(Material.STONE, 1),
            1 to ItemStack(Material.STONE, 5)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        val relation = result.customs().first().second
        assertValidRelation(recipe, view, relation)
        val aCoordinate = recipe.items.entries.first { it.value === a }.key
        val aInput = relation.components.first { it.recipe == aCoordinate }.input
        assertEquals(5, view.materials.getValue(aInput).amount,
            "the 5-requirement slot must be mapped to the 5-stack input")
    }

    // ---- predicate edges ----------------------------------------------------

    @Test
    fun predicateDistinguishesSameMaterialInputs() {
        // P: STONE whose predicate accepts only amount == 5 inputs.
        // Q: plain STONE.
        // Inputs: STONE x5 and STONE x1. P must be assigned the 5-stack.
        val p = CMatterImpl(
            name = "picky",
            candidate = setOf(Material.STONE),
            predicates = listOf(CMatterPredicate { ctx -> ctx.input.amount == 5 })
        )
        val q = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl.shapeless("pred-split", listOf(p, q))

        val view = viewOf(
            0 to ItemStack(Material.STONE, 5),
            1 to ItemStack(Material.STONE, 1)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        val relation = result.customs().first().second
        assertValidRelation(recipe, view, relation)
        val pCoordinate = recipe.items.entries.first { it.value === p }.key
        val pInput = relation.components.first { it.recipe == pCoordinate }.input
        assertEquals(5, view.materials.getValue(pInput).amount,
            "the predicate-guarded slot must be mapped to the amount==5 input")
    }

    @Test
    fun alwaysFalsePredicateBlocksMatch() {
        val matter = CMatterImpl(
            name = "never",
            candidate = setOf(Material.STONE),
            predicates = listOf(CMatterPredicate { false })
        )
        val recipe = CRecipeImpl.shapeless("pred-false", listOf(matter))
        val view = viewOf(0 to ItemStack(Material.STONE))

        val result = search(view, recipe)

        assertTrue(result.customs().isEmpty())
    }

    @Test
    fun predicateFalseOnNonCandidatePairDoesNotBlockOtherSlots() {
        // A's predicate returns false for DIRT inputs, but DIRT is not even a
        // candidate of A. The match A -> STONE, B -> DIRT must still succeed.
        val a = CMatterImpl(
            name = "stone-only",
            candidate = setOf(Material.STONE),
            predicates = listOf(CMatterPredicate { ctx -> ctx.input.type == Material.STONE })
        )
        val b = CMatterImpl.single(Material.DIRT)
        val recipe = CRecipeImpl.shapeless("pred-noncandidate", listOf(a, b))

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.DIRT)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun sharedMatterInstancePredicateRunsPerInput() {
        // The same matter instance guards 2 slots; its predicate depends on
        // the concrete input (amount >= 2). Inputs: amounts 2 and 3 -> match.
        val shared = CMatterImpl(
            name = "shared",
            candidate = setOf(Material.STONE),
            predicates = listOf(CMatterPredicate { ctx -> ctx.input.amount >= 2 })
        )
        val recipe = CRecipeImpl.shapeless("pred-shared", listOf(shared, shared))

        val okView = viewOf(
            0 to ItemStack(Material.STONE, 2),
            1 to ItemStack(Material.STONE, 3)
        )
        assertEquals(1, search(okView, recipe).customs().size)

        // One input violates the predicate -> no all-different assignment.
        val ngView = viewOf(
            0 to ItemStack(Material.STONE, 2),
            1 to ItemStack(Material.STONE, 1)
        )
        assertTrue(search(ngView, recipe).customs().isEmpty())
    }

    @Test
    fun recipePredicateFalseBlocksShapelessMatch() {
        val matter = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl.shapeless(
            "recipe-pred-false",
            listOf(matter),
            predicates = listOf(CRecipePredicate { false })
        )
        val view = viewOf(0 to ItemStack(Material.STONE))

        val result = search(view, recipe)

        assertTrue(result.customs().isEmpty())
    }

    @Test
    fun recipePredicateReceivesCompleteRelation() {
        var observedComponents = -1
        val matter = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl.shapeless(
            "recipe-pred-relation",
            listOf(matter, matter, matter),
            predicates = listOf(CRecipePredicate { ctx ->
                observedComponents = ctx.relation.components.size
                true
            })
        )
        val view = viewOf(
            0 to ItemStack(Material.STONE),
            10 to ItemStack(Material.STONE),
            20 to ItemStack(Material.STONE)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertEquals(3, observedComponents,
            "the recipe predicate must observe a fully-mapped relation")
    }

    // ---- grid scale / placement patterns ------------------------------------

    @Test
    fun scatteredInputsAcrossGridMatch() {
        val materials = listOf(Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD)
        val recipe = CRecipeImpl.shapeless(
            "scattered",
            materials.map { CMatterImpl.single(it) }
        )

        // Far-flung corners of the 6x6 region (indices are x + y*9, x/y in 0..5).
        val view = viewOf(
            0 to ItemStack(Material.EMERALD),      // (0, 0)
            14 to ItemStack(Material.DIAMOND),     // (5, 1)
            27 to ItemStack(Material.GOLD_INGOT),  // (0, 3)
            50 to ItemStack(Material.IRON_INGOT)   // (5, 5)
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun fullGridThirtySixDistinctMaterials() {
        val materials = listOf(
            Material.STONE, Material.DIRT, Material.SAND, Material.GRAVEL,
            Material.COBBLESTONE, Material.OAK_PLANKS, Material.IRON_INGOT, Material.GOLD_INGOT,
            Material.DIAMOND, Material.EMERALD, Material.COAL, Material.REDSTONE,
            Material.LAPIS_LAZULI, Material.QUARTZ, Material.BRICK, Material.STICK,
            Material.FEATHER, Material.FLINT, Material.LEATHER, Material.PAPER,
            Material.BONE, Material.STRING, Material.EGG, Material.SUGAR,
            Material.WHEAT, Material.APPLE, Material.ARROW, Material.BOOK,
            Material.BUCKET, Material.COMPASS, Material.CLOCK, Material.GLOWSTONE_DUST,
            Material.SLIME_BALL, Material.ENDER_PEARL, Material.BLAZE_ROD, Material.GHAST_TEAR
        )
        assertEquals(36, materials.size)
        assertEquals(36, materials.toSet().size)

        val recipe = CRecipeImpl.shapeless(
            "full-grid",
            materials.map { CMatterImpl.single(it) }
        )

        // Place the 36 items shuffled (fixed seed) over the whole 6x6 area.
        val coordinates = CoordinateComponent.squareFill(6).sortedBy { it.toIndex() }
        val shuffled = materials.shuffled(Random(42))
        val view = CraftView(
            materials = coordinates.zip(shuffled).associate { (c, m) -> c to ItemStack(m) }
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        val relation = result.customs().first().second
        assertEquals(36, relation.components.size)
        assertValidRelation(recipe, view, relation)
    }

    @Test
    fun fullGridSingleSharedMatter() {
        // 36 slots guarded by one shared matter instance; 36 identical inputs.
        val stone = CMatterImpl.single(Material.STONE)
        val coordinates = CoordinateComponent.squareFill(6).sortedBy { it.toIndex() }
        val recipe = CRecipeImpl(
            name = "full-grid-shared",
            items = coordinates.associateWith { stone },
            type = CRecipe.Type.SHAPELESS
        )
        val view = CraftView(
            materials = coordinates.associateWith { ItemStack(Material.STONE) }
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        assertValidRelation(recipe, view, result.customs().first().second)
    }

    @Test
    fun randomizedPlacementsAlwaysMatch() {
        val materials = listOf(
            Material.STONE, Material.DIRT, Material.SAND,
            Material.GRAVEL, Material.COBBLESTONE, Material.IRON_INGOT,
            Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD
        )
        val recipe = CRecipeImpl.shapeless(
            "randomized",
            materials.map { CMatterImpl.single(it) }
        )
        val allCoordinates = CoordinateComponent.squareFill(6).toList()
        val random = Random(2026)

        repeat(20) { iteration ->
            val coordinates = allCoordinates.shuffled(random).take(materials.size)
            val shuffledMaterials = materials.shuffled(random)
            val view = CraftView(
                materials = coordinates.zip(shuffledMaterials).associate { (c, m) -> c to ItemStack(m) }
            )

            val result = search(view, recipe)

            assertEquals(1, result.customs().size, "iteration $iteration must match")
            assertValidRelation(recipe, view, result.customs().first().second)
        }
    }

    @Test
    fun extraInputIgnoredWhenRecipeAllowsWiderInputRange() {
        // A custom CRecipe that accepts up to items.size + 1 inputs.
        // The engine assigns every recipe slot a distinct input and ignores
        // the leftover input item.
        val base = CRecipeImpl.shapeless(
            "wide-range-base",
            listOf(CMatterImpl.single(Material.STONE), CMatterImpl.single(Material.DIRT))
        )
        val recipe = object : CRecipe {
            override val name: String = "wide-range"
            override val items: Map<CoordinateComponent, CMatter> = base.items
            override val predicates: List<CRecipePredicate>? = null
            override val results: List<ResultSupplier>? = null
            override val type: CRecipe.Type = CRecipe.Type.SHAPELESS
            override fun requiresInputItemAmountMax(): Int = this.items.size + 1
        }

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.DIRT),
            2 to ItemStack(Material.APPLE) // leftover
        )

        val result = search(view, recipe)

        assertEquals(1, result.customs().size)
        val relation = result.customs().first().second
        assertEquals(recipe.items.size, relation.components.size)
        assertEquals(recipe.items.keys, relation.components.map { it.recipe }.toSet())
        val usedInputs = relation.components.map { it.input }
        assertEquals(usedInputs.size, usedInputs.toSet().size)
        for (component in relation.components) {
            val matter = recipe.items.getValue(component.recipe)
            assertTrue(view.materials.getValue(component.input).type in matter.candidate)
        }
    }

    @Test
    fun manyRecipesOnlyMatchingOnesReturned() {
        val stone = CMatterImpl.single(Material.STONE)
        val dirt = CMatterImpl.single(Material.DIRT)
        val matching = CRecipeImpl.shapeless("match", listOf(stone, stone))
        val nonMatching = CRecipeImpl.shapeless("no-match", listOf(stone, dirt))

        val view = viewOf(
            0 to ItemStack(Material.STONE),
            1 to ItemStack(Material.STONE)
        )

        val source: List<CRecipe> = List(25) { matching } + List(25) { nonMatching }
        val result = Search.search(
            crafterId = UUID.randomUUID(),
            view = view,
            sourceRecipes = source
        )

        assertEquals(25, result.customs().size)
        result.customs().forEach { (recipe, relation) ->
            assertEquals("match", recipe.name)
            assertValidRelation(recipe, view, relation)
        }
    }
}

package online.aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.search.PartialSearch
import io.github.sakaki_aruka.customcrafter.search.Search
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
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

internal object PartialSearchTest {
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

    // ── SHAPELESS ────────────────────────────────────────────────────────────

    @Test
    fun shapelessPartialNotEnoughTest() {
        // Recipe: STONE + IRON + GOLD (3 slots)
        // Input : STONE + IRON       (2 items)
        // -> PARTIAL_NOT_ENOUGH, matched=2, notEnough=1
        val stone = CMatterImpl.single(Material.STONE)
        val iron = CMatterImpl.single(Material.IRON_INGOT)
        val gold = CMatterImpl.single(Material.GOLD_INGOT)
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to iron,
                CoordinateComponent(2, 0) to gold,
            ),
            type = CRecipe.Type.SHAPELESS
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.STONE))
        ui.inventory.setItem(1, ItemStack.of(Material.IRON_INGOT))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            sourceRecipes = listOf(recipe)
        ).get()

        assertEquals(1, results.size)
        val result = results.first()
        assertEquals(PartialSearch.MatchState.PARTIAL_NOT_ENOUGH, result.state())
        assertEquals(2, result.matched().size)
        assertEquals(1, result.notEnough().size)
    }

    @Test
    fun shapelessDuplicateMatterStateTest() {
        // Recipe: STONE x2 slots
        // Input : STONE x1 item
        // -> max matching = 1 < 2 -> PARTIAL_NOT_ENOUGH
        val stone = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to stone,
            ),
            type = CRecipe.Type.SHAPELESS
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.STONE))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            sourceRecipes = listOf(recipe)
        ).get()

        assertEquals(1, results.size)
        assertEquals(PartialSearch.MatchState.PARTIAL_NOT_ENOUGH, results.first().state())
    }

    @Test
    fun shapelessAllMatchStateTest() {
        // Recipe: STONE + IRON (2 slots)
        // Input : STONE + IRON
        // -> state = ALL via fast path (Search.search finds full match)
        val stone = CMatterImpl.single(Material.STONE)
        val iron = CMatterImpl.single(Material.IRON_INGOT)
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to iron,
            ),
            type = CRecipe.Type.SHAPELESS
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.STONE))
        ui.inventory.setItem(1, ItemStack.of(Material.IRON_INGOT))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            sourceRecipes = listOf(recipe)
        ).get()

        assertEquals(1, results.size)
        assertEquals(PartialSearch.MatchState.ALL, results.first().state())
        assertTrue(results.first().notEnough().isEmpty())
    }

    @Test
    fun shapelessNoMatchTest() {
        // Recipe: STONE
        // Input : DIRT
        // -> empty result
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(CoordinateComponent(0, 0) to CMatterImpl.single(Material.STONE)),
            type = CRecipe.Type.SHAPELESS
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.DIRT))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            sourceRecipes = listOf(recipe)
        ).get()

        assertTrue(results.isEmpty())
    }

    // ── SHAPED ───────────────────────────────────────────────────────────────

    @Test
    fun shapedPartialMatchTest() {
        // Recipe: STONE x3 (横一列: (0,0)(1,0)(2,0))
        // Input : STONE x2 at (0,0)(1,0)
        // -> PARTIAL_NOT_ENOUGH, matched=2, notEnough=1
        val stone = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to stone,
                CoordinateComponent(2, 0) to stone,
            ),
            type = CRecipe.Type.SHAPED
        )

        val ui = CraftUI()
        ui.inventory.setItem(CoordinateComponent(0, 0).toIndex(), ItemStack.of(Material.STONE))
        ui.inventory.setItem(CoordinateComponent(1, 0).toIndex(), ItemStack.of(Material.STONE))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            sourceRecipes = listOf(recipe)
        ).get()

        assertTrue(results.isNotEmpty())
        val partialResults = results.filter { it.state() == PartialSearch.MatchState.PARTIAL_NOT_ENOUGH }
        assertTrue(partialResults.isNotEmpty())
        val best = partialResults.maxByOrNull { it.matched().size }!!
        assertEquals(2, best.matched().size)
        assertEquals(1, best.notEnough().size)
    }

    @Test
    fun shapedNoMatchTest() {
        // Recipe: STONE x1 at (0,0)
        // Input : DIRT at (0,0)
        // -> empty result
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(CoordinateComponent(0, 0) to CMatterImpl.single(Material.STONE)),
            type = CRecipe.Type.SHAPED
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.DIRT))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            sourceRecipes = listOf(recipe)
        ).get()

        assertTrue(results.isEmpty())
    }

    // ── asyncPartialSearch SearchMode ────────────────────────────────────────

    @Test
    fun asyncModeAllTest() {
        // 2 recipes both partially matchable
        // -> SearchMode.ALL returns results for both
        val stone = CMatterImpl.single(Material.STONE)
        val iron = CMatterImpl.single(Material.IRON_INGOT)

        val recipe1 = CRecipeImpl(
            name = "r1",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to stone,
            ),
            type = CRecipe.Type.SHAPELESS
        )
        val recipe2 = CRecipeImpl(
            name = "r2",
            items = mapOf(
                CoordinateComponent(0, 0) to iron,
                CoordinateComponent(1, 0) to iron,
            ),
            type = CRecipe.Type.SHAPELESS
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.STONE))
        ui.inventory.setItem(1, ItemStack.of(Material.IRON_INGOT))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            searchQuery = Search.SearchQuery(
                searchMode = Search.SearchQuery.SearchMode.ALL,
                vanillaSearchMode = Search.SearchQuery.VanillaSearchMode.IF_CUSTOMS_NOT_FOUND
            ),
            sourceRecipes = listOf(recipe1, recipe2)
        ).get()

        val matchedRecipes = results.map { it.recipe }.toSet()
        assertTrue(recipe1 in matchedRecipes)
        assertTrue(recipe2 in matchedRecipes)
    }

    @Test
    fun asyncModeOnlyFirstTest() {
        // 2 partially-matchable recipes, ONLY_FIRST -> exactly 1 recipe in result
        val stone = CMatterImpl.single(Material.STONE)

        val recipe1 = CRecipeImpl(
            name = "r1",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to stone,
            ),
            type = CRecipe.Type.SHAPELESS
        )
        val recipe2 = CRecipeImpl(
            name = "r2",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to stone,
                CoordinateComponent(2, 0) to stone,
            ),
            type = CRecipe.Type.SHAPELESS
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack.of(Material.STONE))

        val results = PartialSearch.asyncPartialSearch(
            UUID.randomUUID(), ui.toView(),
            searchQuery = Search.SearchQuery(
                searchMode = Search.SearchQuery.SearchMode.ONLY_FIRST,
                vanillaSearchMode = Search.SearchQuery.VanillaSearchMode.IF_CUSTOMS_NOT_FOUND
            ),
            sourceRecipes = listOf(recipe1, recipe2)
        ).get()

        // ONLY_FIRST: all results in the list belong to the same recipe
        assertTrue(results.isNotEmpty())
        assertEquals(1, results.map { it.recipe }.toSet().size)
    }
}

package online.aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertTrue

object CRecipeTest {

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
    fun detectEmptyItemsTest() {
        val empty = CRecipeImpl(
            name = "",
            items = emptyMap(),
            type = CRecipeType.AMORPHOUS
        )
        assertTrue(empty.isValidRecipe().isFailure)
        assertTrue(empty.isValidRecipe().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun detectOverTheLimitItemsTest() {
        val matter = CMatterImpl.single(Material.STONE)
        val over = CRecipeImpl(
            name = "",
            items = (0..36).associate { i ->
                CoordinateComponent.fromIndex(i, false) to matter },
            type = CRecipeType.AMORPHOUS
        )
        assertTrue(over.isValidRecipe().isFailure)
        assertTrue(over.isValidRecipe().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun detectContainsAirMatterTest() {
        val airMatter = CMatterImpl(
            name = "",
            candidate = setOf(Material.AIR)
        )
        val containsAir = CRecipeImpl(
            name = "",
            items = mapOf(CoordinateComponent(0, 0) to airMatter),
            type = CRecipeType.AMORPHOUS
        )
        assertTrue(containsAir.isValidRecipe().isFailure)
        assertTrue(containsAir.isValidRecipe().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun detectContainsNotItemMatterTest() {
        val notItem = CMatterImpl(
            name = "",
            candidate = setOf(Material.WATER)
        )
        val containsNotItem = CRecipeImpl(
            name = "",
            items = mapOf(CoordinateComponent(0, 0) to notItem),
            type = CRecipeType.AMORPHOUS
        )
        assertTrue(containsNotItem.isValidRecipe().isFailure)
        assertTrue(notItem.isValidMatter().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun validCMatterTest() {
        val matter = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(CoordinateComponent(0, 0) to matter),
            type = CRecipeType.AMORPHOUS
        )
        assertTrue(recipe.isValidRecipe().isSuccess)
        assertNull(recipe.isValidRecipe().exceptionOrNull())
    }
}
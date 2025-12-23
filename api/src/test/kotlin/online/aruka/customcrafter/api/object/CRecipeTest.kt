package online.aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals
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
            type = CRecipe.Type.SHAPELESS
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
                CoordinateComponent.fromIndex(i) to matter },
            type = CRecipe.Type.SHAPELESS
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
            type = CRecipe.Type.SHAPELESS
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
            type = CRecipe.Type.SHAPELESS
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
            type = CRecipe.Type.SHAPELESS
        )
        assertTrue(recipe.isValidRecipe().isSuccess)
        assertNull(recipe.isValidRecipe().exceptionOrNull())
    }

    @Test
    fun getMinAmountTest() {
        // shift       : false
        // mass        : false
        // air         : false
        // multi amount: false
        val matter = CMatterImpl.of(Material.STONE)
        val map = CoordinateComponent.squareFill(3).associateWith { matter }
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)

        val view = CoordinateComponent.squareFill(3).associateWith { ItemStack.of(Material.STONE, 64) }
        val relation = MappedRelation(
            components = CoordinateComponent.squareFill(3).map { MappedRelationComponent(it, it) }.toSet()
        )

        assertEquals(1, recipe.getMinAmount(view, relation, shift = false))
    }

    @Test
    fun getMinAmountWithMassTest() {
        // shift       : false
        // mass        : true
        // air         : false
        // multi amount: false
        val matter = CMatterImpl.of(Material.STONE)
        val massMatter = CMatterImpl("", setOf(Material.STONE), mass = true)
        val map = CoordinateComponent.squareFill(3).associateWith { matter } + mapOf(CoordinateComponent(1, 1) to massMatter)
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)

        val view = CoordinateComponent.squareFill(3).associateWith { ItemStack.of(Material.STONE, 64) }
        val relation = MappedRelation(
            components = CoordinateComponent.squareFill(3).map { MappedRelationComponent(it, it) }.toSet()
        )

        assertEquals(1, recipe.getMinAmount(view, relation, shift = false, withoutMass = false, includeAir = false))
    }

    @Test
    fun getMinAmountWithoutMassAndIncludeAirTest() {
        // true, true
    }

    @Test
    fun getMinAmountWithMassAndNotIncludeAirTest() {
        // false, true
    }
}
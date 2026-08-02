package online.aruka.custom_crafter.api.`object`

import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

object CMatterTest {

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
    fun detectEmptyCandidateTest() {
        val matter = CMatterImpl(
            name = "",
            candidate = emptySet()
        )
        assertThrows<IllegalStateException> { matter.isValidMatter() }
    }

    @Test
    fun detectAirCandidateTest() {
        val air = CMatterImpl(
            name = "",
            candidate = setOf(Material.AIR)
        )
        assertThrows<IllegalStateException> { air.isValidMatter() }
    }

    @Test
    fun detectNotItemCandidateTest() {
        val noItem = CMatterImpl(
            name = "",
            candidate = setOf(Material.WATER)
        )
        assertThrows<IllegalStateException> { noItem.isValidMatter() }
    }

    @Test
    fun detectNegativeAmountTest() {
        val minus = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            amount = -1
        )
        assertThrows<IllegalStateException> { minus.isValidMatter() }
    }

    @Test
    fun validCMatterTest() {
        val valid = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            amount = 1
        )
        assertDoesNotThrow { valid.isValidMatter() }
    }

    private fun context(matter: CMatter): CMatterPredicate.Context {
        val recipe: CRecipe = CRecipeImpl(
            name = "",
            items = mapOf(CoordinateComponent(0, 0) to matter),
            type = CRecipe.Type.SHAPED
        )
        return CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = ItemStack.of(Material.STONE),
            mapped = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
            recipe = recipe,
            crafterId = UUID.randomUUID()
        )
    }

    @Test
    fun firstFailedPredicateReturnsNullWhenAllPassTest() {
        val matter = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            predicates = listOf(CMatterPredicate { true }, CMatterPredicate { true })
        )
        assertNull(matter.firstFailedPredicate(context(matter)))
    }

    @Test
    fun firstFailedPredicateReturnsIndexAndPredicateOfFirstFailureTest() {
        val failing = CMatterPredicate { false }
        val matter = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            predicates = listOf(CMatterPredicate { true }, failing, CMatterPredicate { false })
        )

        val result = matter.firstFailedPredicate(context(matter))

        assertEquals(1, result?.first)
        assertEquals(failing, result?.second)
    }
}
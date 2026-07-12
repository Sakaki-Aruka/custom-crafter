package online.aruka.custom_crafter.api.util

import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicates.allOf
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicates.and
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicates.nOf
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicates.or
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object CMatterPredicatesTest {

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

    private fun context(): CMatterPredicate.Context {
        val matter = CMatterImpl.of(Material.DIRT)
        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        return CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = ItemStack.of(Material.DIRT),
            mapped = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.DIRT)),
            recipe = recipe,
            crafterId = UUID.randomUUID()
        )
    }

    private val alwaysTrue = CMatterPredicate { true }
    private val alwaysFalse = CMatterPredicate { false }

    @Test
    fun andTrueTrueTest() {
        assertTrue(alwaysTrue.and(alwaysTrue).test(context()))
    }

    @Test
    fun andTrueFalseTest() {
        assertFalse(alwaysTrue.and(alwaysFalse).test(context()))
    }

    @Test
    fun andFalseFalseTest() {
        assertFalse(alwaysFalse.and(alwaysFalse).test(context()))
    }

    @Test
    fun orTrueFalseTest() {
        assertTrue(alwaysTrue.or(alwaysFalse).test(context()))
    }

    @Test
    fun orFalseTrueTest() {
        assertTrue(alwaysFalse.or(alwaysTrue).test(context()))
    }

    @Test
    fun orFalseFalseTest() {
        assertFalse(alwaysFalse.or(alwaysFalse).test(context()))
    }

    @Test
    fun allOfAllPassTest() {
        assertTrue(alwaysTrue.allOf(alwaysTrue, alwaysTrue).test(context()))
    }

    @Test
    fun allOfOneFailsTest() {
        assertFalse(alwaysTrue.allOf(alwaysTrue, alwaysFalse).test(context()))
    }

    @Test
    fun allOfNoVarargsPassTest() {
        assertTrue(alwaysTrue.allOf().test(context()))
    }

    @Test
    fun allOfNoVarargsFailsTest() {
        assertFalse(alwaysFalse.allOf().test(context()))
    }

    @Test
    fun nOfExactlyThresholdTest() {
        assertTrue(alwaysTrue.nOf(2, alwaysTrue, alwaysFalse).test(context()))
    }

    @Test
    fun nOfAboveThresholdTest() {
        assertTrue(alwaysTrue.nOf(2, alwaysTrue, alwaysTrue).test(context()))
    }

    @Test
    fun nOfBelowThresholdTest() {
        assertFalse(alwaysTrue.nOf(3, alwaysFalse, alwaysFalse).test(context()))
    }

    @Test
    fun nOfZeroAlwaysPassesTest() {
        assertTrue(alwaysFalse.nOf(0, alwaysFalse, alwaysFalse).test(context()))
    }
}

package online.aruka.custom_crafter.api.util

import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicates.allOf
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicates.and
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicates.nOf
import io.github.sakaki_aruka.customcrafter.recipe.CRecipePredicates.or
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

object CRecipePredicatesTest {

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

    private fun context(): CRecipePredicate.Context {
        val matter = CMatterImpl.of(Material.DIRT)
        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        return CRecipePredicate.Context(
            input = CraftView(mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.DIRT))),
            crafterId = UUID.randomUUID(),
            recipe = recipe,
            relation = MappedRelation(emptySet())
        )
    }

    private val alwaysTrue = CRecipePredicate { true }
    private val alwaysFalse = CRecipePredicate { false }

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

package online.aruka.customcrafter.api.impl.matter

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
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

object CEnchantMatterImplTest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
        server.addPlayer()
    }

    @AfterEach
    fun tearDown() {
        CustomCrafterAPI.unregisterAllRecipes()
        MockBukkit.unmock()
    }

    @Test
    fun defaultEnchantCheckerWithAirTest() {
        val matter = CMatterImpl.of(Material.DIRT)
        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        val map = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.DIRT))

        val context = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = ItemStack.empty(),
            mapped = map,
            recipe = recipe,
            crafterID = UUID.randomUUID()
        )

        assertTrue(CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER.test(context))
    }

    @Test
    fun defaultEnchantCheckerWithNonEnchantMatterTest() {
        val matter = CMatterImpl.of(Material.DIRT)
        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        val map = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.DIRT))

        val context = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = ItemStack.of(Material.DIRT),
            mapped = map,
            recipe = recipe,
            crafterID = UUID.randomUUID()
        )

        assertTrue(CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER.test(context))
    }

    @Test
    fun defaultEnchantCheckerWithEmptyComponentsTest() {
        val matter = CEnchantMatterImpl("", setOf(Material.DIRT), emptySet())
        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        val map = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.DIRT))

        val context = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = ItemStack.of(Material.DIRT),
            mapped = map,
            recipe = recipe,
            crafterID = UUID.randomUUID()
        )

        assertTrue(CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER.test(context))
    }

    @Test
    fun defaultEnchantCheckerWithNoEnchantsItemTest() {
        val matter = CEnchantMatterImpl(
            "",
            setOf(Material.DIRT),
            setOf(
                CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.STRICT)
            )
        )

        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        val map = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.DIRT))

        val context = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = ItemStack.of(Material.DIRT),
            mapped = map,
            recipe = recipe,
            crafterID = UUID.randomUUID()
        )

        assertFalse(CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER.test(context))
    }

    @Test
    fun baseCheckOnlyEnchantTest() {
        assertTrue(
            CEnchantMatterImpl.enchantBaseCheck(
                mapOf(Enchantment.EFFICIENCY to 5),
                CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.ONLY_ENCHANT))
        )

        assertTrue(
            CEnchantMatterImpl.enchantBaseCheck(
                mapOf(Enchantment.EFFICIENCY to 1),
                CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.ONLY_ENCHANT))
        )
    }

    @Test
    fun baseCheckStrictTest() {
        assertTrue(
            CEnchantMatterImpl.enchantBaseCheck(
                mapOf(Enchantment.EFFICIENCY to 5),
                CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.STRICT)
            )
        )

        assertFalse(
            CEnchantMatterImpl.enchantBaseCheck(
                mapOf(Enchantment.EFFICIENCY to 1),
                CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.STRICT)
            )
        )
    }
}
package online.aruka.customcrafter.api.impl.matter

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search.SearchSession
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.potion.CPotionMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object CPotionMatterImplTest {
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
    fun defaultPotionCheckerWithAirTrueTest() {
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
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        assertTrue(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context))
    }

    @Test
    fun defaultPotionCheckerWithNoPotionMatterTrueTest() {
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
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        assertTrue(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context))
    }

    @Test
    fun defaultPotionCheckerWithEmptyComponentsTrueTest() {
        val matter = CPotionMatterImpl("", setOf(Material.DIRT), emptySet())
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
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        assertTrue(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context))
    }

    @Test
    fun defaultPotionCheckerWithNoPotionsItemFalseTest() {
        val matter = CPotionMatterImpl("", setOf(Material.DIRT), setOf(
            CPotionComponent(PotionEffect(PotionEffectType.BAD_OMEN, 200, 1), CPotionComponent.Strict.STRICT)
        ))
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
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        assertFalse(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context))
    }

    @Test
    fun defaultPotionCheckerWithOnlyEffectTest() {
        val matter = CPotionMatterImpl("", setOf(Material.POTION), setOf(
            CPotionComponent(PotionEffect(PotionEffectType.BAD_OMEN, 200, 1), CPotionComponent.Strict.ONLY_EFFECT)
        ))
        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        val lv1 = ItemStack.of(Material.POTION).apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                addCustomEffect(PotionEffect(PotionEffectType.BAD_OMEN, 200, 1), true)
            }
        }

        val lv3 = ItemStack.of(Material.POTION).apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                addCustomEffect(PotionEffect(PotionEffectType.BAD_OMEN, 200, 3), true)
            }
        }

        val map1 = mapOf(CoordinateComponent(0, 0) to lv1)
        val map3 = mapOf(CoordinateComponent(0, 0) to lv3)

        val context1 = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = lv1,
            mapped = map1,
            recipe = recipe,
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        val context3 = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = lv3,
            mapped = map3,
            recipe = recipe,
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        assertTrue(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context1))
        assertTrue(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context3))
    }

    @Test
    fun defaultPotionCheckerWithStrictTest() {
        val matter = CPotionMatterImpl("", setOf(Material.POTION), setOf(
            CPotionComponent(PotionEffect(PotionEffectType.BAD_OMEN, 200, 1), CPotionComponent.Strict.STRICT)
        ))
        val recipe = CRecipeImpl(
            "", mapOf(CoordinateComponent(0, 0) to matter), CRecipe.Type.SHAPED
        )

        val lv1 = ItemStack.of(Material.POTION).apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                addCustomEffect(PotionEffect(PotionEffectType.BAD_OMEN, 200, 1), true)
            }
        }

        val lv3 = ItemStack.of(Material.POTION).apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                addCustomEffect(PotionEffect(PotionEffectType.BAD_OMEN, 200, 3), true)
            }
        }

        val map1 = mapOf(CoordinateComponent(0, 0) to lv1)
        val map3 = mapOf(CoordinateComponent(0, 0) to lv3)

        val context1 = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = lv1,
            mapped = map1,
            recipe = recipe,
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        val context3 = CMatterPredicate.Context(
            coordinate = CoordinateComponent(0, 0),
            matter = matter,
            input = lv3,
            mapped = map3,
            recipe = recipe,
            crafterID = UUID.randomUUID(),
            session = SearchSession.SYNC_SESSION
        )

        assertTrue(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context1))
        assertFalse(CPotionMatterImpl.DEFAULT_POTION_CHECKER.predicate(context3))
    }
}
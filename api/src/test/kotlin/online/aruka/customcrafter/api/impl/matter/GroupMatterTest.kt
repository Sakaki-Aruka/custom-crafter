package online.aruka.customcrafter.api.impl.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantmentStoreMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.GroupRecipe
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
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

object GroupMatterTest {
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
    fun matterOfSuccessTest() {
        assertDoesNotThrow {
            GroupRecipe.Matter.of(
                matter = CMatterImpl.of(Material.STONE)
            )
        }
    }

    @Test
    fun matterOfFailWithClassTest() {
        val matter: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE)
        )

        assertThrows<IllegalArgumentException> {
            GroupRecipe.Matter.of(matter = matter)
        }
    }

    @Test
    fun isValidMatterDetectCandidateEmpty() {
        val matter: CMatter = CMatterImpl(
            name = "",
            candidate = setOf()
        )

        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(matter = matter, includeAir = false)
        }
    }

    @Test
    fun isValidMatterDetectNotItemMaterialTest() {
        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(name = "", candidate = setOf(Material.WATER))
            )
        }
    }

    @Test
    fun isValidMatterDetectZeroOrNegativeAmountTest() {
        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(
                    name = "",
                    candidate = setOf(Material.STONE),
                    amount = 0
                )
            )
        }

        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(
                    name = "",
                    candidate = setOf(Material.STONE),
                    amount = -1
                )
            )
        }
    }

    @Test
    fun isValidMatterDetectOnlyAirCandidateTest() {
        assertThrows<IllegalStateException> {
            GroupRecipe.Matter.of(
                matter = CMatterImpl(
                    name = "",
                    candidate = emptySet()
                ),
                includeAir = true
            )
        }
    }

    @Test
    fun passOriginalMatterPredicateTest() {
        val efficiency = GroupRecipe.Matter.of(
            matter = CEnchantmentStoreMatterImpl(
                name = "",
                candidate = setOf(Material.ENCHANTED_BOOK),
                storedEnchantComponents = setOf(
                    CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.STRICT)
                )
            ),
            includeAir = false
        )

        val recipe: CRecipe = GroupRecipe(
            name = "",
            items = mapOf(CoordinateComponent(0, 0) to efficiency),
            groups = setOf(GroupRecipe.Context(
                members = setOf(CoordinateComponent(0, 0)),
                min = 1
            ))
        )

        val lv4Efficiency: ItemStack = ItemStack.of(Material.ENCHANTED_BOOK)
        lv4Efficiency.editMeta { meta ->
            (meta as EnchantmentStorageMeta).addStoredEnchant(Enchantment.EFFICIENCY, 4, true)
        }

        val view = CraftView(
            materials = mapOf(CoordinateComponent(0, 0) to lv4Efficiency),
            result = ItemStack.empty()
        )

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view,
            sourceRecipes = listOf(recipe)
        )

        // required: Lv.5 (Strict), input Lv.4 -> false
        assertEquals(0, result.size())
    }

    @Test
    fun passAirContainableMatterOriginalPredicate() {
        val stone = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.STONE),
            includeAir = false
        )

        val efficiency = GroupRecipe.Matter.of(
            matter = CEnchantmentStoreMatterImpl(
                name = "",
                candidate = setOf(Material.ENCHANTED_BOOK),
                storedEnchantComponents = setOf(
                    CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.STRICT)
                )
            ),
            includeAir = true
        )

        val recipe: CRecipe = GroupRecipe(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to stone,
                CoordinateComponent(1, 0) to efficiency
            ),
            groups = setOf(
                GroupRecipe.Context(
                    members = setOf(CoordinateComponent(0, 0)),
                    min = 1
                ),
                GroupRecipe.Context(
                    members = setOf(CoordinateComponent(1, 0)),
                    min = 0
                )
            )
        )

        val view = CraftView(
            materials = mapOf(CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)),
            result = ItemStack.empty()
        )

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = view,
            sourceRecipes = listOf(recipe)
        )

        // required: Lv.5 (Strict), input Lv.4 -> false
        assertEquals(1, result.size())
    }
}
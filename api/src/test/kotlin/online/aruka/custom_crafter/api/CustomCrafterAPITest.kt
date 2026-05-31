package online.aruka.custom_crafter.api

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.util.Converter.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal object CustomCrafterAPITest {

    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        server.addWorld(WorldMock())
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun baseBlockSizeTest() {
        assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(-1))
        assertTrue(!CustomCrafterAPI.setBaseBlockSideSize(0))
        assertTrue(CustomCrafterAPI.setBaseBlockSideSize(5))

        CustomCrafterAPI.setBaseBlockSideSize(5)
        assertTrue(CustomCrafterAPI.getBaseBlockSideSize() == 5)

        CustomCrafterAPI.setBaseBlockSideSizeDefault()
        assertTrue(CustomCrafterAPI.getBaseBlockSideSize() == 3)
    }

    @Test
    fun baseBlockTypeTest() {
        assertThrows<IllegalArgumentException> {
            CustomCrafterAPI.setBaseBlock(Material.AIR)
        }

        assertThrows<IllegalArgumentException> {
            CustomCrafterAPI.setBaseBlock(Material.GOLD_INGOT)
        }

        CustomCrafterAPI.setBaseBlock(Material.DIAMOND_BLOCK)
        assertTrue(CustomCrafterAPI.getBaseBlock() == Material.DIAMOND_BLOCK)

        CustomCrafterAPI.setBaseBlockDefault()
        assertTrue(CustomCrafterAPI.getBaseBlock() == Material.GOLD_BLOCK)
    }

    @Test
    fun craftUIDesignerTest() {
        val anonymous = object: CraftUIDesigner {
            override fun blankSlots(context: CraftUIDesigner.Context): Map<CoordinateComponent, ItemStack> {
                val blank = ItemStack.of(Material.STONE)
                return (0..<54).filter { it % 9 > 5 }
                    .map { CoordinateComponent.fromIndex(it) }
                    .associateWith { blank }
            }

            override fun makeButton(context: CraftUIDesigner.Context): Pair<CoordinateComponent, ItemStack> {
                return CoordinateComponent.fromIndex(35) to ItemStack.of(Material.STONE)
            }

            override fun title(context: CraftUIDesigner.Context): Component {
                return "".toComponent()
            }
        }

        CustomCrafterAPI.setCraftUIDesigner(anonymous)

        val nullContext = CraftUIDesigner.Context()
        assertTrue((0..<54).filter { it % 9 > 5 }.map { CoordinateComponent.fromIndex(it) }
            .containsAll(CustomCrafterAPI.getCraftUIDesigner().blankSlots(nullContext).keys)
        )

        CustomCrafterAPI.setCraftUIDesignerDefault()
    }

    @Test
    fun nameStrictLevelMatchTest() {
        assertFalse(CustomCrafterAPI.NameStrictLevel.NOTHING.matches("abc", "abc"))
        assertFalse(CustomCrafterAPI.NameStrictLevel.NOTHING.matches("abc", "a b c"))

        assertTrue(CustomCrafterAPI.NameStrictLevel.WEAK.matches("abc", "abc"))
        assertFalse(CustomCrafterAPI.NameStrictLevel.WEAK.matches("abc", "a b c"))

        assertTrue(CustomCrafterAPI.NameStrictLevel.STRICT.matches("abc", "abc"))
        assertTrue(CustomCrafterAPI.NameStrictLevel.STRICT.matches("abc", "a b c"))
    }

    @Test
    fun nameStrictLevelContainsTest() {
        assertFalse(
            CustomCrafterAPI.NameStrictLevel.contains(
                CustomCrafterAPI.NameStrictLevel.NOTHING,
                targets = setOf("abc"),
                sources = setOf("a b c")
            )
        )

        assertFalse(
            CustomCrafterAPI.NameStrictLevel.contains(
                CustomCrafterAPI.NameStrictLevel.WEAK,
                targets = setOf("abc"),
                sources = setOf("a b c")
            )
        )
    }

    @Test
    fun nameStrictLevelHasDuplicateTest() {
        val list1 = listOf("abc", "a b c")
        val list2 = listOf("abc", "abc")
        val list3 = listOf("abc", "ABC")

        assertFalse(CustomCrafterAPI.NameStrictLevel.NOTHING.hasDuplicate(list1))
        assertFalse(CustomCrafterAPI.NameStrictLevel.NOTHING.hasDuplicate(list2))
        assertFalse(CustomCrafterAPI.NameStrictLevel.NOTHING.hasDuplicate(list3))

        assertFalse(CustomCrafterAPI.NameStrictLevel.WEAK.hasDuplicate(list1))
        assertTrue(CustomCrafterAPI.NameStrictLevel.WEAK.hasDuplicate(list2))
        assertFalse(CustomCrafterAPI.NameStrictLevel.WEAK.hasDuplicate(list3))

        assertTrue(CustomCrafterAPI.NameStrictLevel.STRICT.hasDuplicate(list1))
        assertTrue(CustomCrafterAPI.NameStrictLevel.STRICT.hasDuplicate(list2))
        assertFalse(CustomCrafterAPI.NameStrictLevel.STRICT.hasDuplicate(list3))
    }

    private fun registerFiftyRecipes() {
        val items: Map<CoordinateComponent, CMatter> = mapOf(
            CoordinateComponent(0, 0) to CMatterImpl.of(Material.STONE)
        )
        val recipes: List<CRecipe> = (0..<50).map { t -> CRecipeImpl(t.toString(), items, CRecipe.Type.SHAPED) }
        CustomCrafterAPI.registerRecipe(recipes, plugin)
    }

    @Test
    fun unregisterAllRecipesTest() {
        registerFiftyRecipes()
        CustomCrafterAPI.unregisterAllRecipes()
        assertTrue(CustomCrafterAPI.getRecipes().isEmpty())
    }

    @Test
    fun getRegisteredRecipeNamesTest() {
        registerFiftyRecipes()

        assertEquals(50, CustomCrafterAPI.getRegisteredRecipeNames().size)
        assertTrue(CustomCrafterAPI.getRegisteredRecipeNames().containsAll((0..<50).map { it.toString() }.toSet()))

        CustomCrafterAPI.unregisterAllRecipes()
    }

    @Test
    fun getRegisteredRecipeFromNameTest() {
        registerFiftyRecipes()

        assertEquals(1, CustomCrafterAPI.getRegisteredRecipeFromName("0").size)
        assertEquals("0", CustomCrafterAPI.getRegisteredRecipeFromName("0").first().name)

        CustomCrafterAPI.unregisterAllRecipes()
    }

    @Test
    fun getRegisteredRecipeFromPluginTest() {
        registerFiftyRecipes()

        assertEquals(50, CustomCrafterAPI.getRegisteredRecipeFromPlugin(plugin).size)

        CustomCrafterAPI.unregisterAllRecipes()
    }

    @Test
    fun unregisterRecipeTest() {
        registerFiftyRecipes()

        CustomCrafterAPI.unregisterRecipe(name = "-", plugin = plugin)
        assertEquals(50, CustomCrafterAPI.getRecipes().size)

        CustomCrafterAPI.unregisterRecipe(name = "0", plugin = plugin)
        assertEquals(49, CustomCrafterAPI.getRecipes().size)

        CustomCrafterAPI.unregisterRecipe(plugin = plugin)
        assertTrue(CustomCrafterAPI.getRecipes().isEmpty())

        CustomCrafterAPI.unregisterAllRecipes()
    }

    // MockBukkit error
//    @Test
//    fun allCandidateTest() {
//        assertThrows<IllegalArgumentException> {
//            CustomCrafterAPI.setAllCandidateNotDisplayableItem(ItemStackMock(Material.WATER)) { _ -> null }
//        }
//    }
}
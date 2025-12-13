package online.aruka.customcrafter.api

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
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
import org.mockbukkit.mockbukkit.world.WorldMock
import java.lang.IllegalArgumentException

internal object CustomCrafterAPITest {

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

            override fun resultSlot(context: CraftUIDesigner.Context): CoordinateComponent {
                return CoordinateComponent.fromIndex(44)
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

    // MockBukkit error
//    @Test
//    fun allCandidateTest() {
//        assertThrows<IllegalArgumentException> {
//            CustomCrafterAPI.setAllCandidateNotDisplayableItem(ItemStackMock(Material.WATER)) { _ -> null }
//        }
//    }
}
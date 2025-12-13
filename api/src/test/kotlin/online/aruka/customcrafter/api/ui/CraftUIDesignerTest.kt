package online.aruka.customcrafter.api.ui

import io.github.sakaki_aruka.customcrafter.api.interfaces.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object CraftUIDesignerTest {

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
    fun bakedDesignerCraftSlotsTest() {
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

        assertTrue(
            (0..<54).filter { it % 9 < 6 }.map { CoordinateComponent.fromIndex(it) }.toSet()
                .containsAll(CraftUIDesigner.bake(anonymous, CraftUIDesigner.Context()).craftSlots())
        )
    }

    @Test
    fun bakedDesignerResultIntTest() {
        val baked = CraftUIDesigner.BakedDesigner(
            title = "".toComponent(),
            result = CoordinateComponent.fromIndex(44),
            makeButton = CoordinateComponent.fromIndex(0) to ItemStack.empty(),
            blankSlots = emptyMap()
        )

        assertEquals(44, baked.resultInt())
    }

    @Test
    fun bakedDesignerIsValidDetectSizeNot36Test() {
        val baked = CraftUIDesigner.BakedDesigner(
            title = "".toComponent(),
            result = CoordinateComponent.fromIndex(44),
            makeButton = CoordinateComponent.fromIndex(35) to ItemStack.empty(),
            blankSlots = emptyMap()
        )

        assertTrue(baked.isValid().isFailure)
    }

    @Test
    fun bakedDesignerIsValidDetectTopYNot0Test() {
        val baked = CraftUIDesigner.BakedDesigner(
            title = "".toComponent(),
            result = CoordinateComponent.fromIndex(0),
            makeButton = CoordinateComponent.fromIndex(1) to ItemStack.empty(),
            blankSlots = (0..<54).filter { it / 9 > 1 }.map { CoordinateComponent.fromIndex(it) }
                .associateWith { ItemStack.of(Material.STONE) }
        )

        assertTrue(baked.isValid().isFailure)
    }

    @Test
    fun bakedDesignerIsValidDetectTopXBiggerThan3Test() {
        //
    }

    @Test
    fun bakedDesignerIsValidDetectIsAirTest() {
        val baked = CraftUIDesigner.BakedDesigner(
            title = "".toComponent(),
            result = CoordinateComponent.fromIndex(44),
            makeButton = CoordinateComponent.fromIndex(35) to ItemStack.empty(),
            blankSlots = (0..<54).filter { it % 9 >= 6 }.map { CoordinateComponent.fromIndex(it) }
                .minus(CoordinateComponent.fromIndex(35))
                .minus(CoordinateComponent.fromIndex(44))
                .associateWith { ItemStack.empty() }
        )

        assertTrue(baked.isValid().isFailure)
    }

    @Test
    fun bakedDesignerIsValidSuccessTest() {
        val baked = CraftUIDesigner.BakedDesigner(
            title = "".toComponent(),
            result = CoordinateComponent.fromIndex(44),
            makeButton = CoordinateComponent.fromIndex(35) to ItemStack.empty(),
            blankSlots = (0..<54).filter { it % 9 >= 6 }.map { CoordinateComponent.fromIndex(it) }
                .minus(CoordinateComponent.fromIndex(35))
                .minus(CoordinateComponent.fromIndex(44))
                .associateWith { ItemStack.of(Material.STONE) }
        )

        assertTrue(baked.isValid().isSuccess)
    }
}
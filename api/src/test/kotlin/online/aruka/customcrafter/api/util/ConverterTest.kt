package online.aruka.customcrafter.api.util

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal object ConverterTest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun availableSlotComponentsTest() {
        val list: List<CoordinateComponent> = Converter.getDefaultCraftingSlots()
        val components: List<CoordinateComponent> = listOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(1, 0),
            CoordinateComponent(2, 0),
            CoordinateComponent(3, 0),
            CoordinateComponent(4, 0),
            CoordinateComponent(5, 0),
            CoordinateComponent(0, 1),
            CoordinateComponent(1, 1),
            CoordinateComponent(2, 1),
            CoordinateComponent(3, 1),
            CoordinateComponent(4, 1),
            CoordinateComponent(5, 1),
            CoordinateComponent(0, 2),
            CoordinateComponent(1, 2),
            CoordinateComponent(2, 2),
            CoordinateComponent(3, 2),
            CoordinateComponent(4, 2),
            CoordinateComponent(5, 2),
            CoordinateComponent(0, 3),
            CoordinateComponent(1, 3),
            CoordinateComponent(2, 3),
            CoordinateComponent(3, 3),
            CoordinateComponent(4, 3),
            CoordinateComponent(5, 3),
            CoordinateComponent(0, 4),
            CoordinateComponent(1, 4),
            CoordinateComponent(2, 4),
            CoordinateComponent(3, 4),
            CoordinateComponent(4, 4),
            CoordinateComponent(5, 4),
            CoordinateComponent(0, 5),
            CoordinateComponent(1, 5),
            CoordinateComponent(2, 5),
            CoordinateComponent(3, 5),
            CoordinateComponent(4, 5),
            CoordinateComponent(5, 5)
        )

        assertEquals(components, list)


    }

    @Test
    fun availableCraftingSlotIndicesTest() {
        val intList: Set<Int> = (0..<54).filter { i -> i % 9 < 6 }.take(36).toSet()
        val slots: Set<Int> = Converter.getDefaultCraftingSlotsInt()
        assertEquals(intList.size, slots.size)
        assertTrue(slots.containsAll(intList))

        val componentList = Converter.getDefaultCraftingSlots()
        val mappedIndices = componentList.map { c -> c.x + c.y * 9 }
        assertEquals(mappedIndices.size, slots.size)
        assertTrue(slots.containsAll(mappedIndices))
    }

    @Test
    fun inputMappingTest() {
        val emptyInventory = Bukkit.createInventory(null, 54)
        assertEquals(null, Converter.standardInputMapping(emptyInventory))

        val stones: Inventory = CraftUI().inventory
        Converter.getDefaultCraftingSlotsInt().forEach { index ->
            stones.setItem(index, ItemStack(Material.STONE))
        }

        val mapping = Converter.standardInputMapping(stones)
        assertTrue(mapping != null)
        assertEquals(36, mapping.size)
        assertEquals(1, mapping.values.toSet().size)
        assertTrue(mapping.values.first().isSimilar(ItemStack(Material.STONE)))

        val components: Set<CoordinateComponent> = setOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(1, 0),
            CoordinateComponent(2, 0),
            CoordinateComponent(3, 0),
            CoordinateComponent(4, 0),
            CoordinateComponent(5, 0),
            CoordinateComponent(0, 1),
            CoordinateComponent(1, 1),
            CoordinateComponent(2, 1),
            CoordinateComponent(3, 1),
            CoordinateComponent(4, 1),
            CoordinateComponent(5, 1),
            CoordinateComponent(0, 2),
            CoordinateComponent(1, 2),
            CoordinateComponent(2, 2),
            CoordinateComponent(3, 2),
            CoordinateComponent(4, 2),
            CoordinateComponent(5, 2),
            CoordinateComponent(0, 3),
            CoordinateComponent(1, 3),
            CoordinateComponent(2, 3),
            CoordinateComponent(3, 3),
            CoordinateComponent(4, 3),
            CoordinateComponent(5, 3),
            CoordinateComponent(0, 4),
            CoordinateComponent(1, 4),
            CoordinateComponent(2, 4),
            CoordinateComponent(3, 4),
            CoordinateComponent(4, 4),
            CoordinateComponent(5, 4),
            CoordinateComponent(0, 5),
            CoordinateComponent(1, 5),
            CoordinateComponent(2, 5),
            CoordinateComponent(3, 5),
            CoordinateComponent(4, 5),
            CoordinateComponent(5, 5),
        )

        assertEquals(components, mapping.keys)
    }

    @Test
    fun shapeStringTest() {
        val components = CoordinateComponent.squareFill(2)
        assertEquals("""
            ##
            ##
        """.trimIndent(), Converter.getComponentsShapeString(components))
    }
}
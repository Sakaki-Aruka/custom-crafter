package online.aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object CoordinateComponentTest {

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
    fun toIndexTest() {
        assertEquals(0, CoordinateComponent(0 , 0).toIndex())
        assertEquals(1, CoordinateComponent(1, 0).toIndex())
        assertEquals(9, CoordinateComponent(0, 1).toIndex())
        assertEquals(10, CoordinateComponent(1, 1).toIndex())
    }

    @Test
    fun fromIndexTest() {
        assertEquals(CoordinateComponent(1, 0), CoordinateComponent.fromIndex(1))
        assertEquals(CoordinateComponent(0, 1), CoordinateComponent.fromIndex(9))
        assertEquals(CoordinateComponent(8, 5), CoordinateComponent.fromIndex(53))
    }

    @Test
    fun squareFillTest() {
        assertTrue(CoordinateComponent.squareFill(0).isEmpty())

        assertEquals(
            CoordinateComponent(0, 0),
            CoordinateComponent.squareFill(1).first()
        )

        assertEquals(4, CoordinateComponent.squareFill(2).size)
        assertEquals(
            setOf(0, 1, 9, 10),
            CoordinateComponent.squareFill(size = 2, dx = 0, dy = 0).map { it.toIndex() }.toSet()
        )

        assertEquals(9, CoordinateComponent.squareFill(3).size)
        assertEquals(
            setOf(0, 1, 2, 9, 10, 11, 18, 19, 20),
            CoordinateComponent.squareFill(size = 3, dx = 0, dy = 0).map { it.toIndex() }.toSet()
        )

        assertEquals(
            setOf(1, 2, 3, 10, 11, 12, 19, 20, 21),
            CoordinateComponent.squareFill(size = 3, dx = 1, dy = 0).map { it.toIndex() }.toSet()
        )

        assertEquals(
            setOf(9, 10, 11, 18, 19, 20, 27, 28, 29),
            CoordinateComponent.squareFill(3, dx = 0, dy = 1).map { it.toIndex() }.toSet()
        )

        assertEquals(
            setOf(10, 11, 12, 19, 20, 21, 28, 29, 30),
            CoordinateComponent.squareFill(size = 3, dx = 1, dy = 1).map { it.toIndex() }.toSet()
        )

        // safe trim
        assertEquals(
            setOf(0, 9),
            CoordinateComponent.squareFill(size = 2, dx = -1, dy = 0).map { it.toIndex() }.toSet()
        )

        assertThrows<IllegalArgumentException> {
            CoordinateComponent.squareFill(-1)
        }
    }

    @Test
    fun squareTest() {
        assertTrue(CoordinateComponent.square(0).isEmpty())

        /*
         * Expected
         * #
         */
        assertEquals(
            setOf(CoordinateComponent(0, 0)),
            CoordinateComponent.square(1)
        )

        /*
         * Expected
         * ##
         * ##
         */
        assertEquals(
            setOf(0, 1, 9, 10),
            CoordinateComponent.square(2).map { it.toIndex() }.toSet()
        )

        /*
         * Expected
         * ###
         * # #
         * ###
         */
        assertEquals(
            setOf(0, 1, 2, 9, 11, 18, 19, 20),
            CoordinateComponent.square(3).map { it.toIndex() }.toSet()
        )

        assertEquals(
            setOf(1, 2, 3, 10, 12, 19, 20, 21),
            CoordinateComponent.square(size = 3, dx = 1).map { it.toIndex() }.toSet()
        )

        assertEquals(
            setOf(9, 10, 11, 18, 20, 27, 28, 29),
            CoordinateComponent.square(size = 3, dy = 1).map { it.toIndex() }.toSet()
        )

        assertEquals(
            setOf(10, 11, 12, 19, 21, 28, 29, 30),
            CoordinateComponent.square(size = 3, dx = 1, dy = 1).map { it.toIndex() }.toSet()
        )

        assertThrows<IllegalArgumentException> {
            CoordinateComponent.square(-1)
        }
    }

    @Test
    fun getNTest() {
        assertThrows<IllegalArgumentException> {
            CoordinateComponent.getN(0)
        }

        assertEquals(
            CoordinateComponent(0, 0),
            CoordinateComponent.getN(1).firstOrNull()
        )
    }

    @Test
    fun recipeMapFromStringListDetectEmptyStringListTest() {
        val lines: List<String> = emptyList()
        val map: Map<String, CMatter> = mapOf("a" to CMatterImpl.of(Material.APPLE))
        assertTrue(CoordinateComponent.recipeMapFromStringList(lines, map).isEmpty())
    }

    @Test
    fun recipeMapFromStringListDetectEmptyMapTest() {
        val lines: List<String> = listOf("g,g,g", "g,a,g", "g,g,g")
        val map: Map<String, CMatter> = emptyMap()
        assertTrue(CoordinateComponent.recipeMapFromStringList(lines, map).isEmpty())
    }

    @Test
    fun recipeMapFromStringListDetectOverSizeListTest() {
        val lines: List<String> = listOf("a", "a", "a", "a", "a", "a", "a")
        val map: Map<String, CMatter> = mapOf("a" to CMatterImpl.of(Material.APPLE))
        assertThrows<IllegalArgumentException> {
            CoordinateComponent.recipeMapFromStringList(lines, map)
        }
    }

    @Test
    fun recipeMapFromStringListDetectOverSizeLineTest() {
        val lines: List<String> = listOf("a,a,a,a,a,a,a")
        val map: Map<String, CMatter> = mapOf("a" to CMatterImpl.of(Material.APPLE))
        assertThrows<IllegalArgumentException> {
            CoordinateComponent.recipeMapFromStringList(lines, map)
        }
    }

    @Test
    fun recipeMapFromStringListTest() {
        val lines: List<String> = listOf(
            "g,g,g",
            "g,a,g",
            "g,g,g"
        )
        val map: Map<String, CMatter> = mapOf(
            "a" to CMatterImpl.of(Material.APPLE),
            "g" to CMatterImpl.of(Material.GOLD_BLOCK)
        )

        val result = CoordinateComponent.recipeMapFromStringList(lines, map)

        CoordinateComponent.square(3).forEach { c ->
            assertEquals(Material.GOLD_BLOCK, result.getValue(c).candidate.first())
        }

        assertEquals(Material.APPLE, result.getValue(CoordinateComponent(1, 1)).candidate.first())
    }

    @Test
    fun recipeMapFromStringListTest2() {
        val lines: List<String> = listOf(
            "g,g,g",
            "g,,g",
            "g,g,g"
        )
        val map: Map<String, CMatter> = mapOf("g" to CMatterImpl.of(Material.GOLD_BLOCK))
        val result = CoordinateComponent.recipeMapFromStringList(lines, map)
        assertEquals(8, result.size)
        CoordinateComponent.square(3).forEach { c ->
            assertTrue(c in result.keys)
        }
    }

    @Test
    fun mapToRecipeMapDetectEmptyTest() {
        val source: Map<CMatter, Set<CoordinateComponent>> = emptyMap()
        assertTrue(CoordinateComponent.mapToRecipeMap(source).isEmpty())
    }

    @Test
    fun mapToRecipeMapDetectOverLimitCoordinateTest() {
        val source: Map<CMatter, Set<CoordinateComponent>> = mapOf(
            CMatterImpl.of(Material.APPLE) to setOf(CoordinateComponent(7, 0))
        )

        assertThrows<IllegalArgumentException> {
            CoordinateComponent.mapToRecipeMap(source)
        }
    }

    @Test
    fun mapToRecipeMapTest() {
        val source: Map<CMatter, Set<CoordinateComponent>> = mapOf(
            CMatterImpl.of(Material.APPLE) to setOf(
                CoordinateComponent(0, 0),
                CoordinateComponent(1, 0),
                CoordinateComponent(0, 1),
                CoordinateComponent(1, 1)
            )
        )

        val result = CoordinateComponent.mapToRecipeMap(source)

        assertEquals(4, result.size)
        CoordinateComponent.square(2).forEach { c ->
            assertTrue(c in result.keys)
        }
    }
}
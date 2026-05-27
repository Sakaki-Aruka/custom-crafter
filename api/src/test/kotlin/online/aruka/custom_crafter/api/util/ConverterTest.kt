package online.aruka.custom_crafter.api.util

import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.util.Converter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import kotlin.test.assertEquals

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
    fun shapeStringTest() {
        val components = CoordinateComponent.squareFill(2)
        assertEquals(
            "##" + System.lineSeparator() + "##",
            Converter.getComponentsShapeString(components))
    }
}
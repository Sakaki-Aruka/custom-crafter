package byteArrayTest

import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toByteArray
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object CoordinateComponentTest {
    @Test
    fun toByteArrayTest() {
        val c1 = CoordinateComponent(0, 0)
        val array1 = c1.toByteArray()
        val model1 = 0.toByteArray() + 0.toByteArray()

        Assertions.assertTrue(array1.size == model1.size)
        model1.zip(array1).forEach { (model, byte) ->
            Assertions.assertTrue(model == byte)
        }

        val c2 = CoordinateComponent(5, 2)
        val array2 = c2.toByteArray()
        val model2 = 5.toByteArray() + 2.toByteArray()

        Assertions.assertTrue(array2.size == model2.size)
        model2.zip(array2).forEach { (model, byte) ->
            Assertions.assertTrue(model == byte)
        }

        val c3 = CoordinateComponent(1234567890, 1234567890)
        val array3 = c3.toByteArray()
        val model3 = 1234567890.toByteArray() + 1234567890.toByteArray()

        Assertions.assertTrue(model3.size == array3.size)
        model3.zip(array3).forEach { (model, byte) ->
            Assertions.assertTrue(model == byte)
        }

        val c4 = CoordinateComponent(987654321, 987654321)
        val array4 = c4.toByteArray()
        val model4 = 1234567890.toByteArray() + 1234567890.toByteArray()
        Assertions.assertTrue(model4.size == array4.size)
        Assertions.assertFalse(model4.withIndex().all { (index, byte) -> array4[index] == byte })
    }

    @Test
    fun fromByteArray() {
        val model1 = CoordinateComponent(0, 0)
        val array1 = 0.toByteArray() + 0.toByteArray()
        val c1 = CoordinateComponent.fromByteArray(array1)
        Assertions.assertTrue(model1 == c1)

        val model2 = CoordinateComponent(5, 2)
        val array2 = 5.toByteArray() + 2.toByteArray()
        val c2 = CoordinateComponent.fromByteArray(array2)
        Assertions.assertTrue(model2 == c2)

        val model3 = CoordinateComponent(1234567890, 1234567890)
        val array3 = 1234567890.toByteArray() + 1234567890.toByteArray()
        val c3 = CoordinateComponent.fromByteArray(array3)
        Assertions.assertTrue(model3 == c3)

        val model4 = CoordinateComponent(987654321, 987654321)
        val array4 = 1234567890.toByteArray() + 1234567890.toByteArray()
        val c4 = CoordinateComponent.fromByteArray(array4)
        Assertions.assertFalse(model4 == c4)

        val array5 = array4 + array4
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            CoordinateComponent.fromByteArray(array5)
        }
    }
}
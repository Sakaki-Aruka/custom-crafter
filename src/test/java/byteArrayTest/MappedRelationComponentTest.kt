package byteArrayTest

import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toByteArray
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object MappedRelationComponentTest {
    @Test
    fun toByteArrayTest() {
        val c1 = CoordinateComponent(0, 0)
        val m1 = MappedRelationComponent(c1, c1)
        val array1 = m1.toByteArray()
        val zero = 0.toByteArray()
        val model1 = zero + zero + zero + zero

        Assertions.assertTrue(model1.size == array1.size)
        model1.zip(array1).forEach { (model, byte) ->
            Assertions.assertTrue(model == byte)
        }

        val c2 = CoordinateComponent(5, 2)
        val c3 = CoordinateComponent(1000, 315)
        val m2 = MappedRelationComponent(c2, c3)
        val array2 = m2.toByteArray()
        val model2 = 5.toByteArray() + 2.toByteArray() + 1000.toByteArray() + 315.toByteArray()

        Assertions.assertTrue(model2.size == array2.size)
        model2.zip(array2).forEach { (model, byte) ->
            Assertions.assertTrue(model == byte)
        }

        val c4 = CoordinateComponent(Int.MAX_VALUE, Int.MIN_VALUE)
        val c5 = CoordinateComponent(Int.MIN_VALUE, Int.MAX_VALUE)
        val m3 = MappedRelationComponent(c4, c5)
        val array3 = m3.toByteArray()
        val max = Int.MAX_VALUE.toByteArray()
        val min = Int.MIN_VALUE.toByteArray()
        val model3 = max + min + min + max
        Assertions.assertTrue(model3.size == array3.size)
        model3.zip(array3).forEach { (model, byte) ->
            Assertions.assertTrue(model == byte)
        }
    }

    @Test
    fun fromByteArrayTest() {
        val c1 = CoordinateComponent(100, 128)
        val c2 = CoordinateComponent(256, 512)
        val model1 = MappedRelationComponent(c1, c2)
        val array1 = 100.toByteArray() + 128.toByteArray() + 256.toByteArray() + 512.toByteArray()
        val component1 = MappedRelationComponent.fromByteArray(array1)
        Assertions.assertTrue(model1 == component1)

        val array2 = array1 + array1
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            MappedRelationComponent.fromByteArray(array2)
        }

        val array3 = 128.toByteArray() + 100.toByteArray() + 256.toByteArray() + 512.toByteArray()
        val component3 = MappedRelationComponent.fromByteArray(array3)
        Assertions.assertFalse(model1 == component3)
    }
}
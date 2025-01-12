package byteArrayTest

import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toByteArray
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object MappedRelationTest {
    @Test
    fun toByteArrayTest() {
        val component1 = MappedRelationComponent(
            recipe = CoordinateComponent(1234567890, 987654321),
            input = CoordinateComponent(1234567890, 987654321)
        )
        val component2 = MappedRelationComponent(CoordinateComponent(1, 2), CoordinateComponent(3, 4))
        val component3 = MappedRelationComponent(CoordinateComponent(5, 6), CoordinateComponent(7, 8))

        val relation1 = MappedRelation(setOf(component1, component2, component3))
        val array1 = relation1.toByteArray()
        Assertions.assertTrue(relation1.byteArrayLength() == array1.size)

        val component1Bytes = component1.toByteArray()
        val component2Bytes = component2.toByteArray()
        val component3Bytes = component3.toByteArray()
        Assertions.assertTrue(array1.sliceArray(component1Bytes.indices).contentEquals(component1Bytes))
        Assertions.assertTrue(array1.sliceArray(component1Bytes.size until (component1Bytes.size + component2Bytes.size)).contentEquals(component2Bytes))
        Assertions.assertTrue(array1.sliceArray((component1Bytes.size + component2Bytes.size) until array1.size).contentEquals(component3Bytes))
    }

    @Test
    fun fromByteArrayTest() {
        val array1 = Int.MAX_VALUE.toByteArray() + 456.toByteArray() + 789.toByteArray() + 123.toByteArray()
        val model1 = MappedRelation(setOf(
            MappedRelationComponent(
                CoordinateComponent(Int.MAX_VALUE, 456),
                CoordinateComponent(789, 123)
            )
        ))

        val relation1 = MappedRelation.fromByteArray(array1)
        Assertions.assertTrue(model1 == relation1)

        val array2 = array1 + 128.toByteArray()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            MappedRelation.fromByteArray(array2)
        }
    }
}
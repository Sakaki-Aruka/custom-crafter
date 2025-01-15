package byteArrayTest

import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toByteArray
import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toInt
import io.github.sakaki_aruka.customcrafter.api.search.Search
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object SearchResultTest {
    @Test
    fun emptyResultToByteArrayTest() {
        val result: Search.SearchResult = Search.SearchResult(null, emptyList())
        val resultByteArray: ByteArray = result.toByteArray()

        Assertions.assertTrue(resultByteArray.size == 20)
        Assertions.assertTrue(resultByteArray
            .sliceArray(0..3)
            .toInt() == 1)

        Assertions.assertTrue(resultByteArray
            .sliceArray(4..7)
            .contentEquals(0.toByteArray()))

        Assertions.assertTrue(resultByteArray
            .sliceArray(8..11)
            .contentEquals(0.toByteArray()))

        // vanilla recipe ItemStack's ByteArray (=0, in this case)
        Assertions.assertTrue(resultByteArray
            .sliceArray(12..15)
            .contentEquals(0.toByteArray()))

        // custom recipes info size (= 0 = 0.toByteArray(), in this case)
        Assertions.assertTrue(resultByteArray
            .sliceArray(16..19)
            .contentEquals(0.toByteArray()))
    }

    @Test
    fun emptyResultFromByteArrayTest() {
        val array = ByteArray(20) { i ->
            if (i != 3) 0.toByte()
            else 1.toByte()
        }
        val result: Search.SearchResult = Search.SearchResult.fromByteArray(array)
        val model: Search.SearchResult = Search.SearchResult(null, emptyList())

        Assertions.assertTrue(model.vanilla() == result.vanilla())
        Assertions.assertTrue(model.customs().size == result.customs().size)
    }
}
package io.github.sakaki_aruka.customcrafter.api.`object`

/**
 * A relation holder.
 *
 * @param[components] Relation components. ->[MappedRelationComponent]
 */

data class MappedRelation internal constructor(
    val components: Set<MappedRelationComponent>
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(components.size * 16)
        components.withIndex().forEach { (index, c) ->
            val componentByteArray: ByteArray = c.toByteArray()
            val indexBase: Int = index * 16
            componentByteArray.withIndex().forEach { (offset, byte) ->
                array[indexBase + offset] = byte
            }
        }
        return array
    }

    fun byteArrayLength(): Int {
        return components.size * 16
    }

    companion object {
        fun fromByteArray(
            array: ByteArray
        ): MappedRelation {
            if (array.size % 16 != 0) {
                throw IllegalArgumentException("The size of 'array' must be 0 when divided by 16.")
            }
            val components: MutableSet<MappedRelationComponent> = mutableSetOf()
            (0..(array.size / 16) step 16 ).forEach { point ->
                val start: Int = point * 16
                val end: Int = (point + 1) * 16 // until
                val c: ByteArray = array.sliceArray(start..<end)
                components.add(MappedRelationComponent.fromByteArray(c))
            }
            return MappedRelation(components)
        }
    }

}

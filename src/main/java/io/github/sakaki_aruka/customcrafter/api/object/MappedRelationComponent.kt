package io.github.sakaki_aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toByteArray

/**
 * A component of a relation.
 *
 * @param[recipe] A recipe items coordinate.
 * @param[input] An input items coordinate.
 */

data class MappedRelationComponent internal constructor(
    val recipe: CoordinateComponent,
    val input: CoordinateComponent
) {
    fun toByteArray(): ByteArray {
        /*
         * CoordinateComponent = x: 4Byte, y:4Byte
         * recipe: 8 Byte
         * input: 8 Byte
         *
         * total: 16 Byte
         *
         * payload
         * [0-3] = recipe.x Byte
         * [4-7] = recipe.y Byte
         * [8-11] = input.x Byte
         * [12-15] = input.y Byte
         */
        return recipe.toByteArray() + input.toByteArray()
    }

    companion object {
        fun fromByteArray(
            array: ByteArray
        ): MappedRelationComponent {
            if (array.size != 16) {
                throw IllegalArgumentException("The size of 'array' must be 16.")
            }
            val recipe: CoordinateComponent = CoordinateComponent.fromByteArray(
                array.sliceArray(0..<8)
            )

            val input: CoordinateComponent = CoordinateComponent.fromByteArray(
                array.sliceArray(8..<16)
            )

            return MappedRelationComponent(recipe, input)
        }
    }
}

package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent

/**
 * @suppress
 */
internal data class AmorphousFilterCandidate(
    val coordinate: CoordinateComponent,
    val list: List<CoordinateComponent>
) {
    internal enum class Type {
        NOT_ENOUGH,
        NOT_REQUIRED,
        SUCCESSFUL
    }
}

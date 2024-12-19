package io.github.sakaki_aruka.customcrafter.api.`object`.internal

import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent

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

package io.github.sakaki_aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.AmorphousFilter

/**
 * a result of candidate checks.
 *
 * @param[coordinate] a coordinate of a CRecipe.
 * @param[list] matched itemStack's inventory coordinate with [AmorphousFilter]
 */
data class AmorphousFilterCandidate(
    val coordinate: CoordinateComponent,
    val list: List<CoordinateComponent>
) {
    /**
     * result types of [AmorphousFilter]
     *
     * NOT_ENOUGH: an input is missing candidate
     *
     * NOT_REQUIRED: a recipe does not require those components
     */
    enum class Type {
        NOT_ENOUGH,
        NOT_REQUIRED,
        SUCCESSFUL
    }
}

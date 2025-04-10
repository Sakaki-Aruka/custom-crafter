package io.github.sakaki_aruka.customcrafter.api.objects

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
/**
 * a result of candidate checks.
 *
 * @param[coordinate] a coordinate of a CRecipe.
 * @param[list] matched itemStack's inventory coordinate with [CRecipeFilter]
 */
data class AmorphousFilterCandidate(
    val coordinate: CoordinateComponent,
    val list: List<CoordinateComponent>
) {
    /**
     * result types of [CRecipeFilter.amorphous]
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

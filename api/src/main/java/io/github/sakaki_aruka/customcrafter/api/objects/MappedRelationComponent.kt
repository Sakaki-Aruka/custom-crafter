package io.github.sakaki_aruka.customcrafter.api.objects

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent

/**
 * A component of a relation.
 *
 * @param[recipe] A recipe items coordinate.
 * @param[input] An input items coordinate.
 */

data class MappedRelationComponent (
    val recipe: CoordinateComponent,
    val input: CoordinateComponent
)

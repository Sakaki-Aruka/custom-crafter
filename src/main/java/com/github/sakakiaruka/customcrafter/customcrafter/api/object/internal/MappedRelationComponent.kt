package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent

/**
 * A component of a relation.
 *
 * @param[recipe] A recipe items coordinate.
 * @param[input] An input items coordinate.
 */

data class MappedRelationComponent internal constructor(
    val recipe: CoordinateComponent,
    val input: CoordinateComponent
)

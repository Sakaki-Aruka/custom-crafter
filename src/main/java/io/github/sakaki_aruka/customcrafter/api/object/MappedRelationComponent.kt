package io.github.sakaki_aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import kotlinx.serialization.Serializable

/**
 * A component of a relation.
 *
 * @param[recipe] A recipe items coordinate.
 * @param[input] An input items coordinate.
 */

@Serializable
data class MappedRelationComponent internal constructor(
    val recipe: CoordinateComponent,
    val input: CoordinateComponent
)

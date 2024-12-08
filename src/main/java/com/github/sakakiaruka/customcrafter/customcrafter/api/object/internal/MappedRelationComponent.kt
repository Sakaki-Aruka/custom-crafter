package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent

data class MappedRelationComponent internal constructor(
    val recipe: CoordinateComponent,
    val input: CoordinateComponent
)

package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CMatter

data class CoordinateComponent(
    val x: Int,
    val y: Int,
    val matter: CMatter
) {
    //
}
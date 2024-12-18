package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

/**
 * A coordinate in Crafting slots.
 *
 * You can convert this for index(int) with the following expression. (x + y*9)
 *
 * Zero origin
 *
 * @param[x] X coordinate
 * @param[y] Y coordinate
 */
data class CoordinateComponent(
    val x: Int,
    val y: Int
) {
    //
}
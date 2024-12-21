package io.github.sakaki_aruka.customcrafter.api.`object`.recipe

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
    /**
     * returns a calculated index from a coordinate.
     *
     * @return[Int] calculated index
     */
    fun toIndex(): Int {
        return x + y * 9
    }

    companion object {
        /**
         * return specified size square frame coordinates set.
         *
         * the origin is (0, 0).
         *
         * for example
         * ```
         * val three = CoordinateComponent.square(3)
         * // xxx
         * // x_x
         * // xxx
         * // 'x' means a coordinate what is contained a result.
         * ```
         *
         * @param[size] size of square frame
         * @param[dx] initial x coordinate used to calculate. (default = 0)
         * @param[dy] initial y coordinate used to calculate. (default = 0)
         * @return[Set]<[CoordinateComponent]> set of frame coordinates.
         */
        fun square(size: Int, dx: Int = 0, dy: Int = 0): Set<CoordinateComponent> {
            if (size < 0) throw IllegalArgumentException("'size' must be greater than zero.")
            val result: MutableSet<CoordinateComponent> = mutableSetOf()
            (dy..<size + dy).forEach { y ->
                (dx..<size + dx).forEach { x ->
                    if (y == 0 || y == size - 1 + dy) {
                        result.add(CoordinateComponent(x, y))
                    } else if (x == 0 || x == size - 1 + dx) {
                        result.add(CoordinateComponent(x, y))
                    }
                }
            }
            return result
        }
    }
}
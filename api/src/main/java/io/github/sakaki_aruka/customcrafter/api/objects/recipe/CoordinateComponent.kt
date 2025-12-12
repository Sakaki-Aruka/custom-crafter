package io.github.sakaki_aruka.customcrafter.api.objects.recipe


/**
 * A coordinate in Crafting slots.
 *
 * You can convert this for index(int) with the following expression. (x + y*9)
 *
 * Zero origin
 *
 * @param[x] X coordinate
 * @param[y] Y coordinate
 * @since 5.0.0
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

        private fun inRange(limitExcluded: Int, vararg c: Int): Boolean = c.all { (0..<limitExcluded).contains(it) }

        /**
         * returns [CoordinateComponent] from the given index.
         *
         * @param[index] the input index
         * @return[CoordinateComponent] a converted [CoordinateComponent] from an input
         * @since 5.0.7
         */
        fun fromIndex(index: Int): CoordinateComponent {
            return CoordinateComponent(index % 9, index / 9)
        }

        /**
         * returns specified size square coordinates set.
         *
         *
         * the origin is (0, 0).
         *
         * for example
         * ```
         * val three = CoordinateComponent.square(3)
         * // xxx
         * // xxx
         * // xxx
         * // 'x' means a coordinate what is contained a result.
         * ```
         *
         * @param[size] size of square frame
         * @param[dx] initial x coordinate used to calculate. (default = 0)
         * @param[dy] initial y coordinate used to calculate. (default = 0)
         * @param[safeTrim] trims coordinates what are out of the CustomCrafter's gui range. (default = true)
         * @return[Set] set of filled coordinates.
         * @since 5.0.7
         */
        fun squareFill(size: Int, dx: Int = 0, dy: Int = 0, safeTrim: Boolean = true): Set<CoordinateComponent> {
            if (size < 0) throw IllegalArgumentException("'size' must be grater than zero.")
            val result: MutableSet<CoordinateComponent> = mutableSetOf()
            (dy..<size + dy).forEach { y ->
                (dx..<size + dx).forEach { x ->
                    if (safeTrim) {
                        if (inRange(6, x, y)) result.add(CoordinateComponent(x, y))
                    } else {
                        result.add(CoordinateComponent(x, y))
                    }
                }
            }
            return result
        }

        /**
         * returns specified size square frame coordinates set.
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
                    if (y == dy || y == size - 1 + dy) {
                        result.add(CoordinateComponent(x, y))
                    } else if (x == dx || x == size - 1 + dx) {
                        result.add(CoordinateComponent(x, y))
                    }
                }
            }
            return result
        }

        /**
         * Returns a list what contains specified amount of CoordinateComponent.
         *
         * @throws[IllegalArgumentException] If specified [n] is  < 1
         * @return[List] Result coordinates
         * @since 5.0.16
         */
        fun getN(n: Int): List<CoordinateComponent> {
            if (n < 1) throw IllegalArgumentException("'n' must be greater than zero.")
            return (0..<n).map { CoordinateComponent(it % 9, it / 9) }
        }
    }
}
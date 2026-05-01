package io.github.sakaki_aruka.customcrafter.recipe

import io.github.sakaki_aruka.customcrafter.matter.CMatter


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
        @JvmStatic
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
         * @param[size] size of square (0 or greater; 0 returns an empty set)
         * @param[dx] initial x coordinate used to calculate. (default = 0)
         * @param[dy] initial y coordinate used to calculate. (default = 0)
         * @param[safeTrim] trims coordinates what are out of the CustomCrafter's gui range. (default = true)
         * @return[Set] set of filled coordinates.
         * @since 5.0.7
         */
        @JvmStatic
        @JvmOverloads
        fun squareFill(size: Int, dx: Int = 0, dy: Int = 0, safeTrim: Boolean = true): Set<CoordinateComponent> {
            if (size < 0) throw IllegalArgumentException("'size' must be 0 or greater.")
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
         * @param[size] size of square frame (0 or greater; 0 returns an empty set)
         * @param[dx] initial x coordinate used to calculate. (default = 0)
         * @param[dy] initial y coordinate used to calculate. (default = 0)
         * @return[Set]<[CoordinateComponent]> set of frame coordinates.
         */
        @JvmStatic
        @JvmOverloads
        fun square(size: Int, dx: Int = 0, dy: Int = 0): Set<CoordinateComponent> {
            if (size < 0) throw IllegalArgumentException("'size' must be 0 or greater.")
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
        @JvmStatic
        fun getN(n: Int): List<CoordinateComponent> {
            if (n < 1) throw IllegalArgumentException("'n' must be greater than zero.")
            return (0..<n).map { CoordinateComponent(it % 9, it / 9) }
        }

        /**
         * Builds a `Map<CoordinateComponent, CMatter>` from a list of comma-separated rows and a token-to-matter mapping.
         *
         * Each element of [lines] represents one row (y-axis) of the crafting grid.
         * Within a row, tokens are separated by commas and represent individual slots (x-axis).
         * Each token is looked up in [map]; tokens not present in [map] are silently skipped,
         * which can be used to represent empty slots.
         *
         * Constraints:
         * - [lines] size must be in range 1 to 6 (each row maps to y = 0–5).
         * - Each row's token count must be in range 1 to 6 (each token maps to x = 0–5).
         * - If [lines] or [map] is empty, returns an empty map without throwing.
         *
         * ```kotlin
         * // Example — 3×3 shaped recipe
         * val lines = listOf(
         *     "g,g,g",
         *     "g,a,g",
         *     "g,g,g"
         * )
         * val map = mapOf(
         *     "a" to CMatterImpl.of(Material.APPLE),
         *     "g" to CMatterImpl.of(Material.GOLD_BLOCK)
         * )
         * val recipeMap = CoordinateComponent.recipeMapFromStringList(lines, map)
         * // CoordinateComponent(0,0)→GOLD_BLOCK  CoordinateComponent(1,0)→GOLD_BLOCK  CoordinateComponent(2,0)→GOLD_BLOCK
         * // CoordinateComponent(0,1)→GOLD_BLOCK  CoordinateComponent(1,1)→APPLE        CoordinateComponent(2,1)→GOLD_BLOCK
         * // CoordinateComponent(0,2)→GOLD_BLOCK  CoordinateComponent(1,2)→GOLD_BLOCK  CoordinateComponent(2,2)→GOLD_BLOCK
         * ```
         *
         * ```kotlin
         * // Example 2 — token not in map is skipped (acts as an empty slot)
         * val lines = listOf(
         *     "a,a",
         *     "_,a"   // "_" is not a key in map, so (0,1) is skipped
         * )
         * val map = mapOf("a" to CMatterImpl.of(Material.APPLE))
         * val recipeMap = CoordinateComponent.recipeMapFromStringList(lines, map)
         * // CoordinateComponent(0,0)→APPLE  CoordinateComponent(1,0)→APPLE
         * //                                  CoordinateComponent(1,1)→APPLE
         * ```
         *
         * @param[lines] Comma-separated row strings. Size must be 1 to 6.
         * @param[map] Mapping of token strings to [CMatter] instances.
         * @return[Map] Mapping of coordinates to matters. Empty if [lines] or [map] is empty.
         * @throws[IllegalArgumentException] If [lines] size exceeds 6, or any row's token count exceeds 6.
         * @since 5.0.17-p1
         */
        @JvmStatic
        fun recipeMapFromStringList(lines: List<String>, map: Map<String, CMatter>): Map<CoordinateComponent, CMatter> {
            if (lines.isEmpty() || map.isEmpty()) {
                return emptyMap()
            }
            if (lines.size > 6) {
                throw IllegalArgumentException("'lines' size must be in range of 1 to 6.")
            }
            val result: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
            for ((y: Int, line: String) in lines.withIndex()) {
                val separated: List<String> = line.split(",")
                if (separated.isEmpty() || separated.size > 6) {
                    throw IllegalArgumentException("Line size must be in range of 1 to 6.")
                }

                for ((x: Int, c: String) in separated.withIndex()) {
                    result[CoordinateComponent(x, y)] = map[c] ?: continue
                }
            }
            return result.toMap()
        }

        /**
         * Builds a `Map<CoordinateComponent, CMatter>` from an inverted mapping of matter to coordinates.
         *
         * This is the inverse of the layout used in [recipeMapFromStringList]: instead of specifying
         * where each slot goes, you declare which coordinates each [CMatter] occupies.
         * All coordinates must be within the valid crafting grid range (x: 0–5, y: 0–5).
         * Returns an empty map if [source] is empty.
         *
         * ```kotlin
         * // Example — 3×3 shaped recipe (ring pattern)
         * val source = mapOf(
         *     CMatterImpl.of(Material.GOLD_BLOCK) to setOf(
         *         CoordinateComponent(0, 0), CoordinateComponent(1, 0), CoordinateComponent(2, 0),
         *         CoordinateComponent(0, 1),                            CoordinateComponent(2, 1),
         *         CoordinateComponent(0, 2), CoordinateComponent(1, 2), CoordinateComponent(2, 2),
         *     ),
         *     CMatterImpl.of(Material.APPLE) to setOf(CoordinateComponent(1, 1))
         * )
         * val recipeMap = CoordinateComponent.mapToRecipeMap(source)
         * // G: GOLD_BLOCK, A: APPLE
         * // G,G,G
         * // G,A,G
         * // G,G,G
         * ```
         *
         * @param[source] Mapping of [CMatter] to the set of grid coordinates it occupies.
         * @return[Map] Mapping of coordinates to matters. Empty if [source] is empty.
         * @throws[IllegalArgumentException] If any coordinate has x or y outside the range 0–5.
         * @since 5.0.17-p1
         */
        @JvmStatic
        fun mapToRecipeMap(source: Map<CMatter, Set<CoordinateComponent>>): Map<CoordinateComponent, CMatter> {
            if (source.isEmpty()) {
                return emptyMap()
            }

            val result: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
            source.entries.forEach { (matter, coordinates) ->
                coordinates.forEach { c ->
                    if (!inRange(6, c.x, c.y)) {
                        throw IllegalArgumentException("coordinate 'x' and 'y' must be in range of 0 to 5. (Actual x: ${c.x}, y: ${c.y})")
                    }
                    result[c] = matter
                }
            }
            return result.toMap()
        }
    }
}
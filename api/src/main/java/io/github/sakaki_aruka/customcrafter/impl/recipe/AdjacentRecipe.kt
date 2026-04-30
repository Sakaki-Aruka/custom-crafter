package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent

/**
 * A semi-shaped recipe that sits between fully shaped and fully shapeless recipes.
 *
 * Like a shapeless recipe, items may be placed anywhere in the crafting grid with no
 * fixed positional constraints. Unlike a shapeless recipe, the directions in which
 * neighbouring items may appear are restricted by [RelationType]:
 *
 * - [RelationType.CROSS]: all neighbouring items must be directly above, below, left, or right;
 *   diagonally adjacent items are forbidden (↑ ↓ ← → only).
 * - [RelationType.DIAGONAL]: all neighbouring items must be at a diagonal corner;
 *   cross-adjacent items are forbidden (↖ ↗ ↙ ↘ only).
 * - [RelationType.BOTH]: all eight surrounding directions are allowed; no restriction is applied.
 *
 * Two items are considered "neighbours" if both their x-distance and y-distance are ≤ 1
 * (i.e. they fall inside the 3×3 area centred on either item). The predicate fails if any
 * item has a neighbour in a direction not included in the chosen [RelationType].
 *
 * ## Example — [RelationType.CROSS]
 * Valid (no pair of items is diagonally adjacent):
 * ```
 * _ _ # _ _ _
 * _ _ # _ _ _
 * _ _ # # # _
 * _ _ _ _ _ _
 * _ _ _ _ _ _
 * _ _ _ _ _ _
 * ```
 * Invalid (the `#` at `(0,0)` and the `#` at `(1,1)` are diagonal neighbours):
 * ```
 * # _ _ _ _ _    ← (0,0)
 * # # _ _ _ _    ← (0,1) and (1,1): (0,0) is diagonal to (1,1) → forbidden for CROSS
 * _ _ _ _ _ _
 * _ _ _ _ _ _
 * _ _ _ _ _ _
 * _ _ _ _ _ _
 * ```
 *
 * At least 2 items are required; [isValidRecipe] rejects recipes with fewer items.
 *
 * @param name Unique identifier for this recipe.
 * @param matters List of [CMatter] entries that make up the ingredients. Placement order
 *   within the grid is unconstrained (shapeless matching).
 * @param relationType Adjacency rule applied to every placed item. Defaults to [RelationType.BOTH].
 * @param customPredicates Additional [CRecipePredicate] conditions evaluated before the
 *   adjacency check. Pass `null` or omit to apply no extra conditions.
 * @param results [ResultSupplier] list that produces the craft output. `null` yields no result.
 * @since 5.1.0
 * @see RelationType
 * @see checker
 */
class AdjacentRecipe @JvmOverloads constructor(
    override val name: String,
    matters: List<CMatter>,
    val relationType: RelationType = RelationType.BOTH,
    customPredicates: List<CRecipePredicate>? = null,
    override val results: List<ResultSupplier>? = null
): CRecipe {
    override val items: Map<CoordinateComponent, CMatter> = CoordinateComponent.getN(matters.size)
        .zip(matters)
        .toMap()
    override val type: CRecipe.Type = CRecipe.Type.SHAPELESS
    override val predicates: List<CRecipePredicate> = (customPredicates ?: emptyList()) + checker(relationType)

    /**
     * Defines which neighbouring directions are permitted between placed items.
     *
     * @since 5.1.0
     */
    enum class RelationType {
        /** Neighbouring items must be at a diagonal corner (↖ ↗ ↙ ↘); cross-adjacent neighbours are forbidden. */
        DIAGONAL,

        /** Neighbouring items must be directly above, below, left, or right (↑ ↓ ← →); diagonally adjacent neighbours are forbidden. */
        CROSS,

        /** All eight surrounding directions are permitted; no constraint is applied to neighbouring items. */
        BOTH;

        internal fun offset(): Set<CoordinateComponent> {
            return when (this) {
                DIAGONAL -> diagonalOffset
                CROSS -> crossOffset
                BOTH -> diagonalOffset + crossOffset
            }
        }

        internal fun maxCount(): Int = when (this) {
            CROSS, DIAGONAL -> 18
            BOTH -> 36
        }

        internal fun maxPatternString(): String = when (this) {
            CROSS ->
                """
                |  # _ # _ # _   <- column 0, 2, 4 filled (3 cols x 6 rows = 18)
                |  # _ # _ # _
                |  # _ # _ # _
                |  # _ # _ # _
                |  # _ # _ # _
                |  # _ # _ # _
                """.trimMargin()
            DIAGONAL ->
                """
                |  # _ # _ # _   <- (even,even) and (odd,odd) cells filled (18)
                |  _ # _ # _ #
                |  # _ # _ # _
                |  _ # _ # _ #
                |  # _ # _ # _
                |  _ # _ # _ #
                """.trimMargin()
            BOTH -> "  (no constraint: all 36 cells are valid)"
        }

        /**
         * #_#
         * _0_
         * #_#
         */
        private val diagonalOffset: Set<CoordinateComponent> = setOf(
            CoordinateComponent(-1, -1),
            CoordinateComponent(1, -1),
            CoordinateComponent(-1, 1),
            CoordinateComponent(1, 1)
        )

        /**
         * _#_
         * #0#
         * _#_
         */
        private val crossOffset: Set<CoordinateComponent> = setOf(
            CoordinateComponent(0, -1),
            CoordinateComponent(-1, 0),
            CoordinateComponent(1, 0),
            CoordinateComponent(0, 1)
        )
    }

    companion object {
        /**
         * Returns a [CRecipePredicate] that enforces the direction constraint for the given [relationType].
         *
         * The predicate passes if and only if no item in the player's input has a neighbouring item
         * in a direction not included in [relationType]. Items whose x-distance or y-distance exceeds 1
         * are not considered neighbours and impose no constraint on each other.
         *
         * @param relationType The permitted-direction rule to enforce.
         * @return A [CRecipePredicate] implementing the direction constraint check.
         * @since 5.1.0
         */
        fun checker(relationType: RelationType): CRecipePredicate {
            return CRecipePredicate { context ->
                passedPattern(relationType.offset(), context.input.materials.keys)
            }
        }

        private fun passedPattern(
            offsets: Set<CoordinateComponent>,
            target: Set<CoordinateComponent>
        ): Boolean {
            return target.all { c ->
                val correctRange: Set<CoordinateComponent> = offsets.map { offset ->
                    CoordinateComponent(c.x + offset.x, c.y + offset.y)
                }.toSet()

                val surround: Set<CoordinateComponent> = target
                    .filterNot { (x, y) -> x == c.x && y == c.y }
                    .filter { (x, y) -> x in (c.x - 1..c.x + 1) && y in (c.y - 1..c.y + 1) }
                    .toSet()

                correctRange.containsAll(surround)
            }
        }
    }

    /**
     * Validates this recipe's configuration.
     *
     * Returns [Result.failure] with [IllegalArgumentException] if fewer than 2 items are
     * registered, since a single item can never satisfy any adjacency condition.
     * Otherwise, delegates to [CRecipe.isValidRecipe].
     *
     * @return [Result.success] if the recipe is valid, [Result.failure] otherwise.
     * @since 5.1.0
     */
    override fun isValidRecipe(): Result<Unit> {
        if (this.items.size < 2) {
            return Result.failure(IllegalArgumentException("'items' size must be at least 2. (actual: ${this.items.size})"))
        }

        if (this.items.size > this.relationType.maxCount()) {
            val relType = this.relationType
            val max = relType.maxCount()
            return Result.failure(IllegalStateException(
                buildString {
                    appendLine("This AdjacentRecipe exceeds the maximum item count for '${relType}'.")
                    appendLine("  Current : ${this@AdjacentRecipe.items.size}")
                    appendLine("  Maximum : $max")
                    appendLine()
                    appendLine("  ┌─ Maximum placement pattern for '${relType}' ─────────────┐")
                    relType.maxPatternString().lines().forEach { appendLine("  │  $it") }
                    append(  "  └────────────────────────────────────────────────────────┘")
                }
            ))
        }

        return super.isValidRecipe()
    }
}
package io.github.sakaki_aruka.customcrafter.recipe

/**
 * A group of recipe slots (identified by their [CoordinateComponent] keys in [CRecipe.items])
 * that is satisfied as a whole rather than slot-by-slot.
 *
 * Used by [CRecipe.matchGroups] to tell [CRecipe.Type.SHAPELESS] matching how many of [members]
 * must actually be filled by an input item. A recipe slot whose coordinate is not covered by
 * matching is simply never assigned an input; unlike [GroupRecipe], no `Material.AIR` placeholder
 * candidate is required to express "this slot may stay empty".
 *
 * @param[members] Coordinates (keys of [CRecipe.items]) belonging to this group. Must not be empty.
 * @param[min] Minimum number of [members] that must be matched to an input item. Must be >= 0.
 * @param[max] Maximum number of [members] that may be matched to an input item. Must be in range [min]..[members].size.
 * @see[CRecipe.matchGroups]
 * @see[ShapelessGroupRecipe]
 * @since 5.3.0
 */
class MatchGroup internal constructor(
    val members: Set<CoordinateComponent>,
    val min: Int,
    val max: Int
) {
    companion object {
        /**
         * Creates a [MatchGroup] with validation.
         *
         * @param[members] Coordinates belonging to this group. Must not be empty.
         * @param[min] Minimum number of matched members. Must be >= 0.
         * @param[max] Maximum number of matched members. Must be in range [min] to `members.size`.
         * @throws[IllegalArgumentException] If [members] is empty, [min] is negative, [max] is less than [min],
         *   or [max] exceeds `members.size`.
         * @return[MatchGroup] Created group.
         * @since 5.3.0
         */
        @JvmStatic
        fun of(
            members: Set<CoordinateComponent>,
            min: Int,
            max: Int
        ): MatchGroup {
            if (members.isEmpty()) {
                throw IllegalArgumentException("'members' must not be empty.")
            } else if (min < 0) {
                throw IllegalArgumentException("'min' must be zero or positive number.")
            } else if (max < min) {
                throw IllegalArgumentException("'max' must be greater than or equal to 'min'. (min: $min, max: $max)")
            } else if (max > members.size) {
                throw IllegalArgumentException(
                    "'max' must not exceed 'members' size. (members: ${members.size}, max: $max)"
                )
            }
            return MatchGroup(members, min, max)
        }

        /**
         * Creates a [MatchGroup] that requires exactly one specific coordinate to be matched.
         *
         * Equivalent to `MatchGroup.of(setOf(coordinate), min = 1, max = 1)`. This is what
         * [CRecipe.matchGroups]'s default implementation uses for every recipe slot.
         *
         * @param[coordinate] The single coordinate this group covers.
         * @return[MatchGroup] Created mandatory group.
         * @since 5.3.0
         */
        @JvmStatic
        fun mandatory(coordinate: CoordinateComponent): MatchGroup {
            return MatchGroup(setOf(coordinate), 1, 1)
        }
    }
}

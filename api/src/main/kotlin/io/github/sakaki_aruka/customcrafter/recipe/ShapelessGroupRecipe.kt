package io.github.sakaki_aruka.customcrafter.recipe

import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.result.ResultSupplier

/**
 * Implementation of [CRecipe].
 *
 * This recipe only provides [CRecipe.Type.SHAPELESS].
 *
 * This is the SHAPELESS counterpart of [GroupRecipe]: [groups] states, for each set of recipe
 * slots, how many of them must actually be matched to an input item ([MatchGroup.min] to
 * [MatchGroup.max]). Unlike [GroupRecipe], no `Material.AIR` placeholder candidate is needed to
 * express "this slot may stay unmatched" — SHAPELESS matching has no concept of position, so an
 * unmatched slot is simply absent from the resulting relation. The min/max bounds are enforced
 * structurally by the matching itself (see [io.github.sakaki_aruka.customcrafter.search.Search]),
 * so — again unlike [GroupRecipe] — no extra [CRecipePredicate] safety net is auto-appended here.
 *
 * @param[name] Name of this recipe
 * @param[items] Item mapping. Coordinates are only used as unique slot identifiers; position is irrelevant.
 * @param[groups] Groups covering every coordinate in [items]. Every coordinate must belong to exactly one group.
 * @param[predicates] Additional predicates. Defaults to none.
 * @param[results] [ResultSupplier] list
 * @see[CRecipe]
 * @see[MatchGroup]
 * @since 5.3.0
 */
open class ShapelessGroupRecipe @JvmOverloads constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    val groups: Set<MatchGroup>,
    override val predicates: List<CRecipePredicate>? = null,
    override val results: List<ResultSupplier>? = null
): CRecipe, UnPartialSearchableRecipe {
    override val type: CRecipe.Type = CRecipe.Type.SHAPELESS

    override fun matchGroups(): List<MatchGroup> = this.groups.toList()

    companion object {
        /**
         * Creates a collection of [MatchGroup]s, which are grouping settings that can be set in [ShapelessGroupRecipe.groups].
         *
         * [missingGroups] is a collection of already created [MatchGroup]s; any coordinates of [items] not
         * covered by them are each turned into their own mandatory group ([MatchGroup.mandatory]).
         *
         * If the resulting set is invalid, throws an exception.
         *
         * @param[items] Coordinate and CMatter mapping
         * @param[missingGroups] Set of [MatchGroup]s already created
         * @throws[IllegalArgumentException] If the resulting set is invalid.
         * @see[ShapelessGroupRecipe.isValidGroups]
         * @return[Set] A [MatchGroup] set usable for [ShapelessGroupRecipe.groups]
         * @since 5.3.0
         */
        @JvmStatic
        fun createGroups(
            items: Map<CoordinateComponent, CMatter>,
            missingGroups: Set<MatchGroup>
        ): Set<MatchGroup> {
            val grouped: Set<CoordinateComponent> = missingGroups.flatMap { it.members }.toSet()
            val result: MutableSet<MatchGroup> = missingGroups.toMutableSet()
            for (c in items.keys - grouped) {
                result.add(MatchGroup.mandatory(c))
            }

            isValidGroups(result, items)
            return result
        }

        /**
         * Validates the specified group set against the items mapping.
         *
         * If those are invalid, throws an exception that includes error messages.
         *
         * - Every [MatchGroup.members] coordinate must exist as a key in [items].
         * - No coordinate may belong to more than one group.
         * - Every coordinate in [items] must belong to exactly one group (no gaps).
         *
         * @param[groups] Set of [MatchGroup]
         * @param[items] Item mapping on [ShapelessGroupRecipe]
         * @throws[IllegalArgumentException] If [groups] and [items] are inconsistent
         * @since 5.3.0
         */
        @JvmStatic
        fun isValidGroups(
            groups: Set<MatchGroup>,
            items: Map<CoordinateComponent, CMatter>
        ) {
            val counts: MutableMap<CoordinateComponent, Int> = mutableMapOf()
            for (group in groups) {
                for (c in group.members) {
                    counts[c] = (counts[c] ?: 0) + 1
                }
            }

            if (!items.keys.containsAll(counts.keys)) {
                val builder = StringBuilder(
                    "All CoordinateComponents included in the members of 'groups' must exist as keys in 'items'.${System.lineSeparator()}"
                )
                for (notContained in counts.keys - items.keys) {
                    builder.append("  'items' not contains: $notContained ${System.lineSeparator()}")
                }
                throw IllegalArgumentException(builder.toString())
            } else if (counts.values.any { it > 1 }) {
                val builder = StringBuilder()
                builder.append("A coordinate is not allowed to belong to more than one group. ${System.lineSeparator()}")
                for ((c, count) in counts.filter { (_, n) -> n > 1 }) {
                    builder.append("  duplicated: (${c.x}, ${c.y}), times: $count ${System.lineSeparator()}")
                }
                throw IllegalArgumentException(builder.toString())
            } else if (!counts.keys.containsAll(items.keys)) {
                val builder = StringBuilder("Every coordinate in 'items' must belong to exactly one group. ${System.lineSeparator()}")
                for ((x, y) in items.keys - counts.keys) {
                    builder.append("  not covered: ($x, $y) ${System.lineSeparator()}")
                }
                throw IllegalArgumentException(builder.toString())
            }
        }
    }

    override fun isValidRecipe() {
        if (this.items.isEmpty() || this.items.size > 36) {
            throw IllegalStateException("'items' must contain 1 to 36 valid CMatters.")
        }

        val builder = StringBuilder()
        for ((c, matter) in this.items.entries) {
            try {
                matter.isValidMatter()
            } catch (t: IllegalStateException) {
                builder.append("[items] x: ${c.x}, y: ${c.y}, ${t.message} ${System.lineSeparator()}")
            }
        }
        if (builder.isNotEmpty()) {
            throw IllegalStateException(builder.toString())
        }

        isValidGroups(this.groups, this.items)
    }
}

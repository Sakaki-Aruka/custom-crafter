package io.github.sakaki_aruka.customcrafter.matter

import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import org.bukkit.Material

/**
 * This interface's implementing types can be used as materials for [CRecipe].
 *
 * @see[CRecipe]
 * @see[CMatterPredicate]
 * @since 5.0.0
 */
interface CMatter {
    /** Name of this matter. */
    val name: String
    /** Set of acceptable [Material] types for this slot. */
    val candidate: Set<Material>
    /** Required item count. Ignored when [anyAmount] is `true`. Must be 1 or greater. */
    val amount: Int
    /** If `true`, any input amount ≥ 1 is accepted regardless of [amount]. If `false`, input must have at least [amount] items. */
    val anyAmount: Boolean
    /**
     * List of additional [CMatterPredicate] checks that run during slot matching. `null` or empty means no additional conditions.
     *
     * Ordered, so an unnamed predicate can be reported by its index in diagnostic logs.
     * @see[CMatterPredicate.name]
     */
    val predicates: List<CMatterPredicate>?

    /**
     * Validates this [CMatter].
     *
     * CMatter's default implementation checks below conditions.
     * - [CMatter.candidate] is not empty
     * - [CMatter.candidate] does not contain [Material.isAir] and ![Material.isItem]
     * - [CMatter.amount] is 1 or more
     *
     * ```kotlin
     * // (Usage)
     * val matter: CMatter = ~~~
     * matter.isValidMatter()
     * ```
     *
     * This is called from [CRecipe.isValidRecipe].
     *
     * @throws[IllegalStateException] If this matter is invalid
     * @since 5.0.15
     */
    fun isValidMatter() {
        if (this.candidate.isEmpty()) {
            throw IllegalStateException("'candidate' must contain correct materials at least one.")
        } else if (this.candidate.any { m -> m.isAir || !m.isItem }) {
            throw IllegalStateException("'candidate' not allowed to contain materials that are 'Material#isAir' or '!Material#isItem'.")
        } else if (this.amount < 1) {
            throw IllegalStateException("'amount' must be 1 or more.")
        }
    }

    /**
     * returns this CMatter has some predicates or not.
     *
     * @return[Boolean] == `!predicates.isNullOrEmpty()`
     */
    fun hasPredicates(): Boolean = !predicates.isNullOrEmpty()

    /**
     * Returns a merged result of all predicates run.
     *
     * ```kotlin
     * // Default implementation on `CMatter#predicatesResult`
     * fun predicatesResult(ctx: CMatterPredicate.Context): Boolean {
     *     return predicates?.all { p -> p.test(ctx) } ?: true
     * }
     * ```
     *
     * @param[ctx] Context of CMatterPredicate execution
     * @return[Boolean] all or nothing.
     */
    fun predicatesResult(ctx: CMatterPredicate.Context): Boolean {
        return predicates?.all { p -> p.test(ctx) } ?: true
    }

    /**
     * Returns the first [CMatterPredicate] that rejects [ctx], or `null` when all of them pass.
     *
     * Evaluates predicates in order and stops at the first failure, exactly as [predicatesResult]
     * does, so this can be used in its place when the caller needs to report which predicate failed
     * rather than only whether one did. The returned index is the position within [predicates] and
     * identifies a predicate that did not override [CMatterPredicate.name].
     *
     * @param[ctx] Context of CMatterPredicate execution
     * @return[Pair] The failing predicate's index paired with the predicate itself, or `null` when nothing failed
     * @since 5.3.0
     */
    fun firstFailedPredicate(ctx: CMatterPredicate.Context): Pair<Int, CMatterPredicate>? {
        return predicates
            ?.withIndex()
            ?.firstOrNull { (_, predicate) -> !predicate.test(ctx) }
            ?.let { (i, predicate) -> i to predicate }
    }
}
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
    /** List of additional [CMatterPredicate] checks that run during slot matching. `null` or empty means no additional conditions. */
    val predicates: Collection<CMatterPredicate>?

    /**
     * Returns this [CMatter] is a valid or not.
     *
     * CMatter's default implementation checks below conditions.
     * - [CMatter.candidate] is not empty
     * - [CMatter.candidate] does not contain [Material.isAir] and ![Material.isItem]
     * - [CMatter.amount] is 1 or more
     *
     * ```kotlin
     * // (Usage)
     * val matter: CMatter = ~~~
     * matter.isValidMatter().exceptionOrNull()?.let { throw it }
     * ```
     *
     * This is called from [CRecipe.isValidRecipe].
     *
     * @return[Result] Result of check
     * @since 5.0.15
     */
    fun isValidMatter(): Result<Unit> {
        return if (this.candidate.isEmpty()) {
            Result.failure(IllegalStateException("'candidate' must contain correct materials at least one."))
        } else if (this.candidate.any { m -> m.isAir || !m.isItem }) {
            Result.failure(IllegalStateException("'candidate' not allowed to contain materials that are 'Material#isAir' or '!Material#isItem'."))
        } else if (this.amount < 1) {
            Result.failure(IllegalStateException("'amount' must be 1 or more."))
        } else Result.success(Unit)
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
}
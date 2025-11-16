package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import org.bukkit.Material

/**
 * This interface's implementing types can be used as materials for [CRecipe].
 *
 * @param[name] Name of this matter
 * @param[candidate] Material candidate of this matter
 * @param[amount] Amount of this matter requires
 * @param[mass] Always requires 1 item on input
 * @param[predicates] List of lambda functions what runs on search process
 *
 * @see[CRecipe]
 * @see[CMatterPredicate]
 * @since 5.0.0
 */
interface CMatter {
    val name: String
    val candidate: Set<Material>
    val amount: Int
    val mass: Boolean
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
     * ```Kotlin
     * // Default implementation on `CMatter#predicateResult`
     * fun predicateResult(ctx: CMatterPredicate.Context): Boolean {
     *     return predicates?.all { p -> p.predicate(ctx) } ?: true
     * }
     * ```
     *
     * @param[ctx] Context of CMatterPredicate execution
     * @return[Boolean] all or nothing.
     */
    fun predicatesResult(ctx: CMatterPredicate.Context): Boolean {
        return predicates?.all { p -> p.predicate(ctx) } ?: true
    }
}
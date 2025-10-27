package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * This interface's implementing types can be used as materials for [CRecipe].
 *
 * @since 5.0.0
 */
interface CMatter {
    val name: String
    val candidate: Set<Material>
    val amount: Int
    val mass: Boolean
    val predicates: Set<CMatterPredicate>?

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
     * @return[Boolean] `predicates != null && predicates!!.isNotEmpty()`
     */
    fun hasPredicates(): Boolean = predicates != null && predicates!!.isNotEmpty()

    /**
     * returns a merged result of all predicates run.
     *
     * @return[Boolean] all or nothing.
     */
    fun predicatesResult(
        self: ItemStack,
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): Boolean {
        return predicates?.all { p -> p.predicate(self, mapped, recipe, crafterID) } ?: true
    }

    /**
     * returns a matter what is applied `amount = 1`.
     *
     * @return[CMatter] applied `amount = 1`
     */
    fun asOne(): CMatter

}
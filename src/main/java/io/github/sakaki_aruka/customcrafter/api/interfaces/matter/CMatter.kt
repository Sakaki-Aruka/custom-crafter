package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
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
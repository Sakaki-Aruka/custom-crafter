package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterPredicate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

/**
 * This interface's implementing types can be used as materials for [CRecipe].
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
        return if (!predicates.isNullOrEmpty()) {
            predicates!!.all { p -> p.predicate.invoke(self, mapped, recipe, crafterID) }
        } else true
    }

    /**
     * returns a matter what is applied `amount = 1`.
     *
     * @return[CMatter] applied `amount = 1`
     */
    fun asOne(): CMatter

}
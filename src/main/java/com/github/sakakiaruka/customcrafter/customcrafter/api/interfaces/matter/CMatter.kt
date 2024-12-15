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
    val persistentDataContainer: PersistentDataContainer?

    fun hasPredicates(): Boolean = predicates != null
    fun hasPDC(): Boolean = persistentDataContainer != null

    fun predicatesResult(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): Boolean {
        return if (predicates != null) {
            if (hasPDC()) {
                predicates!!.all { p -> p.predicate(mapped, persistentDataContainer!!, recipe, crafterID) }
            } else false
        } else false
    }
    fun asOne(): CMatter

}
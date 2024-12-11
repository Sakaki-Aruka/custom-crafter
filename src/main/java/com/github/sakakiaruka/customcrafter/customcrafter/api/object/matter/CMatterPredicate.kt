package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * A [CMatter]'s predicate functions.
 */
data class CMatterPredicate(
    val predicate: (
        mapped: Map<CoordinateComponent, ItemStack>,
        container: PersistentDataContainer,
        recipe: CRecipe,
        player: Player) -> Boolean
)
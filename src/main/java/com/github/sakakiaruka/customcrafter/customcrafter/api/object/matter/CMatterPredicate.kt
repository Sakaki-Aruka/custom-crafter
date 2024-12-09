package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * A [CMatter]'s predicate functions.
 */
data class CMatterPredicate(
    val predicates: List<(items: ItemStack, container: PersistentDataContainer) -> Boolean>
)
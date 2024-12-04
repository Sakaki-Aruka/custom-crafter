package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

data class CMatterPredicate(
    val predicates: List<(items: ItemStack, container: PersistentDataContainer) -> Boolean>
)
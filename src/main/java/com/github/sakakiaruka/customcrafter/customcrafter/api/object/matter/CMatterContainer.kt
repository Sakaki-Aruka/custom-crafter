package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

data class CMatterContainer(
    val predicates: List<(player: Player, items: Map<CoordinateComponent, ItemStack>, container: PersistentDataContainer) -> Boolean>
)
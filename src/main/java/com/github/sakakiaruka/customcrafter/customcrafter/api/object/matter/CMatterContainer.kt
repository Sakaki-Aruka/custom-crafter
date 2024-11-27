package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class CMatterContainer(
    val predicates: List<(player: Player, items: List<ItemStack>) -> Boolean>
)
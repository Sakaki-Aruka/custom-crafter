package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class CRecipeContainer(
    val consumers: Map<(player: Player, items: List<ItemStack>) -> Boolean, List<(player: Player, items: List<ItemStack>) -> Unit>> // predicate, List<Consumer>
)
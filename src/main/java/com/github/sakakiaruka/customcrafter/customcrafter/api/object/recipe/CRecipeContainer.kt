package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.MappedRelation
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class CRecipeContainer(
    val consumers: List<Pair<Predicate, Consumer>>
    //val consumers: List<Pair<(player: Player, items: List<ItemStack>) -> Boolean, List<(player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>) -> Unit>>> // predicate, List<Consumer>
) {
    data class Predicate(
        val func: (player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>) -> Boolean
    )

    data class Consumer(
        val func: (player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>) -> Unit
    )

    fun run(
        player: Player,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>
    ) {
        consumers
            .filter { (p, _) -> p.func(player, relate, mapped) }
            .forEach { (_, c) -> c.func(player, relate, mapped) }
    }
}
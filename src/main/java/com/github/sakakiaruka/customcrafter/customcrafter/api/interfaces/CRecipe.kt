package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeContainer
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeType
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface CRecipe {
    val name: String
    val items: Map<CoordinateComponent, CMatter>
    val containers: List<CRecipeContainer>?
    val results: List<(player: Player) -> ItemStack>?
    val type: CRecipeType

    fun runContainers(player: Player, items: List<ItemStack>) {
        containers?.forEach { container ->
            container.consumers.forEach { (predicate, consumer) ->
                if (predicate(player, items)) consumer.forEach { c -> c(player, items) }
            }
        }
    }

    fun giveResults(player: Player) {
        val world: World = player.world
        val loc: Location = player.location
        results?.forEach { resultSupplier ->
            player.inventory.addItem(resultSupplier(player))
                .forEach { (_, overflownItem) ->
                    world.dropItem(loc, overflownItem)
                }
        }
    }
}
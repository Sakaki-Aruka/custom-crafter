package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.MappedRelation
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeContainer
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeType
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * This interface's implementing types can be used as recipes for CustomCrafter.
 */
interface CRecipe {
    val name: String
    val items: Map<CoordinateComponent, CMatter>
    val containers: List<CRecipeContainer>?
    val results: List<(player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>) -> List<ItemStack>>?
    val type: CRecipeType

    fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipe

    fun runContainers(player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>, results: MutableList<ItemStack>) {
        containers?.forEach { container ->
            container.run(player, relate, mapped, results)
        }
    }

    fun getResults(player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>): MutableList<ItemStack> {
        return results?.let { consumers ->
            val list: MutableList<ItemStack> = mutableListOf()
            consumers.map { c ->
                list.addAll(c(player, relate, mapped))
            }
            list
        } ?: mutableListOf()
    }
}
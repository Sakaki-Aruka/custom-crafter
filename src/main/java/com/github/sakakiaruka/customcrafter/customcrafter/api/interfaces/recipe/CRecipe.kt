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

    /**
     * replace [items]
     *
     * @param[newItems] new items what are replace old.
     * @return[CRecipe] created new recipe.
     */
    fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipe

    /**
     * runs all [containers] if it is not null.
     *
     * @param[player] a crafter
     * @param[relate] an input inventory and [CRecipe] coordinates relation.
     * @param[mapped] coordinates and input items relation.
     * @param[results] generated results by this recipe.
     */
    fun runContainers(player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>, results: MutableList<ItemStack>) {
        containers?.forEach { container ->
            container.run(player, relate, mapped, results)
        }
    }

    /**
     * returns results of suppliers made
     *
     * @param[player] a crafter
     * @param[relate] an input inventory and [CRecipe] coordinates relation.
     * @param[mapped] coordinates and input items relation.
     * @return[MutableList<ItemStack>] generated items list. if no item supplier applied, returns an empty list.
     */
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
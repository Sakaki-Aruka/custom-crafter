package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.MappedRelation
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class CRecipe(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val containers: List<CRecipeContainer>?,
    override val results: List<(player: Player, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>) -> List<ItemStack>>?,
    override val type: CRecipeType,
): CRecipe {
    //
    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipe {
        return CRecipe(this.name, newItems, this.containers, this.results, this.type)
    }
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CRecipe
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class CRecipe(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val containers: List<CRecipeContainer>?,
    override val results: List<(player: Player) -> ItemStack>?,
    override val type: CRecipeType,
): CRecipe {
    //
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class CRecipe(
    val name: String,
    val items: List<CoordinateComponent>,
    val containers: List<CRecipeContainer>?,
    val results: List<(player: Player) -> ItemStack>?
) {
    //
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.search

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object Search {

    fun search(player: Player, inventory: Inventory, one: Boolean) {
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory)
            .takeIf { it?.isNotEmpty() == true } ?: return

        val candidate: List<CRecipe> = CustomCrafterAPI.RECIPES
            .filter { it.items.size == mapped.size }
            .takeIf { it.isNotEmpty() } ?: return

    }
}
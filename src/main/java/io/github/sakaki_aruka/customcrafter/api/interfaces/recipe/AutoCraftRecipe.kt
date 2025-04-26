package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A subinterface of [CRecipe] for Auto Crafting feature.
 *
 * @since 5.0.10
 */
interface AutoCraftRecipe: CRecipe {
    val autoCraftID: UUID

    fun getAutoCraftDisplayItem(player: Player): ItemStack
}
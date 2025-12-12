package io.github.sakaki_aruka.customcrafter.api.objects

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import kotlin.math.max

/**
 * A view of crafting gui.
 *
 * @param[materials] A mapping of materials what were placed by a player.
 * @param[result] An item that is placed the result item slot.
 */

data class CraftView internal constructor(
    val materials: Map<CoordinateComponent, ItemStack>,
    val result: ItemStack
) {
    /**
     * decrement items from the provided CraftView
     *
     * if [forCustomSettings] is not null, this runs a process for CRecipe.
     *
     * only for Shift clicked
     *
     * @param[shiftUsed] a crafter used shift-click or not
     * @param[forCustomSettings] a matched result info. (requires these when matched custom recipe)
     * @since 5.0.8
     */
    fun getDecrementedCraftView(
        shiftUsed: Boolean = true,
        forCustomSettings: Pair<CRecipe, MappedRelation>? = null
    ): CraftView {
        val minAmount: Int = this.materials.values
            .filter { item -> !item.isEmpty && item.type.isItem }
            .minOf { item -> item.amount }
        return forCustomSettings?.let { (cRecipe, mapped) ->
            val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
            mapped.components.forEach { component ->
                val matter: CMatter = cRecipe.items[component.recipe]!!
                val isMass: Boolean = matter.mass
                val decrementAmount: Int =
                    if (isMass) 1
                    else if (shiftUsed) (minAmount / matter.amount) * matter.amount
                    else matter.amount
                val newAmount: Int = max(0, this.materials[component.input]!!.amount - decrementAmount)
                map[component.input] =
                    if (newAmount == 0) ItemStack.empty()
                    else let {
                        val newItem: ItemStack = this.materials[component.input]?.clone() ?: ItemStack.empty()
                        newItem.amount = newAmount
                        newItem
                    }
            }
            CraftView(map, ItemStack.empty())
        } ?: run {
            val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
            this.materials.forEach { (c, item) ->
                val newAmount: Int = max(0, item.amount - if (shiftUsed) minAmount else 1)
                map[c] =
                    if (newAmount == 0) ItemStack.empty()
                    else let {
                        val newItem: ItemStack = item.clone()
                        newItem.amount = newAmount
                        newItem
                    }
            }
            CraftView(map, ItemStack.empty())
        }
    }

    /**
     * Drops all contained items.
     * @param[world] dropped world
     * @param[location] dropped location
     * @since 5.0.13
     */
    fun drop(world: World, location: Location) {
        this.materials.values.filter { item -> !item.isEmpty }
            .forEach { item -> world.dropItem(location, item) }

        this.result.takeIf { item -> !item.isEmpty }?.let { item ->
            world.dropItem(location, item)
        }
    }
}

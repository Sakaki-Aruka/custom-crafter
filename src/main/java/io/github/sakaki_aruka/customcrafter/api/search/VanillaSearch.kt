package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

object VanillaSearch {
    /**
     * if a recipe found with provided input, returns that.
     * but, there is no recipe to find with provided inputs, returns null.
     *
     * @param[world] a crafter exists world
     * @param[inventory] an input inventory
     * @return[Recipe] a found recipe
     */
    fun search(
        world: World,
        inventory: Inventory
    ): Recipe? {
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory) ?: return null
        val nineArray: Array<ItemStack> = getNineItemStackArray(mapped) ?: return null
        return Bukkit.getCraftingRecipe(nineArray, world)
    }

    private fun getNineItemStackArray(
        mapped: Map<CoordinateComponent, ItemStack>
    ): Array<ItemStack>? {
        if (mapped.isEmpty()) return null
        val minCoordinate: CoordinateComponent = mapped
            .filter { (_, item) -> !item.type.isEmpty }
            .keys.minBy { i -> i.toIndex() }
        val list: MutableList<ItemStack> = mutableListOf()
        CoordinateComponent.squareFill(
            size = 3,
            dx = minCoordinate.x,
            dy = minCoordinate.y,
            safeTrim = false
        ).sortedBy { c -> c.toIndex() }
            .takeIf { arr -> arr.containsAll(mapped.keys) }
            ?.forEach { c ->
                list.add(mapped[c] ?: ItemStack.empty())
            } ?: return null
        return list.toTypedArray()
    }
}
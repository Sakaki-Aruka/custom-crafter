package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

object VanillaSearch {
    /**
     * if a recipe found with provided input, returns that.
     * but, there is no recipe to find with provided inputs, returns null.
     *
     * @param[world] a crafter exists world
     * @param[view] an input view
     * @return[Recipe] a found recipe
     */
    fun search(
        world: World,
        view: CraftView
    ): Recipe? {
        val nineArray: Array<ItemStack> = getNineItemStackArray(view.materials) ?: return null
        return Bukkit.getCraftingRecipe(nineArray, world)
    }

    /**
     * If a recipe found with provided input, returns that.
     * But, there is no recipe to find with provided inputs, returns null.
     *
     * @param[world] crafter exists world
     * @param[mapped] input items
     * @return[Recipe] a found recipe
     * @since 5.0.14
     */
    fun search(
        world: World,
        mapped: Map<CoordinateComponent, ItemStack>
    ): Recipe? {
        val nineArray: Array<ItemStack> = getNineItemStackArray(mapped)
            ?: return null
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
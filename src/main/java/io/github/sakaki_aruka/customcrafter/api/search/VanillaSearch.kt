package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import kotlin.math.max

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

    private fun getNineItemStackArray(mapped: Map<CoordinateComponent, ItemStack>): Array<ItemStack>? {
        // TODO fix here (use default 'index')
        if (mapped.isEmpty()) return null
        val xMin: CoordinateComponent = mapped.keys.minBy { it.x }
        val xMax: CoordinateComponent = mapped.keys.maxBy { it.x }
        val yMin: CoordinateComponent = mapped.keys.minBy { it.y }
        val yMax: CoordinateComponent = mapped.keys.maxBy { it.y }
        if (max(xMax.x - xMin.x, yMax.y - yMin.y) > 2) return null

        val result: Array<ItemStack> = Array(9) { ItemStack.empty() }
        Converter.getAvailableCraftingSlotComponents()
            .filter { (xMin.x..xMax.x).contains(it.x)
                    && (yMin.y..yMax.y).contains(it.y)
            }
            .withIndex()
            .map { (index, c) ->
                result[index] = mapped.getOrDefault(c, ItemStack.empty())
            }
        return result
    }
}
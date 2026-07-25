package io.github.sakaki_aruka.customcrafter.search

import io.github.sakaki_aruka.customcrafter.debug.Explainer
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
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
     * @param[explainer] Logs container instance (since 5.3.0)
     * @return[Recipe] a found recipe
     */
    @JvmStatic
    @JvmOverloads
    fun search(
        world: World,
        view: CraftView,
        explainer: Explainer? = null
    ): Recipe? {
        return search(world, view.materials, explainer)
    }

    /**
     * If a recipe found with provided input, returns that.
     * But, there is no recipe to find with provided inputs, returns null.
     *
     * @param[world] crafter exists world
     * @param[mapped] input items
     * @param[explainer] Logs container instance (since 5.3.0)
     * @return[Recipe] a found recipe
     * @since 5.0.14
     */
    @JvmStatic
    @JvmOverloads
    fun search(
        world: World,
        mapped: Map<CoordinateComponent, ItemStack>,
        explainer: Explainer? = null
    ): Recipe? {
        val nineArray: Array<ItemStack> = getNineItemStackArray(mapped, explainer)
            ?: return null
        val found: Recipe? = Bukkit.getCraftingRecipe(nineArray, world)
        explainer?.writeLog(Explainer.Loglevel.DEBUG, "[VanillaSearch/search] Bukkit#getCraftingRecipe: world=${world.name}, found=${found != null}")
        return found
    }

    /**
     * Flattens the input into the 3x3 array Bukkit's vanilla lookup requires.
     *
     * Returns `null` when the input cannot be expressed as a 3x3 grid, which makes the whole
     * vanilla search return `null` without ever reaching Bukkit.
     */
    private fun getNineItemStackArray(
        mapped: Map<CoordinateComponent, ItemStack>,
        explainer: Explainer? = null
    ): Array<ItemStack>? {
        if (mapped.isEmpty()) {
            explainer?.writeLog(Explainer.Loglevel.DEBUG, "[VanillaSearch/getNineItemStackArray] vanilla search skipped: input is empty")
            return null
        }
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
            } ?: run {
                explainer?.writeLog(Explainer.Loglevel.DEBUG, "[VanillaSearch/getNineItemStackArray] vanilla search skipped: input does not fit a 3x3 grid anchored at $minCoordinate, inputs=${mapped.keys.sortedBy { it.toIndex() }}")
                return null
            }
        return list.toTypedArray()
    }
}

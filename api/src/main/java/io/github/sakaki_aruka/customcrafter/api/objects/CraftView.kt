package io.github.sakaki_aruka.customcrafter.api.objects

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.inventory.ItemStack

/**
 * A view of crafting gui.
 *
 * @param[materials] A mapping of materials what were placed by a player.
 * @param[result] An item that is placed the result item slot.
 */

data class CraftView (
    val materials: Map<CoordinateComponent, ItemStack>,
    val result: ItemStack
) {
    /**
     * Returns a decremented view what calculated by specified arguments.
     *
     * @param[shiftUsed] Consider to using Shift key or not
     * @param[recipe] Recipe
     * @param[relations] Relations with `this.materials` and `recipe.items`
     * @return[CraftView] Decremented view
     * @since 5.0.16
     */
    fun getDecremented(
        shiftUsed: Boolean,
        recipe: CRecipe,
        relations: MappedRelation
    ): CraftView {
        val minAmountWithoutMass: Int = recipe.getMinAmount(
            map = this.materials, relation = relations, shift = shiftUsed, withoutMass = true, includeAir = false
        )

        val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        for ((r, i) in relations.components) {
            val matter: CMatter = recipe.items.getValue(r)
            val input: ItemStack = this.materials[i] ?: continue
            val xLimit: Int = minAmountWithoutMass / matter.amount

            val decrementAmount: Int =
                if (matter.mass) 1
                else
                    if (shiftUsed) xLimit * matter.amount
                    else matter.amount

            if (input.amount - decrementAmount < 1) {
                map[i] = ItemStack.empty()
            } else {
                map[i] = input.clone().asQuantity(input.amount - decrementAmount)
            }
        }

        return CraftView(materials = map.toMap(), result = this.result.clone())
    }

    /**
     * Returns a cloned view
     *
     * @return[CraftView] view of cloned
     * @since 5.0.16
     */
    fun clone(): CraftView {
        val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        this.materials.forEach { (component, item) -> map[component] = item.clone() }
        return CraftView(materials = map.toMap(), result = this.result.clone())
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

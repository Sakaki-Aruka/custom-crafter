package com.github.sakakiaruka.customcrafter.customcrafter.api.search

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

object VanillaSearch {
    fun search(player: Player, inventory: Inventory, one: Boolean) {
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory) ?: return
        val nineArray: Array<ItemStack> = getNineItemStackArray(mapped) ?: return
        Bukkit.getCraftingRecipe(nineArray, player.world)?.let { recipe ->
            val min: Int = nineArray.filter { it.type != Material.AIR }.minOf { it.amount }
            val amount: Int = if (one) 1 else min
            val result: ItemStack = recipe.result.asQuantity(amount)
            decrementCraftingSlotMaterials(inventory, mapped.keys, amount)
            val already: ItemStack? = inventory.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT)
            if (already == null || already.type == Material.AIR) {
                inventory.setItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT, result)
            } else if (already.asOne().isSimilar(result.asOne())) {
                if (already.amount + amount < already.type.maxStackSize) already.amount += amount
                else {
                    player.inventory.addItem(result).forEach { (_, overflown) ->
                        player.world.dropItem(player.location, overflown) { item ->
                            item.owner = player.uniqueId
                        }
                    }

                }
            }
        }

    }

    private fun decrementCraftingSlotMaterials(inventory: Inventory, slots: Set<CoordinateComponent>, amount: Int) {
        slots.forEach {
            val slot: Int = it.x + it.y * 9
            inventory.getItem(slot)?.let { item ->
                if (item.type != Material.AIR) item.amount = max(item.amount - amount, 0)
            }
        }
    }

    private fun getNineItemStackArray(mapped: Map<CoordinateComponent, ItemStack>): Array<ItemStack>? {
        if (mapped.isEmpty()) return null
        val start: CoordinateComponent = getUpperLeftCorner(mapped.keys)
        val end: CoordinateComponent = getBottomRightCorner(start)
        val result: Array<ItemStack> = Array(9) { ItemStack.empty() }
        Converter.getAvailableCraftingSlots()
            .filter { c -> (start.x..end.x).contains(c.x) && (start.y..end.y).contains(c.y) }
            .forEachIndexed { index, coordinate -> result[index] = mapped.getOrDefault(coordinate, ItemStack.empty()) }
        return result
    }

    private fun getUpperLeftCorner(keys: Set<CoordinateComponent>): CoordinateComponent {
        val left: CoordinateComponent = keys.minBy { it.x }
        val upper: CoordinateComponent = keys.minBy { it.y }
        return if (left.y == upper.y) left else CoordinateComponent(left.x, upper.y)
        // left.y == upper.y or left.y < upper.y
    }

    private fun getBottomRightCorner(left: CoordinateComponent): CoordinateComponent {
        // 3 * 3 ItemStack matrix
        return CoordinateComponent(min(6 - 1, left.x + 2), min(9 - 1, left.y + 2))
    }
}
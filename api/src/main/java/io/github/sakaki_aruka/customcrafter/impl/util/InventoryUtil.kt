package io.github.sakaki_aruka.customcrafter.impl.util

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object InventoryUtil {
    /**
     * Gives items to a player
     * @param[saveLimit] Considers `Material.maxStackSize` or not
     * @param[items] Items
     */
    fun Player.giveItems(
        saveLimit: Boolean = true,
        vararg items: ItemStack
    ) {
        items.forEach { item: ItemStack ->
            if (item.amount <= item.type.maxStackSize || !saveLimit) {
                addToInventory(item, this)
            } else {
                val one: ItemStack = item.asOne()
                val q: Int = item.amount / item.type.maxStackSize
                val r: Int = item.amount % item.type.maxStackSize
                repeat(q) {
                    addToInventory(one.asQuantity(item.type.maxStackSize), this)
                }
                addToInventory(one.asQuantity(r), this)
            }
        }
    }

    private fun addToInventory(
        item: ItemStack,
        player: Player
    ) {
        player.inventory.addItem(item).forEach { (_, over) ->
            if (!over.isEmpty) {
                player.world.dropItem(player.location, over)
            }
        }
    }
}
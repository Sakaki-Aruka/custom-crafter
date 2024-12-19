package io.github.sakaki_aruka.customcrafter.api.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.processor.Converter
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * @suppress
 */
object InventoryCloseListener: Listener {
    @EventHandler
    fun InventoryCloseEvent.onClose() {
        if (!CustomCrafterAPI.isCustomCrafterGUI(inventory)) return
        val world: World = player.world
        val location: Location = player.location
        Converter.getAvailableCraftingSlotIndices().forEach { slot ->
            inventory.getItem(slot)?.let { item ->
                player.inventory.addItem(item).forEach { (_, over) ->
                    world.dropItem(location, over)
                }
            }
        }
        inventory.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT)?.let { result ->
            player.inventory.addItem(result).forEach { (_, over) ->
                player.world.dropItem(player.location, over)
            }
        }
    }
}
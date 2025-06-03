package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * @suppress
 */
object InventoryCloseListener: Listener {
    @EventHandler
    fun InventoryCloseEvent.onClose() {
        CustomCrafterGUI.getGUI(this.inventory)?.onClose(this)
    }
}
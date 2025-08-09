package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory

/**
 * @suppress
 */
object InventoryClickListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClick() {
        val clicked: Inventory = clickedInventory ?: return

        clicked.holder?.let { holder ->
            (holder as? CustomCrafterUI)?.let { ui ->
                ui.onClick(clicked, this)
                return
            }
        }

        val top: Inventory = whoClicked.openInventory.topInventory
        if (clicked is PlayerInventory && top.holder != null && top.holder is CustomCrafterUI) {
            (top.holder as CustomCrafterUI).onPlayerInventoryClick(clicked, this)
        }
    }
}
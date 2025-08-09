package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import io.github.sakaki_aruka.customcrafter.internal.gui.OldWarnGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.ReactionProvider
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
        if (clicked is PlayerInventory && top is CustomCrafterUI) {
            top.onPlayerInventoryClick(clicked, this)
        }
    }

    private fun isOld(gui: Inventory): Boolean {
        val (key, type, _) = CustomCrafterAPI.genCCKey()
        val time: Long = gui.contents
            .filterNotNull()
            .firstOrNull { item -> item.itemMeta.persistentDataContainer.has(key, type) }
            ?.let { i -> i.itemMeta.persistentDataContainer.get(key, type) }
            ?: return false
        return time < CustomCrafter.INITIALIZED
    }
}
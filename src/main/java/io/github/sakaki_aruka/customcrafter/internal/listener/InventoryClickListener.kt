package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.OldWarnGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.ReactionProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * @suppress
 */
object InventoryClickListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClick() {
        val clicked: Inventory = clickedInventory ?: return
        val top: Inventory = whoClicked.openInventory.topInventory
        val bottom: Inventory = whoClicked.openInventory.bottomInventory

        val gui: CustomCrafterGUI = CustomCrafterGUI.getGUI(top) ?: return
        if (isOld(top) || isOld(bottom)) {
            whoClicked.openInventory(OldWarnGUI.getPage())
            return
        } else {
            (gui as? ReactionProvider)?.eventReaction(this, gui, clicked, isTopInventory = clicked == top)
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
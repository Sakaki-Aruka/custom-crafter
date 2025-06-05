package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory

/**
 * @suppress
 */
object PlayerInteractListener: Listener {
    @EventHandler
    fun PlayerInteractEvent.onInteract() {

        val inv: Inventory = PageOpenTrigger.getGUI(this)
            ?.takeIf { gui -> gui is PageOpenTrigger }
            ?.let { gui -> (gui as PageOpenTrigger).getFirstPage(this) }
            ?: return
        isCancelled = true
        player.openInventory(inv)
    }
}
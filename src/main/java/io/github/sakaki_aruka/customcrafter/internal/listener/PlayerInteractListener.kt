package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.AutoCraftUI
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitRunnable

/**
 * @suppress
 */
object PlayerInteractListener: Listener {
    @EventHandler
    fun PlayerInteractEvent.onInteract() {

        //debug
        if (this.clickedBlock != null
            && this.action.isRightClick
            && this.clickedBlock!!.type == Material.CRAFTER
            ) {
            object: BukkitRunnable() {
                override fun run() {
                    this@onInteract.player.openInventory(
                        AutoCraftUI(
                            this@onInteract.clickedBlock!!,
                            this@onInteract.player
                        ).inventory)
                }
            }.runTaskLater(CustomCrafter.getInstance(), 1L)

        }

        val inv: Inventory = PageOpenTrigger.getGUI(this)
            ?.takeIf { gui -> gui is PageOpenTrigger }
            ?.let { gui -> (gui as PageOpenTrigger).getFirstPage(this) }
            ?: return
        isCancelled = true
        player.openInventory(inv)
    }
}
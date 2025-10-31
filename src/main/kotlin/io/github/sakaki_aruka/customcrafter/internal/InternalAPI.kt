package io.github.sakaki_aruka.customcrafter.internal

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryHolder

internal object InternalAPI {

    fun shutdown() {
        for (player in Bukkit.getOnlinePlayers()) {
            val holder: InventoryHolder = player.openInventory.topInventory.holder ?: continue
            if (holder is CustomCrafterUI) {
                holder.onClose(
                    InventoryCloseEvent(player.openInventory)
                )
                player.closeInventory()
            }
        }
    }

    fun warn(str: String) = CustomCrafter.getInstance().logger.warning(str)
    fun info(str: String) = CustomCrafter.getInstance().logger.info(str)
}
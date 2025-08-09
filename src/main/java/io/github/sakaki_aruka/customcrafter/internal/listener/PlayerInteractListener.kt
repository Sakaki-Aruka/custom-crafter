package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

/**
 * @suppress
 */
object PlayerInteractListener: Listener {
    @EventHandler
    fun PlayerInteractEvent.onInteract() {
        CustomCrafterUI.InteractTriggered.PAGES
            .firstOrNull { t -> t.isTrigger(this) }
            ?.open(this)
    }
}
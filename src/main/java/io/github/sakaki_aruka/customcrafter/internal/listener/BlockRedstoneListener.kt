package io.github.sakaki_aruka.customcrafter.internal.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent

object BlockRedstoneListener: Listener {
    @EventHandler
    fun BlockRedstoneEvent.on() {
        NoPlayerListener.runMatchFunc(this)
    }
}
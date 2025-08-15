package io.github.sakaki_aruka.customcrafter.internal.event

import org.bukkit.block.Crafter
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AutoCraftPowerOnEvent internal constructor(
    val crafter: Crafter
): Event() {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

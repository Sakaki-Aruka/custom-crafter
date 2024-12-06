package com.github.sakakiaruka.customcrafter.customcrafter.api.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.Inventory

class CreateCustomItemEvent internal constructor(val player: Player, val input: Inventory): Event() {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = HANDLER_LIST
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.listener

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent


object InventoryClickListener: Listener {
    @EventHandler
    fun InventoryClickEvent.onClick() {
        val player: Player = Bukkit.getPlayer(whoClicked.uniqueId) ?: return
        //
    }
}
package com.github.sakakiaruka.customcrafter.customcrafter.listener

import com.github.sakakiaruka.customcrafter.customcrafter.event.CreateCustomItemEvent
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistory
import com.github.sakakiaruka.customcrafter.customcrafter.util.HistoryUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object CreateCustomItem: Listener {
    @EventHandler
    fun onCreate(e: CreateCustomItemEvent) {
        HistoryUtil.addHistory(CraftHistory(e.player, e.recipe, e.made))

        //debug
        println("add create info=${CraftHistory(e.player, e.recipe, e.made)}")
    }
}
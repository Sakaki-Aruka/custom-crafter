package com.github.sakakiaruka.customcrafter.customcrafter.event

import com.github.sakakiaruka.customcrafter.customcrafter.`object`.Recipe.Recipe
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

data class CreateCustomItemEvent(val player: Player, val recipe: Recipe, val made: ItemStack): Event() {
    companion object {
        @JvmField
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = HANDLER_LIST
}
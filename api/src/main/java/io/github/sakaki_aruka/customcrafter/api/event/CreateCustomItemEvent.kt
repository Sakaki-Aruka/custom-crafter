package io.github.sakaki_aruka.customcrafter.api.event

import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.search.Search
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a player did craft items what through custom crafting table.
 *
 * @param[player] A crafter
 * @param[view] A view of crafting gui
 * @param[result] A result of crafting
 * @param[shiftUsed] Shift-key used or not (since 5.0.17)
 * @param[isAsync] Called from async or not (since 5.0.17)
 */

class CreateCustomItemEvent (
    val player: Player,
    val view: CraftView,
    val result: Search.SearchResult?,
    val shiftUsed: Boolean,
    val isAsync: Boolean
): Event() {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = HANDLER_LIST
}
package io.github.sakaki_aruka.customcrafter.api.event.failure

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a player attempts to start a new craft while a craft process
 * (search or result generation) is already in progress, preventing the new attempt.
 *
 * @param[player] The player who attempted the double craft
 * @param[isAsync] Called from async or not
 * @since 5.0.21
 */
class PreventDoubleCraftEvent(
    val player: Player,
    isAsync: Boolean = false
): Event(isAsync) {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}

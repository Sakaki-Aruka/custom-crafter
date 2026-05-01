package io.github.sakaki_aruka.customcrafter.event.failure

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a player interacts with craft input slots or closes the CraftUI
 * while a craft process (search or result generation) is in progress,
 * causing the ongoing process to be interrupted.
 *
 * @param[interrupter] The player who caused the interruption
 * @param[isAsync] Called from async or not
 * @since 5.0.21
 */
class CraftInputInterruptEvent(
    val interrupter: Player,
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

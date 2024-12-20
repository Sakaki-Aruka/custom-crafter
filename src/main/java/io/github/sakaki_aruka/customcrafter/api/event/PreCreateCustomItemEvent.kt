package io.github.sakaki_aruka.customcrafter.api.event

import io.github.sakaki_aruka.customcrafter.api.`object`.CraftView
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.inventory.ClickType

/**
 * Called when a player click the making button and if crafting material slots are not empty.
 *
 * If you cancel this event, [CreateCustomItemEvent] is not fired.
 *
 * @param[player] A clicked player
 * @param[view] A view of crafting gui
 * @param[clickType] A click type what is player did
 */

class PreCreateCustomItemEvent internal constructor(
    val player: Player,
    val view: CraftView,
    val clickType: ClickType
): Event(), Cancellable {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    private var cancelled: Boolean = false
    override fun getHandlers(): HandlerList = HANDLER_LIST
    override fun isCancelled(): Boolean = cancelled
    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }
}
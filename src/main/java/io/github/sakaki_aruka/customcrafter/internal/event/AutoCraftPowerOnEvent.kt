package io.github.sakaki_aruka.customcrafter.internal.event

import org.bukkit.block.Crafter
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.jetbrains.annotations.ApiStatus

/**
 * This event is called when a crafter what is registered auto-craft-recipe receives redstone signal.
 *
 * Only for internal.
 * @since 5.0.12
 */
@ApiStatus.Internal
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

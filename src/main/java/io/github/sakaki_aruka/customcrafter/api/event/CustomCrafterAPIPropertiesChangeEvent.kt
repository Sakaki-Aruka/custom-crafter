package io.github.sakaki_aruka.customcrafter.api.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CustomCrafterAPIPropertiesChangeEvent<T>(
    val propertyName: String,
    val old: Property<T>,
    val new: Property<T>,
    val fromStandardCommand: Boolean
): Event() {
    class Property<T> internal constructor(
        val value: T
    )

    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}

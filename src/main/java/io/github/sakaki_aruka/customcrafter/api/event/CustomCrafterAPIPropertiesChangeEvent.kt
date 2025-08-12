package io.github.sakaki_aruka.customcrafter.api.event

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when [CustomCrafterAPI] changeable properties changed. (Only changed. When an old value is same with new value, this won't be called this.)
 *
 * (External plugins cannot initialize this instance.)
 *
 * @param[propertyName] Name of property
 * @param[old] An old value
 * @param[new] A new value
 * @since[5.0.11]
 */
class CustomCrafterAPIPropertiesChangeEvent<T> internal constructor(
    val propertyName: String,
    val old: Property<T>,
    val new: Property<T>,
): Event() {

    /**
     * A value of [CustomCrafterAPI] property.
     *
     * `T` is a type of instance contains value.
     *
     * @param[value] Value
     * @since 5.0.11
     */
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

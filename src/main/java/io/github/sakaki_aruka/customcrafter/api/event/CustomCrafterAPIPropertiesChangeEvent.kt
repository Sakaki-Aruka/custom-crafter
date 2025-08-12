package io.github.sakaki_aruka.customcrafter.api.event

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when [CustomCrafterAPI] changeable properties changed. (Only changed. When an old value is same with new value, this won't be called this.)
 *
 * (External plugins cannot initialize this instance.)
 *
 * ```Kotlin
 * // Example Code (Safe / Kotlin)
 * object PropertiesChangeListener: Listener {
 *   fun <T> CustomCrafterAPIPropertiesChangeEvent<T>.onChange() {
 *     val baseKey = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK
 *     if (this.propertyName != baseKey.name) {
 *       return
 *     }
 *     val baseTypeOld: Material = this.old.getOrNull(baseKey) ?: return
 *     CustomCrafterAPI.setBaseBlock(baseTypeOld)
 *     println("I rejected new setting... Hahaha!!!")
 *   }
 * }
 * ```
 * ```Kotlin
 * // Example Code (Danger / Kotlin)
 * object PropertiesChangeListener: Listener {
 *   fun <T> CustomCrafterAPIPropertiesChangeEvent<T>.onChange() {
 *     if (this.propertyName != "BASE_BLOCK") {
 *       return
 *     }
 *     CustomCrafterAPI.setBaseBlock(this.old.value as Material)
 *     println("I rejected new setting... Hahaha!!!")
 *   }
 * }
 * ```
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
    ) {
        @SuppressWarnings("unchecked")
        fun <S> getOrNull(key: PropertyKey<S>): S? {
            return value as? S
        }
    }

    class PropertyKey<T> internal constructor(
        val name: String
    ) {
        companion object {
            val RESULT_GIVE_CANCEL = PropertyKey<Boolean>("RESULT_GIVE_CANCEL")
            val BASE_BLOCK = PropertyKey<Material>("BASE_BLOCK")
            val USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = PropertyKey<Boolean>("USE_MULTIPLE_CANDIDATE_FEATURE")
            val USE_AUTO_CRAFTING_FEATURE = PropertyKey<Boolean>("USE_AUTO_CRAFTING_FEATURE")
            val BASE_BLOCK_SIDE = PropertyKey<Int>("BASE_BLOCK_SIDE")
        }
    }

    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}

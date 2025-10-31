package io.github.sakaki_aruka.customcrafter.api.event

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when [CustomCrafterAPI] changeable properties changed.
 *
 * Only changed. When an old value is same with new value, this won't be called this.
 *
 * (External plugins cannot initialize this instance.)
 *
 * ```Kotlin
 * // Example Code (Safe / Kotlin)
 * object PropertiesChangeListener: Listener {
 *     @EventHandler
 *     fun <T> CustomCrafterAPIPropertiesChangeEvent<T>.onChange() {
 *         val baseKey = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK
 *         if (this.propertyName != baseKey.name) {
 *             return
 *         }
 *         val baseTypeOld: Material = this.oldValue.getOrNull(baseKey) ?: return
 *         CustomCrafterAPI.setBaseBlock(baseTypeOld)
 *         println("I rejected new setting... Hahaha!!!")
 *     }
 * }
 * ```
 * ```Java
 * // Example Code (Safe / Java)
 * public class PropertiesChangeListener implements Listener {
 *     @EventHandler
 *     public void <T> onChange(CustomCrafterAPIPropertiesChangeEvent<T> event) {
 *         String baseKey = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.Companion.BASE_BLOCK;
 *         if (!event.getPropertyName().equals(baseKey.getName())) {
 *             return;
 *         }
 *         Material baseTypeOld = event.getOldValue().getOrNull(baseKey);
 *         if (baseTypeOld == null) {
 *             return;
 *         }
 *         CustomCrafterAPI.INSTANCE.setBaseBlock(baseTypeOld);
 *         System.out.println("I rejected new setting... Hahaha!!!");
 *     }
 * }
 * ```
 *
 * @param[propertyName] Name of property
 * @param[oldValue] An old value
 * @param[newValue] A new value
 * @since[5.0.11]
 */
class CustomCrafterAPIPropertiesChangeEvent<T> internal constructor(
    val propertyName: String,
    val oldValue: Property<T>,
    val newValue: Property<T>,
    isAsync: Boolean = false
): Event(isAsync) {

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
        /**
         * Returns `Property<T>` value in safety.
         *
         * If provided key mismatches with `T`, this returns `null`.
         *
         * @param[key] Key of a Property
         * @return[S] Contained value
         * @see[PropertyKey]
         * @since 5.0.11
         */
        @SuppressWarnings("unchecked")
        fun <S> getOrNull(key: PropertyKey<S>): S? {
            return value as? S
        }
    }

    /**
     * Key of `Property<T>`
     *
     * @param[name] Name of a Property
     * @param[T] Type of a Property value
     * @see[Property.getOrNull]
     * @see[CustomCrafterAPIPropertiesChangeEvent]
     * @since 5.0.11
     */
    class PropertyKey<T> internal constructor(
        val name: String
    ) {
        companion object {
            val RESULT_GIVE_CANCEL = PropertyKey<Boolean>("RESULT_GIVE_CANCEL")
            val BASE_BLOCK = PropertyKey<Material>("BASE_BLOCK")
            val USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = PropertyKey<Boolean>("USE_MULTIPLE_CANDIDATE_FEATURE")
            val USE_CUSTOM_CRAFT_UI = PropertyKey<Boolean>("USE_CUSTOM_CRAFT_UI")
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

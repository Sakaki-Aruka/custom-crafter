package io.github.sakaki_aruka.customcrafter.event

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.ui.AllCandidateUIDesigner
import io.github.sakaki_aruka.customcrafter.ui.CraftUIDesigner
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID
import java.util.function.BiFunction

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
 *         val sideKey = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE
 *         if (this.propertyName != sideKey.name) {
 *             return
 *         }
 *         val sideOld: Int = this.oldValue.getOrNull(sideKey) ?: return
 *         CustomCrafterAPI.setBaseBlockSideSize(sideOld)
 *         println("I rejected new setting... Hahaha!!!")
 *     }
 * }
 * ```
 * ```Java
 * // Example Code (Safe / Java)
 * public class PropertiesChangeListener implements Listener {
 *     @EventHandler
 *     public void <T> onChange(CustomCrafterAPIPropertiesChangeEvent<T> event) {
 *         CustomCrafterAPIPropertiesChangeEvent.PropertyKey<Integer> sideKey = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE;
 *         if (!event.getPropertyName().equals(sideKey.getName())) {
 *             return;
 *         }
 *         Integer sideOld = event.getOldValue().getOrNull(sideKey);
 *         if (sideOld == null) {
 *             return;
 *         }
 *         CustomCrafterAPI.setBaseBlockSideSize(sideOld);
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
class CustomCrafterAPIPropertiesChangeEvent<T> (
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
        @Suppress("UNCHECKED_CAST")
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
            @JvmField
            val RESULT_GIVE_CANCEL = PropertyKey<Boolean>("RESULT_GIVE_CANCEL")
            @JvmField
            val USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = PropertyKey<Boolean>("USE_MULTIPLE_CANDIDATE_FEATURE")
            @JvmField
            val USE_CUSTOM_CRAFT_UI = PropertyKey<Boolean>("USE_CUSTOM_CRAFT_UI")
            @JvmField
            val BASE_BLOCK_SIDE = PropertyKey<Int>("BASE_BLOCK_SIDE")
            @JvmField
            val CRAFT_UI_DESIGNER = PropertyKey<CraftUIDesigner>("CRAFT_UI_DESIGNER")
            @JvmField
            val ALL_CANDIDATE_UI_DESIGNER = PropertyKey<AllCandidateUIDesigner>("ALL_CANDIDATE_UI_DESIGNER")
            @JvmField
            val BASE_BLOCK_CHECKER = PropertyKey<BiFunction<UUID, Set<Block>, Boolean>>("BASE_BLOCK_CHECKER")
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

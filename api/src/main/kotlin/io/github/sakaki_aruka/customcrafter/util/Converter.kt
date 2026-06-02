package io.github.sakaki_aruka.customcrafter.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * @suppress
 */
object Converter {

    /**
     * Kotlin string extension. Deserializes a MiniMessage string to a [Component] (net.kyori.adventure).
     *
     * ```kotlin
     * val component = "<aqua>This is a kyori component!".toComponent()
     * ```
     *
     * @return[Component] Deserialized component
     * @since 5.0.0
     */
    internal fun String.toComponent(): Component = MiniMessage.miniMessage().deserialize(this)

}
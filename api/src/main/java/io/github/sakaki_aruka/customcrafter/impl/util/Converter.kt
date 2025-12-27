package io.github.sakaki_aruka.customcrafter.impl.util

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object Converter {
    /**
     * ※ This is a high cost function.
     *
     * ```kotlin
     * val list = CoordinateComponent.squareFill(3)
     * println(Converter.getComponentsShapeString(list))
     * // -> OUTPUT
     * // ###
     * // ###
     * // ###
     * ```
     *
     * ```kotlin
     * val list = CoordinateComponent.square(3)
     * println(Converter.getComponentsShapeString(list))
     * // -> OUTPUT
     * // ###
     * // #_#
     * // ###
     * ```
     *
     * @param[list] List of coordinates
     * @param[existsSlotChar] This is a char what is used on coordinate exists. Default = `#`.
     * @param[notExistsSlotChar] This is a char what is used on coordinate NOT exists. Default = `_`.
     * @return[String] Shape
     * @since 5.0.16
     */
    @JvmStatic
    @JvmOverloads
    fun getComponentsShapeString(
        list: Collection<CoordinateComponent>,
        existsSlotChar: Char = '#',
        notExistsSlotChar: Char = '_'
    ): String {
        val minX = list.minOf { it.x }
        val minY = list.minOf { it.y }
        val maxX = list.maxOf { it.x }
        val maxY = list.maxOf { it.y }

        val builder = StringBuilder()
        (minY..maxY).forEach { y ->
            (minX..maxX).forEach { x ->
                builder.append(
                    if (list.contains(CoordinateComponent(x, y))) existsSlotChar
                    else notExistsSlotChar
                )
            }
            if (y != maxY) builder.append(System.lineSeparator())
        }
        return builder.toString()
    }

    /**
     * Kotlin string extension. String to Component (net.kyori.adventure)
     *
     * Usage
     * ```kotlin
     * val component = "<aqua>This is a kyori component!".toComponent()
     * ```
     */
    fun String.toComponent(): Component = MiniMessage.miniMessage().deserialize(this)

}
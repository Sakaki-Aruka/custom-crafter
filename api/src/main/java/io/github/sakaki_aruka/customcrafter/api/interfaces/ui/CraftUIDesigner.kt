package io.github.sakaki_aruka.customcrafter.api.interfaces.ui

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * UI builder for CraftUI
 *
 * Default UI implementation is in CraftUI. (companion object)
 * @since 5.0.16
 */
interface CraftUIDesigner {
    /**
     * UI title
     * @param[context] Execution context
     * @return[Component] Component of UI title (net.kyori.adventure)
     * @since 5.0.16
     */
    fun title(context: Context): Component

    /**
     * Result slot coordinate
     * @param[context] Execution context
     * @return[CoordinateComponent] Coordinate of result slot
     * @since 5.0.16
     */
    fun resultSlot(context: Context): CoordinateComponent

    /**
     * Make button slot coordinate and make button item (icon)
     * @param[context] Execution context
     * @return[Pair] Pair of coordinate and item
     * @since 5.0.16
     */
    fun makeButton(context: Context): Pair<CoordinateComponent, ItemStack>

    /**
     * Blank slots coordinates and those item(icon)s
     * @param[context] Execution context
     * @return[Map] Relation of coordinate and item
     * @since 5.0.16
     */
    fun blankSlots(context: Context): Map<CoordinateComponent, ItemStack>

    /**
     * Context for [CraftUIDesigner].
     * @param[player] UI opener or null
     * @since 5.0.16
     */
    class Context (
        val player: Player? = null
    ) {
        override fun toString(): String {
            return "Context: Name=${player?.displayName()}, ID=${player?.uniqueId}"
        }
    }

    /**
     * BakedDesigner: An immutable CraftUIDesigner with a set (baked) value.
     *
     * ```kotlin
     * val designer: CraftUIDesigner = ~~~
     * val context: CraftUIDesigner.Context = ~~~
     * val baked: BakedDesigner = CraftUIDesigner.bake(designer, context)
     * ```
     *
     * @param[title] Title of UI
     * @param[result] Coordinate of result slot
     * @param[makeButton] Make button coordinate and icon (item)
     * @param[blankSlots] Unclickable slot coordinates and icons
     * @see[CraftUIDesigner.bake]
     * @since 5.0.16
     */
    class Baked internal constructor(
        val title: Component,
        val result: CoordinateComponent,
        val makeButton: Pair<CoordinateComponent, ItemStack>,
        val blankSlots: Map<CoordinateComponent, ItemStack>
    ) {
        /**
         * Applies designer to a provided ui (inventory).
         *
         * @param[ui] UI
         * @since 5.0.16
         */
        fun apply(ui: Inventory) {
            this.makeButton.let { (c, item) -> ui.setItem(c.toIndex(), item) }
            this.blankSlots.forEach { (c, item) -> ui.setItem(c.toIndex(), item) }
        }

        /**
         * Returns craft slots (6x6)
         *
         * @return[List] Craft slot coordinates list
         * @since 5.0.16
         */
        fun craftSlots(): List<CoordinateComponent> {
            return (0..<54).map { CoordinateComponent.fromIndex(it) }
                .minus(blankSlots.keys)
                .minus(result)
                .minus(makeButton.first)
        }

        /**
         * Returns result slot coordinate (Int)
         *
         * @return[Int] Slot index
         * @since 5.0.16
         */
        fun resultInt(): Int {
            return this.result.toIndex()
        }

        /**
         * Returns is this valid or not.
         *
         * @return[Result] Check result
         * @since 5.0.16
         */
        fun isValid(): Result<Unit> {
            val craftSlots: List<CoordinateComponent> = craftSlots()
            val leftTop: Int = craftSlots.minBy { it.toIndex() }.toIndex()

            if (craftSlots.size != 36
                || !CoordinateComponent.squareFill(6).containsAll(craftSlots.map { CoordinateComponent.fromIndex(it.toIndex() - leftTop) })) {
                return Result.failure(IllegalStateException(
                    """
                        
                        CraftSlots must be 36 size and 6x6 square.
                        Current Slot Size: ${craftSlots.size}
                        Current Coordinates: ('_': Blank, Result or MakeButton Slots, '#': Craft Slots)
                    """.trimIndent()
                            + System.lineSeparator()
                            + Converter.getComponentsShapeString(
                        (0..<54).map { CoordinateComponent.fromIndex(it) }
                            .minus(craftSlots.toSet()))
                ))
            }

            if (blankSlots.values.any { it.type.isAir }) {
                return Result.failure(IllegalStateException("'blankSlots' must not contain any 'Material#isAir' icons."))
            }

            return Result.success(Unit)
        }
    }

    companion object {
        /**
         * Bake a specified designer
         * @suppress
         * @param[designer] UI designer
         * @param[context] Context for baking
         * @return[Baked] Baked designer
         * @since 5.0.16
         */
        internal fun bake(
            designer: CraftUIDesigner,
            context: Context
        ): Baked {
            return Baked(
                title = designer.title(context),
                result = designer.resultSlot(context),
                makeButton = designer.makeButton(context),
                blankSlots = designer.blankSlots(context)
            )
        }
    }
}
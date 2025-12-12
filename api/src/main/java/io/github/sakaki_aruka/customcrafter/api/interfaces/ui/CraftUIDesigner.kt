package io.github.sakaki_aruka.customcrafter.api.interfaces.ui

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface CraftUIDesigner {
    fun title(context: Context): Component
    fun resultSlot(context: Context): CoordinateComponent
    fun makeButton(context: Context): Pair<CoordinateComponent, ItemStack>
    fun blankSlots(context: Context): Map<CoordinateComponent, ItemStack>

    class Context internal constructor(
        val player: Player?
    )

    class BakedDesigner(
        val title: Component,
        val result: Pair<CoordinateComponent, ItemStack>,
        val makeButton: Pair<CoordinateComponent, ItemStack>,
        val blankSlots: Map<CoordinateComponent, ItemStack>
    ) {
        fun apply(ui: Inventory) {
            ui.setItem(this.resultInt(), this.result.second)
            this.makeButton.let { (c, item) -> ui.setItem(c.toIndex(), item) }
            this.blankSlots.forEach { (c, item) -> ui.setItem(c.toIndex(), item) }
        }

        fun craftSlots(): List<CoordinateComponent> {
            return Converter.getAvailableCraftingSlotComponents()
                .minus(blankSlots.keys)
                .minus(result.first)
                .minus(makeButton.first)
        }

        fun resultInt(): Int {
            return this.result.first.toIndex()
        }

        fun isValid(): Boolean {
            val craftSlots: List<CoordinateComponent> = craftSlots()
            val leftTop: CoordinateComponent = craftSlots.minBy { it.toIndex() }

            if (craftSlots.size != 36) return false
            if (leftTop.y != 0) return false
            if (leftTop.x > 3) return false // width <=5, can not use 6 x 6
            return CoordinateComponent.squareFill(size = 6, dx = leftTop.x, dy = 0)
                .containsAll(craftSlots.toSet())
        }
    }

    companion object {
        fun bake(
            designer: CraftUIDesigner,
            context: Context
        ): BakedDesigner {
            return BakedDesigner(
                title = designer.title(context),
                result = designer.resultSlot(context) to ItemStack.empty(),
                makeButton = designer.makeButton(context),
                blankSlots = designer.blankSlots(context)
            )
        }

        fun apply(designer: CraftUIDesigner, ui: Inventory, context: Context) {
            ui.setItem(designer.resultSlot(context).toIndex(), ItemStack.empty())
            designer.makeButton(context).let { (c, item) -> ui.setItem(c.toIndex(), item) }
            designer.blankSlots(context).forEach { (c, item) -> ui.setItem(c.toIndex(), item) }
        }
    }
}
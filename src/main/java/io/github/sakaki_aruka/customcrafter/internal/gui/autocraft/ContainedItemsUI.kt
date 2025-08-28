package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.TestOnly
import java.util.Collections

internal class ContainedItemsUI(
    private val cBlock: CBlock,
): CustomCrafterUI.Static, InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(
        this,
        54,
        "<b>Contained Items".toComponent()
    )

    private val ignoreSlots: Set<Int>

    init {

        val recipe: AutoCraftRecipe = this.cBlock.getRecipe()!!
        for (i in (0..<54) - recipe.items.keys.map { c -> c.toIndex() }) {
            this.inventory.setItem(i, BLANK)
        }

        this.ignoreSlots = (Converter.getAvailableCraftingSlotIndices() - recipe.items.keys.map { c -> c.toIndex() }).toSet()

        val containedItems: List<ItemStack> = this.cBlock.getContainedItems()
        recipe.items.keys.map { c -> c.toIndex() }.sorted().withIndex().forEach { (index, slot) ->
            this.inventory.setItem(slot, containedItems.getOrNull(index) ?: ItemStack.empty())
        }
    }

    companion object {
        val CLICKABLE_SLOTS: Set<Int> = Converter.getAvailableCraftingSlotIndices()
        val BLANK: ItemStack =ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.empty())
            }
        }

        val UI_LIST: MutableSet<ContainedItemsUI> = Collections.synchronizedSet(mutableSetOf())

        private fun getUIOrNull(block: Block): ContainedItemsUI? {
            val cloned: Set<ContainedItemsUI> = synchronized(UI_LIST) {
                UI_LIST.toSet()
            }

            return cloned.firstOrNull { ui -> ui.cBlock.block.location == block.location }
        }

        private fun addUI(ui: ContainedItemsUI) {
            synchronized(UI_LIST) {
                UI_LIST.add(ui)
            }
        }

        private fun removeUI(ui: ContainedItemsUI) {
            synchronized(UI_LIST) {
                UI_LIST.remove(ui)
            }
        }

        @TestOnly
        fun of(cBlock: CBlock): ContainedItemsUI {
            val ui = ContainedItemsUI(cBlock)
            addUI(ui)
            return ui
        }

        fun of(block: Block): ContainedItemsUI? {
            getUIOrNull(block)?.let { ui -> return ui }

            if (block.type != Material.CRAFTER || block.state !is Crafter) return null
            val cBlock: CBlock = CBlock.of(block.state as Crafter) ?: return null
            val ui = ContainedItemsUI(cBlock)
            addUI(ui)
            return ui
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        if ((event.inventory.viewers - event.player).isEmpty()) {
            val ui: ContainedItemsUI = event.inventory.holder as? ContainedItemsUI ?: return
            removeUI(ui)
        }
    }

    override fun getClickableType(slot: Int): CustomCrafterUI.ClickableType {
        return if (slot in CLICKABLE_SLOTS) CustomCrafterUI.ClickableType.DYNAMIC_TOGGLED
          else CustomCrafterUI.ClickableType.ALWAYS_UNCLICKABLE
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        event.isCancelled = true
        when (event.rawSlot) {
            in this.ignoreSlots -> return
            //
        }
    }

    override fun getInventory(): Inventory = this.inventory
}
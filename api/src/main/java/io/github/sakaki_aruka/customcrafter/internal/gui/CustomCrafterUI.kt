package io.github.sakaki_aruka.customcrafter.internal.gui

import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

internal interface CustomCrafterUI {

    companion object {
        val DEFAULT_PAGE: Inventory = Bukkit.createInventory(null, 54)

        val NEXT_BUTTON: ItemStack = ItemStack.of(Material.ENDER_PEARL).apply {
            itemMeta = itemMeta.apply {
                displayName("<b>NEXT".toComponent())
            }
        }

        val PREVIOUS_BUTTON: ItemStack = ItemStack.of(Material.ENDER_EYE).apply {
            itemMeta = itemMeta.apply {
                displayName("<b>PREVIOUS".toComponent())
            }
        }
    }

    fun onClose(event: InventoryCloseEvent) {}
    fun onClick(clicked: Inventory, event: InventoryClickEvent)
    fun onPlayerInventoryClick(clicked: Inventory, event: InventoryClickEvent) {
        event.isCancelled = true
    }

    interface Pageable: CustomCrafterUI {
        var currentPage: Int

        fun flipPage()
        fun canFlipPage(): Boolean
        fun flipBackPage()
        fun canFlipBackPage(): Boolean
    }

    interface InteractTriggered {
        companion object {
            val PAGES: Set<InteractTriggered> = setOf(
                CraftUI
            )
        }
        fun isTrigger(event: PlayerInteractEvent): Boolean
        fun open(event: PlayerInteractEvent)
    }
}
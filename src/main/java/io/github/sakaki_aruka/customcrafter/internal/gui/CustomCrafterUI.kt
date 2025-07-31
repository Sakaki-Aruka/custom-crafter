package io.github.sakaki_aruka.customcrafter.internal.gui

import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

internal sealed interface CustomCrafterUI {

    companion object {
        val DEFAULT_PAGE: Inventory = Bukkit.createInventory(null, 54)
    }

    enum class ClickableType {
        ALWAYS_CLICKABLE,
        ALWAYS_UNCLICKABLE,
        DYNAMIC_TOGGLED
    }

    fun onClose(event: InventoryCloseEvent)
    fun onClick(event: InventoryClickEvent)

    interface Pageable: CustomCrafterUI {
        val currentPage: Int

        fun flipPage()
        fun canFlipPage()
        fun flipBackPage()
        fun canFlipBackPage()
    }

    interface Static: CustomCrafterUI {
        fun getClickableType(slot: Int): ClickableType
    }
}
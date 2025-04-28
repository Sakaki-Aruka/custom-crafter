package io.github.sakaki_aruka.customcrafter.internal.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

internal interface ReactionProvider {
    fun eventReaction(
        event: InventoryClickEvent,
        ui: CustomCrafterGUI,
        inventory: Inventory
    )
}
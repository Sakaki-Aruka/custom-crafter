package io.github.sakaki_aruka.customcrafter.internal.gui

import org.bukkit.event.Event
import org.bukkit.inventory.Inventory

internal interface PageOpenTrigger {
    fun <T: Event> predicate(event: T): CustomCrafterGUI?
    fun <T: Event> getFirstPage(event: T): Inventory?
}
package io.github.sakaki_aruka.customcrafter.internal.gui

import org.bukkit.event.Event
import org.bukkit.inventory.Inventory

/**
 *
 * @since 5.0.10
 */
internal interface PageOpenTrigger {
    fun <T: Event> getFirstPage(event: T): Inventory?

    companion object {
        fun <T: Event> getGUI(event: T): CustomCrafterGUI? {
            return PredicateProvider.PROVIDERS
                .filterIsInstance<PredicateProvider<out CustomCrafterGUI>>()
                .firstNotNullOfOrNull { i -> i.predicate(event) }
        }
    }
}
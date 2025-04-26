package io.github.sakaki_aruka.customcrafter.internal.gui

import org.bukkit.event.Event
import org.bukkit.inventory.Inventory
import kotlin.reflect.full.companionObjectInstance

/**
 *
 * @since 5.0.10
 */
internal interface PageOpenTrigger {
    fun <T: Event> getFirstPage(event: T): Inventory?

    companion object {
        fun <T: Event> getGUI(event: T): CustomCrafterGUI? {
            return PredicateProvider.PROVIDERS
                .asSequence()
                .filter { c ->
                    c.companionObjectInstance != null
                            || c.objectInstance != null
                }
                .mapNotNull { c -> c.companionObjectInstance ?: c.objectInstance }
                .filterIsInstance<PredicateProvider<CustomCrafterGUI>>()
                .map { i -> i.predicate(event) }
                .firstOrNull()
        }
    }
}
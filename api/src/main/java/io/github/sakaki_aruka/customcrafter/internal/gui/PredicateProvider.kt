package io.github.sakaki_aruka.customcrafter.internal.gui

import org.bukkit.event.Event

/**
 * A class that implements PageOpenTrigger must implement this in whose companion object.
 * @since 5.0.10
 */
internal interface PredicateProvider<U> {
    fun <T: Event> predicate(event: T): U?
}
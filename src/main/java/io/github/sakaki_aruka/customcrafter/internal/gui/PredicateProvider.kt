package io.github.sakaki_aruka.customcrafter.internal.gui

import org.bukkit.event.Event
import kotlin.reflect.KClass

/**
 * A class that implements PageOpenTrigger must implement this in whose companion object.
 * @since 5.0.10
 */
interface PredicateProvider<U> {
    fun <T: Event> predicate(event: T): U?

    companion object {
        val PROVIDERS: MutableSet<KClass<*>> = mutableSetOf()
    }
}
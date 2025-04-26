package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.gui.PredicateProvider
import org.bukkit.event.Event

interface NoPlayerListener: PredicateProvider<Boolean> {
    companion object {
        private val LISTENERS: MutableSet<NoPlayerListener> = mutableSetOf()

        fun <T: Event> runMatchFunc(event: T) {
            LISTENERS.firstOrNull { listener ->
                listener.predicate(event) == true
            }?.func(event)
        }
    }

    fun <T: Event> func(event: T)
}
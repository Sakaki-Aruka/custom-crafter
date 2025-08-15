package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.gui.PredicateProvider
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent

internal interface NoPlayerListener: PredicateProvider<Boolean> {
    companion object: Listener {
        val LISTENERS: MutableSet<NoPlayerListener> = mutableSetOf()

        private fun <T: Event> runMatchFunc(event: T) {
            LISTENERS.firstOrNull { listener ->
                listener.predicate(event) == true
            }?.func(event)
        }


//        @EventHandler
//        fun InventoryMoveItemEvent.on() {
//            runMatchFunc(this)
//        }
    }

    fun <T: Event> func(event: T)
}
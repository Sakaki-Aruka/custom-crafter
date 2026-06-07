package io.github.sakaki_aruka.customcrafter.event

import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a recipe unregistered.
 *
 * @param[recipes] registered recipe.
 * @param[isAsync] Called from async or not (since 5.2.0)
 */
class UnregisterCustomRecipeEvent(
    val recipes: List<CRecipe>,
    isAsync: Boolean
): Event(isAsync) {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = HANDLER_LIST
}
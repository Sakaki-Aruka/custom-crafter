package io.github.sakaki_aruka.customcrafter.event

import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a recipe registered.
 *
 * @param[recipes] registered recipe.
 */
class RegisterCustomRecipeEvent(
    val recipes: List<CRecipe>
): Event() {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

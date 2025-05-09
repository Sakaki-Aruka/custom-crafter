package io.github.sakaki_aruka.customcrafter.api.event

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a recipe registered.
 *
 * @param[recipe] registered recipe.
 */
class RegisterCustomRecipeEvent(
    val recipe: CRecipe
): Event(), Cancellable {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    private var cancelled: Boolean = false
    override fun getHandlers(): HandlerList = HANDLER_LIST
    override fun isCancelled(): Boolean = cancelled
    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }
}

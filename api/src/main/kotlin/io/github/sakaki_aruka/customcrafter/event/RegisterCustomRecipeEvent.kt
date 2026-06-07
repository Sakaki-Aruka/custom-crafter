package io.github.sakaki_aruka.customcrafter.event

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.papermc.paper.plugin.configuration.PluginMeta
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a recipe registered.
 *
 * @param[recipes] registered recipe.
 * @param[registeredBy] A plugin metadata that registered recipes.
 * @param[isAsync] Called from async or not (since 5.2.0)
 */
class RegisterCustomRecipeEvent(
    val recipes: List<CRecipe>,
    private val registeredBy: PluginMeta,
    isAsync: Boolean
): Event(isAsync) {
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = HANDLER_LIST

    /**
     * Returns a plugin metadata that registered recipes.
     *
     * If registered by unspecified, returns null.
     * @return[PluginMeta] A plugin metadata
     * @since 5.2.0
     */
    fun registererMeta(): PluginMeta? {
        return this.registeredBy.takeIf { it.mainClass != CustomCrafter::class.java.canonicalName }
    }
}

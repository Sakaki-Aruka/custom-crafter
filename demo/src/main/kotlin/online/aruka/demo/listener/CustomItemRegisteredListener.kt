package online.aruka.demo.listener

import io.github.sakaki_aruka.customcrafter.api.event.RegisterCustomRecipeEvent
import online.aruka.demo.Demo
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object CustomItemRegisteredListener: Listener {
    @EventHandler
    fun RegisterCustomRecipeEvent.onRegister() {
        Demo.plugin.logger.info("New Custom Recipe Registered: ${this.recipe.name}")
    }
}
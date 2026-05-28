package online.aruka.custom_crafter.demo.listener

import io.github.sakaki_aruka.customcrafter.event.RegisterCustomRecipeEvent
import online.aruka.custom_crafter.demo.Demo
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object CustomItemRegisteredListener: Listener {
    @EventHandler
    fun RegisterCustomRecipeEvent.onRegister() {
        Demo.plugin.logger.info("New Custom Recipe Registered: ${this.recipes.joinToString(",") { it.name }}")
    }
}
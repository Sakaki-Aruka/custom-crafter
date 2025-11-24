package online.aruka.demo.listener

import io.github.sakaki_aruka.customcrafter.api.event.CustomCrafterAPIPropertiesChangeEvent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object CustomCrafterAPIPropertiesChangeListener: Listener {
    @EventHandler
    fun <T> CustomCrafterAPIPropertiesChangeEvent<T>.onChange() {
        val key = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK
        if (this.propertyName != key.name) {
            return
        }
        val new: Material = this.newValue.getOrNull(key) ?: return
        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendMessage("Custom Crafter base-block changed: (New) ${new.name}".toComponent())
        }
    }
}
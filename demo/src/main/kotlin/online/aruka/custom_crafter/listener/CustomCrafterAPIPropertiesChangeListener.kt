package online.aruka.custom_crafter.listener

import io.github.sakaki_aruka.customcrafter.event.CustomCrafterAPIPropertiesChangeEvent
import io.github.sakaki_aruka.customcrafter.util.Converter.toComponent
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
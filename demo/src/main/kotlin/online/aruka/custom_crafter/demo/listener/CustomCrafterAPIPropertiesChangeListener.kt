package online.aruka.custom_crafter.demo.listener

import io.github.sakaki_aruka.customcrafter.event.CustomCrafterAPIPropertiesChangeEvent
import net.kyori.adventure.text.minimessage.MiniMessage
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
            player.sendMessage(MiniMessage.miniMessage().deserialize("Custom Crafter base-block changed: (New) ${new.name}"))
        }
    }
}
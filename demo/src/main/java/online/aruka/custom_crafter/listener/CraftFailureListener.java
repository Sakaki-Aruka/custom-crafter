package online.aruka.custom_crafter.listener;

import io.github.sakaki_aruka.customcrafter.event.failure.CraftInputInterruptEvent;
import io.github.sakaki_aruka.customcrafter.event.failure.PreventDoubleCraftEvent;
import online.aruka.custom_crafter.Demo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class CraftFailureListener implements Listener {

    @EventHandler
    public void onCraftInputInterrupt(CraftInputInterruptEvent event) {
        Demo.plugin.getLogger().info(String.format(
                "[Craft Interrupt] Player: %s interrupted an ongoing craft.",
                event.getInterrupter().getName()
        ));
    }

    @EventHandler
    public void onPreventDoubleCraft(PreventDoubleCraftEvent event) {
        Demo.plugin.getLogger().info(String.format(
                "[Double Craft Prevented] Player: %s attempted to start a new craft while one is already running.",
                event.getPlayer().getName()
        ));
    }
}

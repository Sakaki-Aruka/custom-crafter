package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;

public class CloseCrafter implements Listener {
    public void onInventoryClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        if(guiOpening.containsKey(player)){
            guiOpening.remove(player);
        }
    }
}

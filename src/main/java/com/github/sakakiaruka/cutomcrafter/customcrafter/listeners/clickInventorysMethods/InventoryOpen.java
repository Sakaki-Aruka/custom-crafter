package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.ClickInventory.isTransitionMode;

public class InventoryOpen implements Listener {
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event){
        Player player = (Player) event.getPlayer();
        if(isTransitionMode.contains(player)){
            //debug
            System.out.println("isTransitionMode tag was removed.");

            isTransitionMode.remove(player);
        }
    }
}

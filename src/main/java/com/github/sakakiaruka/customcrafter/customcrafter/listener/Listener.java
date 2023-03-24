package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event){
        new CloseCraftingTable().onInventoryClose(event);
    }

    @EventHandler
    public void onInventoryModify(InventoryClickEvent event){
        new ModifyCraftingInventory().onInventoryClick(event);
    }

    @EventHandler
    public void onInventoryOpen(PlayerInteractEvent event){
        new OpenCraftingTable().onPlayerInteract(event);
    }
}

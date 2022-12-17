package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class Listeners implements Listener {
    @EventHandler
    public void closeInventory(InventoryCloseEvent event){
        new CloseCrafter().onInventoryClose(event);
    }

    @EventHandler
    public void openInventory(InventoryOpenEvent event){
        new OpenCrafter().onInventoryOpen(event);
    }

    @EventHandler
    public void clickInventory(InventoryClickEvent event){
        new ClickInventory().onInventoryClick(event);
    }
}

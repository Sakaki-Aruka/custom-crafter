package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.command.Check;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

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


    @EventHandler
    public void onPlayerConsumeItem(PlayerInteractEvent event) {
        new PlayerUseSpawnEgg().onPlayerUseSpawnEgg(event);
    }

    @EventHandler
    public void consume(PlayerUseUnknownEntityEvent event) {
        CustomCrafter.getInstance().getLogger().info(event.toString());
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        new SpawnerSpawn().onSpawnerSpawn(event);
    }
}

package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class MobSpawn implements Listener {
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!event.getEntity().getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) return;
        //debug
        Bukkit.getLogger().info("mob spawned location=" + event.getEntity().getLocation());
        Bukkit.getLogger().info("mob spawned event fired location = " + event.getLocation());
        Bukkit.getLogger().info("mob spawned location (int)=" +
                event.getEntity().getLocation().getBlockX() + "," +
                event.getEntity().getLocation().getBlockY() + "," +
                event.getEntity().getLocation().getBlockZ());
    }
}

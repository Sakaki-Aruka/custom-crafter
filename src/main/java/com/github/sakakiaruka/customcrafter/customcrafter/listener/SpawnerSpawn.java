package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class SpawnerSpawn implements Listener {
    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        CreatureSpawner spawner = event.getSpawner();
        if (spawner == null || !spawner.getPersistentDataContainer().has(EntityUtil.SPAWN_INFO_NK)) return;
        event.setCancelled(true);
        Map<String, String> data = new HashMap<>();
        Location location = spawner.getLocation();
        data.put("WORLD_UUID", location.getWorld().getUID().toString());
        data.put("BLOCK_X", String.valueOf(location.x()));
        data.put("BLOCK_Y", String.valueOf(location.y()));
        data.put("BLOCK_Z", String.valueOf(location.z()));
        data.put(EntityUtil.FROM_SPAWNER_ANCHOR, "");
        if (spawner.getPersistentDataContainer().has(EntityUtil.ONLY_INFO_SETUP_NK)) { //if (spawner.hasMetadata(EntityUtil.ONLY_INFO_SETUP)) {
            data.put(EntityUtil.ONLY_INFO_SETUP, "");
            spawner.getPersistentDataContainer().remove(EntityUtil.ONLY_INFO_SETUP_NK);
        }
        EntityUtil.spawn(data, spawner.getPersistentDataContainer().get(EntityUtil.SPAWN_INFO_NK, PersistentDataType.STRING));

    }
}

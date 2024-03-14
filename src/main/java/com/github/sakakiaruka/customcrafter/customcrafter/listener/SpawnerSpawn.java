package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SpawnerSpawn implements Listener {
    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        CreatureSpawner spawner = event.getSpawner();
        Entity entity = event.getEntity();
        if (spawner == null || !spawner.hasMetadata(EntityUtil.SPAWNER_INFO_KEY)) return;
        event.setCancelled(true);
        Map<String, String> data = new HashMap<>();
        Location location = spawner.getLocation();
        data.put("WORLD_UUID", location.getWorld().getUID().toString());
        data.put("BLOCK_X", String.valueOf(location.x()));
        data.put("BLOCK_Y", String.valueOf(location.y()));
        data.put("BLOCK_Z", String.valueOf(location.z()));
        data.put(EntityUtil.FROM_SPAWNER_ANCHOR, "");
        if (spawner.hasMetadata(EntityUtil.ONLY_INFO_SETUP)) {
            data.put(EntityUtil.ONLY_INFO_SETUP, "");
            spawner.removeMetadata(EntityUtil.ONLY_INFO_SETUP, CustomCrafter.getInstance());
        }
        spawner.getMetadata(EntityUtil.SPAWNER_INFO_KEY).forEach(e -> EntityUtil.spawn(data, e.asString()));
        spawner.resetTimer();
    }
}

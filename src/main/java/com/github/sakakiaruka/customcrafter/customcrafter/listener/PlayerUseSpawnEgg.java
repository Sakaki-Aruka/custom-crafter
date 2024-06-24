package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.EntityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerUseSpawnEgg implements Listener {
    @EventHandler
    public void onPlayerUseSpawnEgg(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.isBlockInHand()) return;
        Player player = event.getPlayer();
        ItemStack consumed = player.getInventory().getItemInMainHand();
        if (!consumed.getType().name().matches("[A-Z_0-9]+_SPAWN_EGG")) return;

        Block targetBlock = event.getClickedBlock();
        if (targetBlock == null) return;

        Map<String, String> data = new HashMap<>();
        data.put("BLOCK_X", String.valueOf(player.getLocation().x()));
        data.put("BLOCK_Y", String.valueOf(player.getLocation().y()));
        data.put("BLOCK_Z", String.valueOf(player.getLocation().z()));
        data.put("WORLD_UUID", player.getWorld().getUID().toString());
        String formula = consumed.getItemMeta().getPersistentDataContainer().get(EntityUtil.SPAWN_INFO_NK, PersistentDataType.STRING);

        if (!targetBlock.getType().equals(Material.SPAWNER)) {
            if (!consumed.getItemMeta().getPersistentDataContainer().has(EntityUtil.SPAWN_INFO_NK)) return;
            event.setCancelled(true);
            EntityUtil.spawn(data, formula);
            return;
        }

        event.setCancelled(true);
        CreatureSpawner spawner = (CreatureSpawner) targetBlock.getState();
        if (spawner.getSpawnedType() != null) {
            player.sendMessage(Component.text("§c[Custom Crafter] You can not rewrite those. This spawner has been already written entity data."));
            return;
        }

        if (formula == null || formula.isEmpty()) return;

        spawner.getPersistentDataContainer().set(EntityUtil.SPAWN_INFO_NK, PersistentDataType.STRING, formula);
        spawner.getPersistentDataContainer().set(EntityUtil.ONLY_INFO_SETUP_NK, PersistentDataType.STRING, "");

//        targetBlock.setMetadata(EntityUtil.SPAWNER_INFO_KEY, new FixedMetadataValue(CustomCrafter.getInstance(), formula));
//        targetBlock.setMetadata(EntityUtil.ONLY_INFO_SETUP, new FixedMetadataValue(CustomCrafter.getInstance(), ""));

        FallingBlock fallingBlock = (FallingBlock) targetBlock.getWorld().spawn(targetBlock.getLocation(), Objects.requireNonNull(EntityType.FALLING_BLOCK.getEntityClass()));
        BlockState pseudoState = fallingBlock.getBlockState().copy();
        pseudoState.setType(Material.AIR);
        fallingBlock.setBlockState(pseudoState);
        spawner.setSpawnedEntity(fallingBlock.createSnapshot());
        spawner.setDelay(20);
        spawner.update();
        EntityUtil.spawn(data, formula);
    }
}

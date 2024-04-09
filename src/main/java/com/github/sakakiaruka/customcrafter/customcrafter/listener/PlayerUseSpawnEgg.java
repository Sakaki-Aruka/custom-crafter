package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.EntityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
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
import org.bukkit.inventory.EquipmentSlot;
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
        if (!consumed.getType().name().matches("(?i)([A-Z_0-9]+_SPAWN_EGG)")) return;

        Block targetBlock = event.getClickedBlock();
        if (targetBlock == null || !targetBlock.getType().equals(Material.SPAWNER)) return;
        CreatureSpawner spawner = (CreatureSpawner) targetBlock.getState();

        event.setCancelled(true);

        if (spawner.getSpawnedType() != null) {
            player.sendMessage(Component.text("§c[Custom Crafter] You can not rewrite those. This spawner has been already written entity data."));
            return;
        } else if (!consumed.getItemMeta().getPersistentDataContainer().has(EntityUtil.SPAWN_INFO_NK)) {
            event.setCancelled(false);
            return;
        }

        String formula = consumed.getItemMeta().getPersistentDataContainer().get(EntityUtil.SPAWN_INFO_NK, PersistentDataType.STRING);
        if (formula == null || formula.isEmpty()) return;

        targetBlock.setMetadata(EntityUtil.SPAWNER_INFO_KEY, new FixedMetadataValue(CustomCrafter.getInstance(), formula));
        targetBlock.setMetadata(EntityUtil.ONLY_INFO_SETUP, new FixedMetadataValue(CustomCrafter.getInstance(), ""));

        FallingBlock fallingBlock = (FallingBlock) targetBlock.getWorld().spawn(targetBlock.getLocation(), Objects.requireNonNull(EntityType.FALLING_BLOCK.getEntityClass()));
        BlockState pseudoState = fallingBlock.getBlockState().copy();
        pseudoState.setType(Material.AIR);
        fallingBlock.setBlockState(pseudoState);
        spawner.setSpawnedEntity(fallingBlock.createSnapshot());
        spawner.setDelay(20);
        spawner.update();

        Map<String, String> data = new HashMap<>();
        data.put("BLOCK_X", String.valueOf(player.getLocation().x()));
        data.put("BLOCK_Y", String.valueOf(player.getLocation().y()));
        data.put("BLOCK_Z", String.valueOf(player.getLocation().z()));
        data.put("WORLD_UUID", player.getWorld().getUID().toString());
        EntityUtil.spawn(data, formula);
    }
}

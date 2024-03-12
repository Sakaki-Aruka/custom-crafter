package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.EntityUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerUseSpawnEgg implements Listener {
    @EventHandler
    public void onPlayerUseSpawnEgg(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.isBlockInHand()) return;
        Block target = event.getPlayer().getTargetBlockExact(5);
        if (target == null) return;
        EquipmentSlot slot = event.getHand();
        Player player = event.getPlayer();
        ItemStack consumed = player.getInventory().getItem(Objects.requireNonNull(slot));
        //debug
        Bukkit.getLogger().info("player consume target=" + target.getLocation());
        Bukkit.getLogger().info("player consume item=" + consumed);

        if (!consumed.getType().name().matches("(?i)([A-Z_0-9]+_SPAWN_EGG)")) return;
        event.setCancelled(true);

        if (!consumed.getItemMeta().getPersistentDataContainer().has(EntityUtil.SPAWN_EGG_INFO_KEY)) return;
        String formula = consumed.getItemMeta().getPersistentDataContainer().get(EntityUtil.SPAWN_EGG_INFO_KEY, PersistentDataType.STRING);
        if (formula == null || formula.isEmpty() || !formula.matches("[a-zA-Z_0-9]+:.+")) return;
        SpawnEggMeta meta = (SpawnEggMeta) consumed.getItemMeta();
        Entity base = player.getWorld().spawn(player.getLocation(), meta.getCustomSpawnedType().getEntityClass());
        StringBuilder buffer = new StringBuilder();
        Map<String, String> data = new HashMap<>();
        data.put("BLOCK_X", String.valueOf(player.getLocation().getBlockX()));
        data.put("BLOCK_Y", String.valueOf(player.getLocation().getBlockY()));
        data.put("BLOCK_Z", String.valueOf(player.getLocation().getBlockZ()));
        String name = formula.substring(0, formula.indexOf(":")); // name_test:~~~~,~~~~,~~~~....
        String values = formula.substring(formula.indexOf(":") + 1);
        for (int i = 0; i < values.length(); i++) {
            char c = values.charAt(i);
            if (c == ',' && (i - 1 < 0 || values.charAt(i - 1) == '\\')) {
                // ~~~:~~~~~~~
                // e.g. add_passenger:passenger=test,add_passenger:passenger=test_2
                String f = buffer.toString();
                String type = f.substring(0, f.indexOf(":"));
                String value = f.substring(f.indexOf(":"));
                switch (type) {
                    case "type" -> {
                        EntityType t;
                        try {t = EntityType.valueOf(value.toUpperCase());} catch (IllegalArgumentException e) {continue;}
                        meta.setCustomSpawnedType(t);
                    }
                    case "add_passenger" -> EntityUtil.ADD_PASSENGER.accept(value, data, base);
                    case "set_armor" -> EntityUtil.SET_ARMOR.accept(value, data, base);
                    case "set_drop_chance" -> EntityUtil.SET_DROP_CHANCE.accept(value, data, base);
                    case "set_various_values" -> EntityUtil.SET_VARIOUS_VALUES.accept(value, data, base);
                    case "add_attribute" -> EntityUtil.ADD_ATTRIBUTE.accept(value, data, base);
                }
            } else buffer.append(c);
        }
        EntityUtil.DEFINED_ENTITIES.put(name, base);
    }
}
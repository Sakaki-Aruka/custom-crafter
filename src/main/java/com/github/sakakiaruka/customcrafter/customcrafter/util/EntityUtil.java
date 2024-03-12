package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class EntityUtil {
    // name must be use only "a-zA-Z0-9_-"
    public static Map<String, Entity> DEFINED_ENTITIES = new HashMap<>();
    public static final NamespacedKey SPAWN_EGG_INFO_KEY = new NamespacedKey(CustomCrafter.getInstance(), "spawn_info");

    private static final String ALL_ENTITY_TYPE_REGEX_PATTERN = "(" + String.join("|", Arrays.stream(EntityType.values()).map(Enum::name).collect(Collectors.toSet())) + ")";

    public static final TriConsumer<Map<String, String>, ItemStack, String> SPAWN_EGG = (data, item, formula) -> {
        // type: spawn_egg, value: name:v1-length:v1,v2-length:v2
        if (!(item.getItemMeta() instanceof SpawnEggMeta)) return;
        SpawnEggMeta meta = (SpawnEggMeta) item.getItemMeta();
        meta.getPersistentDataContainer().set(SPAWN_EGG_INFO_KEY, PersistentDataType.STRING, CalcUtil.getContent(data, formula));
    };

    private static Location getLocationFromData(Map<String, String> data) {
        return new Location(
                Bukkit.getWorld(UUID.fromString(data.get("WORLD_UUID"))),
                Double.parseDouble(data.get("BLOCK_X")),
                Double.parseDouble(data.get("BLOCK_Y")),
                Double.parseDouble(data.get("BLOCK_Z")));
    }

    public static final TriConsumer<String, Map<String, String>, Entity> ADD_PASSENGER = (formula, data, base) -> {
        // passenger=~~~
        final String pattern = "(predicate=(true|false)/)?passenger=([a-zA-Z0-9_-]+)";
        addEntityAndWorldData(data, base);
        data.put("$CURRENT_TARGET_PASSENGERS_COUNT$", String.valueOf(base.getPassengers().size()));
        formula = CalcUtil.getContent(data, formula);
        ContainerUtil.removeCurrentVariables(data);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        Entity passenger = DEFINED_ENTITIES.get(parsed.group(3));
        if (!(base instanceof Mob) || !(passenger instanceof Mob)) return;
        base.addPassenger(passenger);
    };

    public static final TriConsumer<String, Map<String, String>, Entity> SET_ARMOR = (formula, data, base) -> {
        // (helmet|chest|leggings|boots)=~~~
        final String pattern = "(predicate=(true|false)/)?(helmet|chest|leggings|boots|mainhand|offhand)=([a-zA-Z0-9_-]+)";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        ItemStack item = ContainerUtil.DEFINED_ITEMS.get(parsed.group(4));
        if (!(base instanceof Mob) || item == null) return;
        EntityEquipment equipment = ((LivingEntity) base).getEquipment();
        switch (parsed.group(3)) {
            case "helmet" -> equipment.setHelmet(item);
            case "chest" -> equipment.setChestplate(item);
            case "leggings" -> equipment.setLeggings(item);
            case "boots" -> equipment.setBoots(item);
            case "mainhand" -> equipment.setItemInMainHand(item);
            case "offhand" -> equipment.setItemInOffHand(item);
        }
    };

    public static final TriConsumer<String, Map<String, String>, Entity> SET_DROP_CHANCE = (formula, data, base) -> {
        // slot=~~~/chance=~~~
        final String pattern = "(predicate=(true|false)/)?slot=(helmet|chest|leggings|boots|mainhand|offhand)/chance=([0-9.]+)";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        if (!(base instanceof Mob)) return;
        EntityEquipment equipment = ((LivingEntity) base).getEquipment();
        float chance = Float.parseFloat(parsed.group(4));
        switch (parsed.group(3)) {
            case "helmet" -> equipment.setHelmetDropChance(chance);
            case "chest" -> equipment.setChestplateDropChance(chance);
            case "leggings" -> equipment.setLeggingsDropChance(chance);
            case "boots" -> equipment.setBootsDropChance(chance);
            case "mainhand" -> equipment.setItemInMainHandDropChance(chance);
            case "offhand" -> equipment.setItemInOffHandDropChance(chance);
        }
    };

    public static final TriConsumer<String, Map<String, String>, Entity> SET_VARIOUS_VALUES = (formula, data, base) -> {
        // type=~~~/value=~~~
        final String pattern = "(predicate=(true|false)/)?type=(delay|nearby|max_delay|min_delay|spawn_range|entities|in_player)/value=([0-9]+)";
        addEntityAndWorldData(data, base);
        Location location = getLocationFromData(data);
        CreatureSpawner spawner = (CreatureSpawner) Bukkit.getWorld(UUID.fromString(data.get("WORLD_UUID"))).getBlockAt(location);
        data.put("$CURRENT_SPAWNER_DELAY$", String.valueOf(spawner.getDelay()));
        data.put("$CURRENT_SPAWNER_MAX_DELAY$", String.valueOf(spawner.getMaxSpawnDelay()));
        data.put("$CURRENT_SPAWNER_MIN_DELAY$", String.valueOf(spawner.getMinSpawnDelay()));
        data.put("$CURRENT_SPAWNER_MAX_NEARBY$", String.valueOf(spawner.getMaxNearbyEntities()));
        data.put("$CURRENT_SPAWNER_PLAYER_RANGE$", String.valueOf(spawner.getRequiredPlayerRange()));
        data.put("$CURRENT_SPAWNER_SPAWN_RANGE$", String.valueOf(spawner.getSpawnRange()));
        data.put("$CURRENT_SPAWNER_SPAWN_COUNT$", String.valueOf(spawner.getSpawnCount()));
        formula = CalcUtil.getContent(data, formula);
        ContainerUtil.removeCurrentVariables(data);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        int value = Integer.parseInt(parsed.group(4));
        switch (parsed.group(3)) {
            case "delay" -> spawner.setDelay(value);
            case "nearby" -> spawner.setMaxNearbyEntities(value);
            case "max_delay" -> spawner.setMaxSpawnDelay(value);
            case "min_delay" -> spawner.setMinSpawnDelay(value);
            case "in_player" -> spawner.setRequiredPlayerRange(value);
            case "spawn_range" -> spawner.setSpawnRange(value);
            case "entities" -> spawner.setSpawnCount(value);
        }
    };

    public static final TriConsumer<String, Map<String, String>, Entity> ADD_ATTRIBUTE = (formula, data, base) -> {
        // attribute=~~~/action=~~~/value=~~~/slot=~~~
        final String pattern = "(predicate=(true|false)/)?attribute=([a-zA-Z_]+)/action=([a-zA-Z_0-9]+)/value=([0-9-.]+)/(slot=([a-zA-Z_]+))?";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        Attribute attribute = Attribute.valueOf(parsed.group(3).toUpperCase());
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(parsed.group(4).toUpperCase());
        double value = Double.parseDouble(parsed.group(5));
        EquipmentSlot slot = parsed.group(6) != null ? EquipmentSlot.valueOf(parsed.group(6)) : null;
        AttributeModifier modifier;
        if (slot != null) modifier = new AttributeModifier(UUID.randomUUID(), UUID.randomUUID().toString(), value, operation, slot);
        else modifier = new AttributeModifier(UUID.randomUUID(), UUID.randomUUID().toString(), value, operation);

        AttributeInstance instance;
        if ((instance = ((Attributable) base).getAttribute(attribute)) == null) {
            ((Attributable) base).registerAttribute(attribute);
            instance = ((Attributable) base).getAttribute(attribute);
        }
        Objects.requireNonNull(instance).addModifier(modifier);
    };

    private static void addEntityAndWorldData(Map<String, String> data, Entity target) {
        data.put("BASE_ENTITY_TYPE", target.getType().name());
        data.put("CURRENT_TIME_TICK", String.valueOf(Objects.requireNonNull(Bukkit.getWorld(UUID.fromString(data.get("WORLD_UUID")))).getTime()));
    }
}

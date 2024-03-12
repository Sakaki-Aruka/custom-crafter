package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.PentaConsumer;
import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.net.ssl.SNIHostName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class EntityUtil {
    // name must be use only "a-zA-Z0-9_-"
    public static Map<String, Entity> DEFINED_ENTITIES = new HashMap<>();
    public static final NamespacedKey SPAWN_EGG_INFO_KEY = new NamespacedKey(CustomCrafter.getInstance(), "spawn_info");

    private static final String ALL_ENTITY_TYPE_REGEX_PATTERN = "(" + Arrays.stream(EntityType.values()).map(Enum::name).collect(Collectors.joining("|")) + ")";


    public static final TriConsumer<Map<String, String>, ItemStack, String> ENTITY_DEFINE = (data, item, formula) -> {
        // type: entity_define, value: name:([a-zA-Z_0-9]+),actions:~~~~~,~~~~~,~~~~~
        final String pattern = "name:([a-zA-Z_0-9]+),actions:(.+)";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String name = parsed.group(1);
        StringBuilder builder = new StringBuilder(container.has(SPAWN_EGG_INFO_KEY) ? container.get(SPAWN_EGG_INFO_KEY, PersistentDataType.STRING) + "," : "");
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < parsed.group(2).length(); i++) {
            char c = parsed.group(2).charAt(i);
            if (c == ',' && 0 < i - 1 && parsed.group(2).charAt(i - 1) != '\\') {
                // separate
                builder.append(name.length() + 1 + buffer.length())
                        .append(":")
                        .append(name)
                        .append(":")
                        .append(buffer)
                        .append(",");
                buffer.setLength(0);
            } else if (c == ',' && 0 < i - 1 && parsed.group(2).charAt(i - 1) == '\\') {
                // escape sequence
                buffer.deleteCharAt(buffer.length() - 1);
            } else buffer.append(c);
        }
        if (!buffer.isEmpty()) {
            builder.append(name.length() + 1 + buffer.length())
                    .append(":")
                    .append(name)
                    .append(":")
                    .append(buffer);
        }

        //debug
        Bukkit.getLogger().info("entity define builder=" + builder);

        container.set(SPAWN_EGG_INFO_KEY, PersistentDataType.STRING, builder.toString());
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> SPAWN_EGG = (data, item, formula) -> {
        // type: spawn_egg, value: name:v1-length:v1,v2-length:v2
        if (!(item.getItemMeta() instanceof SpawnEggMeta)) return;
        SpawnEggMeta meta = (SpawnEggMeta) item.getItemMeta();
        meta.getPersistentDataContainer().set(SPAWN_EGG_INFO_KEY, PersistentDataType.STRING, "");
        item.setItemMeta(meta);
        ENTITY_DEFINE.accept(data, item, formula);
        //meta.getPersistentDataContainer().set(SPAWN_EGG_INFO_KEY, PersistentDataType.STRING, CalcUtil.getContent(data, formula));
    };

    public static void spawn(Map<String, String> data, String formula) {
        // spawn info format = "length:name:info"
        // "self" means base target
        String uniqueID = UUID.randomUUID().toString();
        data.put("uniqueID", uniqueID);
        formula = CalcUtil.getContent(data, formula);
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < formula.length(); i++) {
            // read length = flag 0 | read name = flag 1 | read actions = flag 2
            char c = formula.charAt(i);
            if (c != ':') {
                buffer.append(c);
                continue;
            }
            int length = Integer.parseInt(buffer.toString().replace(",", ""));
            if (i + length > formula.length()) break;
            String element = formula.substring(i + 1, i + 1 + length);
            i += length;

            // debug
            System.out.println("element=" + element);
            //System.out.println("data=" + data);
            System.out.println("formula=" + formula);

            Matcher parsed = Pattern.compile("([a-zA-Z_0-9]+):([|a-z_0-9]+)=(.+)").matcher(element);
            if (!parsed.matches()) break;
            buffer.setLength(0);
            String name = parsed.group(1);
            String type = parsed.group(2);
            String action = parsed.group(3);
            String key = uniqueID + "." + name;

            //debug
            System.out.println("name=" + name);
            System.out.println("type=" + type);
            System.out.println("action=" + action);

            if (type.equalsIgnoreCase("type") && !DEFINED_ENTITIES.containsKey(key)) {
                Location location = getLocationFromData(data);
                World world;
                if ((world = Bukkit.getWorld(UUID.fromString(data.get("WORLD_UUID")))) == null) break;
                Entity entity = world.spawn(location, Objects.requireNonNull(EntityType.valueOf(action.toUpperCase()).getEntityClass()));
                DEFINED_ENTITIES.put(key, entity);
                data.put(name, key);
                continue;
            }
            if (!DEFINED_ENTITIES.containsKey(key) && !name.equals("__internal__")) continue;
            Entity defined = DEFINED_ENTITIES.get(key);
            if (type.equalsIgnoreCase("add_passenger")) ADD_PASSENGER.accept(action, data, defined);
            else if (type.equalsIgnoreCase("set_armor")) SET_ARMOR.accept(action, data, defined);
            else if (type.equalsIgnoreCase("set_drop_chance")) SET_DROP_CHANCE.accept(action, data, defined);
            else if (type.equalsIgnoreCase("set_various_value")) SET_VARIOUS_VALUES.accept(action, data, defined);
            else if (type.equalsIgnoreCase("add_attribute")) ADD_ATTRIBUTE.accept(action, data, defined);
            else if (type.equalsIgnoreCase("item_define") && name.equals("__internal__")) {
                // item define
                Map<String, String> pseudoData = Map.of("$RECIPE_NAME$", uniqueID);
                ItemStack pseudoItem = new ItemStack(Material.AIR);
                ContainerUtil.ITEM_DEFINE.accept(pseudoData, pseudoItem, action);

                //debug
                System.out.println("defined item map(internal)=" + ContainerUtil.DEFINED_ITEMS);
            }
        }
        DEFINED_ENTITIES.keySet().removeIf(e -> e.startsWith(uniqueID));
        ContainerUtil.DEFINED_ITEMS.keySet().removeIf(e -> e.startsWith("$" + uniqueID + "."));
    }

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

        //debug
        System.out.println("add passenger formula=" + formula);
        System.out.println("defined entities map=" + DEFINED_ENTITIES);

        ContainerUtil.removeCurrentVariables(data);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        Entity passenger = DEFINED_ENTITIES.get(data.get(parsed.group(3)));
        if (!(base instanceof Mob) || !(passenger instanceof Mob)) return;
        base.addPassenger(passenger);
    };

    public static final TriConsumer<String, Map<String, String>, Entity> SET_ARMOR = (formula, data, base) -> {
        // (helmet|chest|leggings|boots)=~~~
        final String pattern = "(predicate=(true|false)/)?(helmet|chest|leggings|boots|mainhand|offhand)=([$.a-zA-Z0-9_-]+)";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);

        //debug
        System.out.println("set armor formula=" + formula);
        System.out.println("defined item map=" + ContainerUtil.DEFINED_ITEMS);

        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        ItemStack item = ContainerUtil.DEFINED_ITEMS.get("$" + data.get("uniqueID") + "." + parsed.group(4) + "$");

        //debug
        System.out.println("defined item map key=" + parsed.group(4));

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

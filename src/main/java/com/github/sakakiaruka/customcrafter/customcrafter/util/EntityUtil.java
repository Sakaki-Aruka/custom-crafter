package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class EntityUtil {
    // name must be use only "a-zA-Z0-9_-"
    public static Map<String, Entity> DEFINED_ENTITIES = new HashMap<>();
    public static final NamespacedKey SPAWN_INFO_NK = new NamespacedKey(CustomCrafter.getInstance(), "spawn_info");
    public static final String SPAWNER_INFO_KEY = "spawn_info";
    public static final String ONLY_INFO_SETUP = "ONLY_INFO_SETUP";
    public static final String FROM_SPAWNER_ANCHOR = "from_spawner_anchor";
    public static final String FALLING_BLOCK_HAS_UNTRACKED_CHANGE_ANCHOR = "falling_block_has_untracked_change_anchor";

    public static final NamespacedKey ONLY_INFO_SETUP_NK = new NamespacedKey(CustomCrafter.getInstance(), "only_info_setup");
    public static final NamespacedKey FROM_SPAWNER_ANCHOR_NK = new NamespacedKey(CustomCrafter.getInstance(), "from_spawner_anchor");
    public static int MAX_NEARBY_ENTITIES = 1000;
    public static int MAX_SPAWN_RANGE = 100;
    public static int MAX_SPAWN_COUNT = 100;
    public static int MAX_REQ_PLAYER_RANGE = 64;
    public static int MAX_LIGHT_LEVEL = 15;
    public static int MIN_LIGHT_LEVEL = 0;
    public static int MAX_SPAWN_WEIGHT = 1000;
    public static int MAX_SPAWN_DELAY = Integer.MAX_VALUE - 1;

    private static final String ALL_ENTITY_TYPE_REGEX_PATTERN = "(" + Arrays.stream(EntityType.values()).map(Enum::name).collect(Collectors.joining("|")) + ")";
    private static final String UNIQUE_ID_KEY = "uniqueID";
    private static final NamespacedKey ALREADY_SPAWNER_SETUP_OK_KEY = new NamespacedKey(CustomCrafter.getInstance(), "already_spawner_setup_ok_key");

    public static final TriConsumer<Map<String, String>, ItemStack, String> ENTITY_DEFINE = (data, item, formula) -> {
        // type: entity_define, value: name:([a-zA-Z_0-9]+),actions:~~~~~,~~~~~,~~~~~
        final String pattern = "name:([a-zA-Z_0-9]+),actions:(.+)";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String name = parsed.group(1);
        StringBuilder builder = new StringBuilder(container.has(SPAWN_INFO_NK) ? container.get(SPAWN_INFO_NK, PersistentDataType.STRING) + "," : "");
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < parsed.group(2).length(); i++) {
            char c = parsed.group(2).charAt(i);
            if (c == ',' && 0 < i - 1 && parsed.group(2).charAt(i - 1) != '\\') {
                // separate
                if (buffer.toString().matches("->[a-zA-Z_0-9]+")) {
                    // change target in one line
                    name = buffer.substring(2);
                    buffer.setLength(0);
                    continue;
                }
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
        container.set(SPAWN_INFO_NK, PersistentDataType.STRING, builder.toString());
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> SPAWN_EGG = (data, item, formula) -> {
        // type: spawn_egg, value: name:v1-length:v1,v2-length:v2
        if (!(item.getItemMeta() instanceof SpawnEggMeta)) return;
        SpawnEggMeta meta = (SpawnEggMeta) item.getItemMeta();
        meta.getPersistentDataContainer().set(SPAWN_INFO_NK, PersistentDataType.STRING, "");
        item.setItemMeta(meta);
        ENTITY_DEFINE.accept(data, item, formula);
        //meta.getPersistentDataContainer().set(SPAWN_EGG_INFO_KEY, PersistentDataType.STRING, CalcUtil.getContent(data, formula));
    };

    public static void spawn(Map<String, String> data, String formula) {
        // spawn info format = "length:name:info"
        // "self" means base target
        String uniqueID = UUID.randomUUID().toString();
        data.put(UNIQUE_ID_KEY, uniqueID);
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

            Matcher parsed = Pattern.compile("([a-zA-Z_0-9]+):([|a-z_0-9]+)=(.+)").matcher(element);
            if (!parsed.matches()) break;
            buffer.setLength(0);
            String name = parsed.group(1);
            String type = parsed.group(2);
            String action = parsed.group(3);
            String key = uniqueID + "." + name;

            if (type.equalsIgnoreCase("type") && !DEFINED_ENTITIES.containsKey(key)) {
                World world;
                if ((world = Bukkit.getWorld(UUID.fromString(data.get("WORLD_UUID")))) == null) break;
                Location location;
                if (data.containsKey(FROM_SPAWNER_ANCHOR)) {
                    CreatureSpawner spawner = (CreatureSpawner) world.getBlockAt(getLocationFromData(data)).getState();
                    // random step is "0.5"
                    int xzRandomRange = spawner.getSpawnRange();
                    double x = Double.parseDouble(data.get("BLOCK_X"));
                    double z = Double.parseDouble(data.get("BLOCK_Z"));
                    if (1 < xzRandomRange) {
                        List<Double> diffCandidate = new ArrayList<>();
                        for (double d = 1; d < xzRandomRange; d += 0.2) diffCandidate.add(d);
                        x += (new Random().nextBoolean() ? -1 : 1) * diffCandidate.get(new Random().nextInt(diffCandidate.size()));
                        z += (new Random().nextBoolean() ? -1 : 1) * diffCandidate.get(new Random().nextInt(diffCandidate.size()));
                    }
                    location = new Location(world, x, Double.parseDouble(data.get("BLOCK_Y")), z);
                } else location = getLocationFromData(data);  // when player using
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
            else if (type.equalsIgnoreCase("ai")) AI.accept(action, data, defined);
            else if (type.equalsIgnoreCase("set_spawner_value")) SET_VARIOUS_SPAWNER_VALUE.accept(action, data, defined);
            else if (type.equalsIgnoreCase("falling_type")) FALLING_TYPE.accept(action, data, defined);
            else if (type.equalsIgnoreCase("dropped_item_detail")) DROPPED_ITEM_DETAIL.accept(action, data, defined);
            else if (type.equalsIgnoreCase("splash_potion_detail")) SPLASH_POTION_DETAIL.accept(action, data, defined);
            else if (type.equalsIgnoreCase("item_define") && name.equals("__internal__")) {
                // item define
                //Map<String, String> pseudoData = Map.of("$RECIPE_NAME$", uniqueID);
                Map<String, String> pseudoData = new HashMap<>();
                pseudoData.put("$RECIPE_NAME$", uniqueID);
                ItemStack pseudoItem = new ItemStack(Material.AIR);
                ContainerUtil.ITEM_DEFINE.accept(pseudoData, pseudoItem, action);
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

    public static final TriConsumer<String, Map<String, String>, Entity> AI = (formula, data, base) -> {
        // ai=(true|false)
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);
        if (!(base instanceof Mob)) return;
        ((Mob) base).setAI(formula.endsWith("true"));
    };

    public static final TriConsumer<String, Map<String, String>, Entity> SET_VARIOUS_SPAWNER_VALUE = (formula, data, base) -> {


        if (!data.containsKey(ONLY_INFO_SETUP)) return;
        final String pattern = "((delay|max_nearby_entities|max_spawn_delay|min_spawn_delay|spawn_range|spawn_count|req_player_range|spawn_weight|max_block_light|min_block_light|max_sky_light|min_sky_light|rough_control):([0-9]+|random\\[([0-9-]+)?:([0-9-]+)?]);)+";
        final String singlePattern = "(delay|max_nearby_entities|max_spawn_delay|min_spawn_delay|spawn_range|spawn_count|req_player_range|spawn_weight|max_block_light|min_block_light|max_sky_light|min_sky_light|rough_control):([0-9]+|random\\[([0-9-]+)?:([0-9-]+)?])";
        // if you want to set (min|max)_block_light, (min|max)_sky_light, need to write all of those elements
        // in 'data', "uniqueID.(min|max)_(sky|block)_light" => new SpawnRule (with "spawn_weight")
        // "SpawnCount" needs to set "MinSpawnDelay"
        formula = CalcUtil.getContent(data, formula);
        if (!formula.matches(pattern)) return;
        World world = Bukkit.getWorld(UUID.fromString(data.get("WORLD_UUID")));
        if (world == null) return;
        Block block = world.getBlockAt(getLocationFromData(data));
        if (!block.getType().equals(Material.SPAWNER)) return;

        CreatureSpawner spawner = (CreatureSpawner) world.getBlockAt(getLocationFromData(data)).getState();
        if (spawner.getPersistentDataContainer().has(ALREADY_SPAWNER_SETUP_OK_KEY)) return;

        for (String element : formula.split(";")) {


            Matcher parsed = Pattern.compile(singlePattern).matcher(element);
            if (!parsed.matches()) continue;
            String type = parsed.group(1);
            String numSource = parsed.group(2);

            switch (type) {
                case "delay" -> spawner.setDelay(CalcUtil.getRandomNumber(numSource, -1, Integer.MAX_VALUE - 1));
                case "max_nearby_entities" -> spawner.setMaxNearbyEntities(CalcUtil.getRandomNumber(numSource, 1, MAX_NEARBY_ENTITIES));
                case "min_spawn_delay" -> data.put(data.get(UNIQUE_ID_KEY) + ".min_delay", String.valueOf(CalcUtil.getRandomNumber(numSource, 1, MAX_SPAWN_DELAY)));
                case "max_spawn_delay" -> data.put(data.get(UNIQUE_ID_KEY) + ".max_delay", String.valueOf(CalcUtil.getRandomNumber(numSource, 1, MAX_SPAWN_DELAY)));
                case "spawn_range" -> spawner.setSpawnRange(CalcUtil.getRandomNumber(numSource, 0, MAX_SPAWN_RANGE));
                case "spawn_count" -> spawner.setSpawnCount(CalcUtil.getRandomNumber(numSource, 1, MAX_SPAWN_COUNT));
                case "req_player_range" -> spawner.setRequiredPlayerRange(CalcUtil.getRandomNumber(numSource, 0, MAX_REQ_PLAYER_RANGE));

                case "min_block_light" -> data.put(data.get(UNIQUE_ID_KEY) + ".min_block_light", numSource);
                case "max_block_light" -> data.put(data.get(UNIQUE_ID_KEY) + ".max_block_light", numSource);
                case "min_sky_light" -> data.put(data.get(UNIQUE_ID_KEY) + ".min_sky_light", numSource);
                case "max_sky_light" -> data.put(data.get(UNIQUE_ID_KEY) + ".max_sky_light", numSource);

                case "spawn_weight" -> {
                    String minBlockKey = data.get(UNIQUE_ID_KEY) + ".min_block_light";
                    String maxBlockKey =  data.get(UNIQUE_ID_KEY) + ".max_block_light";
                    String minSkyKey = data.get(UNIQUE_ID_KEY) + ".min_sky_light";
                    String maxSkyKey = data.get(UNIQUE_ID_KEY) + ".max_sky_light";
                    if (!data.keySet().containsAll(Set.of(minBlockKey, maxBlockKey, minSkyKey, maxSkyKey))) continue;

                    int minBlock = CalcUtil.getRandomNumber(data.get(minBlockKey), MIN_LIGHT_LEVEL, MAX_LIGHT_LEVEL);
                    int maxBlock = CalcUtil.getRandomNumber(data.get(maxBlockKey), Math.max(minBlock, MIN_LIGHT_LEVEL), MAX_LIGHT_LEVEL);
                    int minSky = CalcUtil.getRandomNumber(data.get(minSkyKey), MIN_LIGHT_LEVEL, MAX_LIGHT_LEVEL);
                    int maxSky = CalcUtil.getRandomNumber(data.get(maxSkyKey), Math.max(minSky, MIN_LIGHT_LEVEL), MAX_LIGHT_LEVEL);
                    SpawnRule rule = new SpawnRule(minBlock, maxBlock, minSky, maxSky);
                    int weight = CalcUtil.getRandomNumber(numSource, 1, MAX_SPAWN_WEIGHT);
                    spawner.addPotentialSpawn(new SpawnerEntry(base.createSnapshot(), weight, rule));

                    CustomCrafter.getInstance().getLogger().info(
                            SettingsLoad.LINE_SEPARATOR +
                            "Spawner Setup done." + SettingsLoad.LINE_SEPARATOR +
                                    "Location: " + spawner.getLocation() + SettingsLoad.LINE_SEPARATOR +
                                    "  - require min block light: " + rule.getMinBlockLight() + SettingsLoad.LINE_SEPARATOR +
                                    "  - require max block light: " + rule.getMaxBlockLight() + SettingsLoad.LINE_SEPARATOR +
                                    "  - require min sky light: " + rule.getMinSkyLight() + SettingsLoad.LINE_SEPARATOR +
                                    "  - require max sky light: " + rule.getMaxSkyLight() + SettingsLoad.LINE_SEPARATOR +
                                    "  - spawn weight: " + weight
                    );
                }
                case "rough_control" -> {
                    int weight = Integer.parseInt(numSource);
                    spawner.addPotentialSpawn(base.createSnapshot(), weight, null);
                    //spawner.removeMetadata(SPAWNER_INFO_KEY, CustomCrafter.getInstance());
                    spawner.getPersistentDataContainer().remove(EntityUtil.SPAWN_INFO_NK);
                }
            }

            String minDelayKey = data.get(UNIQUE_ID_KEY) + ".min_delay";
            String maxDelayKey = data.get(UNIQUE_ID_KEY) + ".max_delay";
            if (data.containsKey(minDelayKey) && !data.containsKey(maxDelayKey)) {
                // only min
                int min = Integer.parseInt(data.get(minDelayKey));
                if (min <= spawner.getMaxSpawnDelay()) spawner.setMinSpawnDelay(min);
            } else if (!data.containsKey(minDelayKey) && data.containsKey(maxDelayKey)) {
                // only max
                int max = Integer.parseInt(data.get(maxDelayKey));
                if (spawner.getMinSpawnDelay() <= max) spawner.setMaxSpawnDelay(max);
            } else if (data.keySet().containsAll(Set.of(minDelayKey, maxDelayKey))) {
                // set both
                int min = Integer.parseInt(data.get(minDelayKey));
                int max = Integer.parseInt(data.get(maxDelayKey));
                if (min <= max) {
                    spawner.setMinSpawnDelay(0);
                    spawner.setMaxSpawnDelay(Integer.MAX_VALUE);
                    spawner.update();
                    spawner.setMinSpawnDelay(min);
                    spawner.setMaxSpawnDelay(max);
                }
            }
            spawner.update(true, true);
        }

        spawner.getPersistentDataContainer().set(ALREADY_SPAWNER_SETUP_OK_KEY, PersistentDataType.STRING, "");
        spawner.update(true, true);
    };

    public static final TriConsumer<String, Map<String, String>, Entity> DROPPED_ITEM_DETAIL = (formula, data, base) -> {
        // item:~~~
        if (!(base instanceof Item)) return;
        final String pattern = "(predicate:(true|false);)?item:([a-zA-Z0-9_]+)";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);

        if (!parsed.matches()) return;
        if (parsed.group(2) != null && parsed.group(2).equals("false")) return;
        String name = "$" + data.get(UNIQUE_ID_KEY) + "." + parsed.group(3) + "$";

        if (!ContainerUtil.DEFINED_ITEMS.containsKey(name)) return;
        ((Item) base).setItemStack(ContainerUtil.DEFINED_ITEMS.get(name));
    };

    public static final TriConsumer<String, Map<String, String>, Entity> FALLING_TYPE = (formula, data, base) -> {
        // block:~~~
        if (!(base instanceof FallingBlock)) return;
        final String pattern = "(predicate:(true|false);)?name:([a-zA-Z_0-9]+);block:([a-zA-Z_0-9\\[\\]!/]+)(;toBlock:(true|false))?(;dropItem:(true|false))?(;max_damage:(default|[0-9]+\\.?[0-9]*))?(;damage_per_block:([0-9]+\\.?[0-9]*))?";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);

        if (!parsed.matches()) return;
        if (parsed.group(2) != null && parsed.group(2).equalsIgnoreCase("false")) return;
        boolean toBlock = parsed.group(6) != null && Boolean.parseBoolean(parsed.group(6));
        boolean dropItem = parsed.group(8) != null && Boolean.parseBoolean(parsed.group(8));
        boolean setMaxDamage = parsed.group(10) != null;
        boolean setDamagePerBlock = parsed.group(12) != null;
        Material material;
        try {
            if (parsed.group(4).startsWith("random")) {
                Set<Material> limit = new HashSet<>();
                limit.addAll(RandomUtil.solidMaterials);
                limit.addAll(RandomUtil.occludingMaterials);
                material = RandomUtil.getRandomMaterial(parsed.group(4).replace("/", ","), limit);
            }
            else material = Material.valueOf(parsed.group(4).toUpperCase());
            if (material.equals(Material.AIR) || !RandomUtil.blockMaterials.contains(material)) return;
        } catch (Exception e) {
            return;
        }

        String name = parsed.group(3);
        BlockState pseudoState = ((FallingBlock) base).getBlockState().copy();
        pseudoState.setType(material);
        ((FallingBlock) base).setCancelDrop(!toBlock);
        ((FallingBlock) base).setDropItem(dropItem);
        ((FallingBlock) base).setBlockState(pseudoState);

        if (setMaxDamage) {
            int maxDamage;
            try {
                if (parsed.group(10).equalsIgnoreCase("default")) {
                    ((FallingBlock) base).setHurtEntities(true);
                } else {
                    maxDamage = (int) Math.round(Double.parseDouble(parsed.group(10)));
                    if (maxDamage >= 0) ((FallingBlock) base).setMaxDamage(maxDamage);
                }
            } catch (Exception e) {
                CustomCrafter.getInstance().getLogger().warning("falling_type error: invalid number format or the damage out of the range.");
            }
        }

        if (setDamagePerBlock) {
            float damagePerBlock;
            try {
                damagePerBlock = Float.parseFloat(parsed.group(12));
                if (damagePerBlock > 0f) ((FallingBlock) base).setDamagePerBlock(damagePerBlock);
            } catch (Exception e) {
                CustomCrafter.getInstance().getLogger().warning("falling_type error: invalid number format or the damage per block out of the range.");
            }
        }

        String key = data.get(UNIQUE_ID_KEY) + "." + name;
        DEFINED_ENTITIES.put(key, base);
        data.put(FALLING_BLOCK_HAS_UNTRACKED_CHANGE_ANCHOR, "");
    };

    public static final TriConsumer<String, Map<String, String>, Entity> SPLASH_POTION_DETAIL = (formula, data, base) -> {
        if (!(base instanceof ThrownPotion)) return;
        final String pattern = "(predicate=(true|false);)?item:([a-zA-Z_0-9]+)(;cloudy;[a-zA-Z_0-9:\\[\\];]+)?";
        final String eachElementPattern = "([a-zA-Z_0-9]+):(.+)";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        String key = "$" + data.get(UNIQUE_ID_KEY) + "." + parsed.group(3) + "$";
        if (!ContainerUtil.DEFINED_ITEMS.containsKey(key)) return;
        ItemStack potion = ContainerUtil.DEFINED_ITEMS.get(key);
        if (!(potion.getItemMeta() instanceof PotionMeta)) return;
        ((ThrownPotion) base).setPotionMeta(((PotionMeta) potion.getItemMeta()).clone());
        if (parsed.group(4) == null) return; // not contained "cloudy" section.
        Location location = getLocationFromData(data);
        World world = location.getWorld();
        AreaEffectCloud aec = (AreaEffectCloud) world.spawn(location, EntityType.AREA_EFFECT_CLOUD.getEntityClass());
        aec.setDuration(getMinPotionEffectDuration(((PotionMeta) potion.getItemMeta()).getCustomEffects()));
        ((PotionMeta) potion.getItemMeta()).getCustomEffects().forEach(e -> aec.addCustomEffect(e, true));
        for (String element : parsed.group(4).split(";")) {
            Matcher mini = Pattern.compile(eachElementPattern).matcher(element);
            if (!mini.matches()) continue;
            String type = mini.group(1);
            String value = mini.group(2);
            if (value.matches("(-?[0-9]+|random\\[(-?[0-9-]+)?:(-?[0-9-]+)?])")) {
                // set duration
                int num = value.startsWith("random")
                        ? CalcUtil.getRandomNumber(value, -1, 20 * 60 * 10)
                        : Integer.parseInt(value);
                if (type.matches("d|duration")) aec.setDuration(num);
                else if (type.matches("dou|durationOnUse")) aec.setDurationOnUse(num);
                else if (type.matches("rd|reapplicationDelay")) aec.setReapplicationDelay(num);
                else if (type.matches("wt|waitTime")) aec.setWaitTime(num);
            } else if (value.matches("([0-9.]+|random\\[([0-9-.]+)?:([0-9-.]+)?])")) {
                float num = value.startsWith("random")
                        ? RandomUtil.getRandomFloat(value, 1, 100)
                        : Float.parseFloat(value);
                if (!(1 <= num && num <= 100)) return;
                if (type.matches("rou|radiusOnUse")) aec.setRadiusOnUse(num);
                else if (type.matches("rpt|radiusPerTick")) aec.setRadiusPerTick(num);
                else if (type.matches("r|radius")) aec.setRadius(num);
            } else if (type.matches("p|particle")) {
                Particle particle = value.startsWith("random")
                        ? RandomUtil.getRandomParticle(value)
                        : value.matches(RandomUtil.allParticlesRegexPattern) ? Particle.valueOf(value.toUpperCase()) : null;
                if (particle == null) continue;
                aec.setParticle(particle);
            } else if (type.matches("c|color")) {
                Color color = value.startsWith("random")
                        ? RandomUtil.getRandomColor(value)
                        : ContainerUtil.getRGBColor(value);
                if (color == null) continue;
                aec.setColor(color);
            }
        }
    };

    private static int getMinPotionEffectDuration(Collection<PotionEffect> effects) {
        int result = Integer.MAX_VALUE;
        for (PotionEffect effect : effects) {
            if (effect.getDuration() < result) result = effect.getDuration();
        }
        return result;
    }

    public static final TriConsumer<String, Map<String, String>, Entity> ADD_PASSENGER = (formula, data, base) -> {
        // passenger=~~~
        final String pattern = "(predicate=(true|false)/)?\\+([a-zA-Z0-9_-]+)";
        addEntityAndWorldData(data, base);
        data.put("$CURRENT_TARGET_PASSENGERS_COUNT$", String.valueOf(base.getPassengers().size()));
        formula = CalcUtil.getContent(data, formula);

        ContainerUtil.removeCurrentVariables(data);
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        Entity passenger = DEFINED_ENTITIES.get(data.get(parsed.group(3)));
        if (passenger == null) return;
        base.addPassenger(passenger);
    };

    public static final TriConsumer<String, Map<String, String>, Entity> SET_ARMOR = (formula, data, base) -> {
        // (helmet|chest|leggings|boots)=~~~
        final String pattern = "(predicate=(true|false)/)?(helmet|chest|leggings|boots|mainhand|offhand)=([$.a-zA-Z0-9_-]+)";
        addEntityAndWorldData(data, base);
        formula = CalcUtil.getContent(data, formula);

        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return;
        if (parsed.group(2) != null && !Boolean.parseBoolean(parsed.group(2))) return;
        ItemStack item = ContainerUtil.DEFINED_ITEMS.get("$" + data.get(UNIQUE_ID_KEY) + "." + parsed.group(4) + "$");

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
        //Attribute attribute = Attribute.valueOf(parsed.group(3).toUpperCase());
        Attribute attribute;
        if ((attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(parsed.group(2).toUpperCase()))) == null) return;
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(parsed.group(4).toUpperCase());
        double value = Double.parseDouble(parsed.group(5));
        //EquipmentSlot slot = parsed.group(6) != null ? EquipmentSlot.valueOf(parsed.group(6)) : null;
        EquipmentSlotGroup group = EquipmentSlotGroup.getByName(parsed.group(6).toUpperCase());
        AttributeModifier modifier;
        //if (slot != null) modifier = new AttributeModifier(UUID.randomUUID(), UUID.randomUUID().toString(), value, operation, slot);
        NamespacedKey randomNamespacedKey = new NamespacedKey(CustomCrafter.getInstance(), UUID.randomUUID().toString());
        if (group != null) modifier = new AttributeModifier(randomNamespacedKey, value, operation, group);
        else modifier = new AttributeModifier(randomNamespacedKey, value, operation);

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

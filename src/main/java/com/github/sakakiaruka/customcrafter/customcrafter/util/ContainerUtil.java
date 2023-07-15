package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.regex.qual.Regex;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;

public class ContainerUtil {
    public static final String ALLOW_TAG = "allow_tag";
    public static final String ALLOW_VALUE = "allow_value";
    public static final String DENY_TAG = "deny_tag";
    public static final String DENY_VALUE = "deny_value";

    //---
    private static final String ORDER_NUMBER_PATTERN = "_\\d{1,2}";
    private static final String ARROW_RANGE_PATTERN = "(_?[\\d]{1,2})<-->(_?[\\d]{1,2})";
    private static final String LARGER_PATTERN = "";
    private static final String SMALLER_PATTERN = "";

    private PersistentDataType getDataType(String input) {
        if (input.equalsIgnoreCase("string")) return PersistentDataType.STRING;
        if (input.equalsIgnoreCase("int")) return PersistentDataType.INTEGER;
        return null;
    }

    public Map<String, List<ContainerWrapper>> mattersLoader(Path path) {
        Map<String, List<ContainerWrapper>> map = new LinkedHashMap<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        int current = 0;
        while (true) {
            if (!config.contains("terms."+current)) break;
            String thumb =  "terms."+current+".";

            String tag = config.getString(thumb+"tag");
            int order = config.getInt(thumb+"order");
            NamespacedKey key = new NamespacedKey(getInstance(), config.getString(thumb+"key"));
            PersistentDataType type = getDataType(config.getString(thumb+"type"));
            String value = null;
            if (config.contains(thumb+"value")) {
                value = config.getString(thumb+"value");
            }

            ContainerWrapper wrapper = new ContainerWrapper(key, type, value,order,tag);

            if (!map.containsKey(tag)) map.put(tag, new ArrayList<>());
            map.get(tag).add(wrapper);

            current++;
        }
        return map;
    }

    public boolean isPass(ItemStack item, Matter matter) {
        if (!matter.hasContainer()) return true;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        for(Map.Entry<String, List<ContainerWrapper>> entry : matter.getContainerWrappers().entrySet()) {
            for (ContainerWrapper wrapper : entry.getValue()) {
                String tag = wrapper.getTag();
                NamespacedKey key = wrapper.getKey();
                PersistentDataType type = wrapper.getType();
                String value = wrapper.getValue();
                int order = wrapper.getOrder();

                if (tag.equalsIgnoreCase(ALLOW_TAG)) {
                    if (container.has(key, type)) continue;
                    return false;
                }

                if (tag.equals(ALLOW_VALUE)) {
                    if (!container.has(key, type)) return false;
                    String element = String.valueOf(container.get(key, type));
                    if (value.matches(ARROW_RANGE_PATTERN)) {
                        //
                    }


                }
            }
        }
    }

    private boolean arrowPatternOperation(String source, PersistentDataContainer container, ContainerWrapper wrapper) {
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        String element = String.valueOf(container.get(key,type));

        Pattern pattern = Pattern.compile(ARROW_RANGE_PATTERN);
        Matcher matcher = pattern.matcher(element);
        int start =
        String start = matcher.group(1);
        String end = matcher.group(2);
    }

    private int getSpecifiedOrderElements(int order, PersistentDataContainer container, ContainerWrapper wrapper) {
        List<String> contents = new ArrayList<>(wrapper.get)
    }
}

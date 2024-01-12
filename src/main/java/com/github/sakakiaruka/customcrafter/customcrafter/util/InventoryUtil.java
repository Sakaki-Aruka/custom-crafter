package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class InventoryUtil {

    private static final String LEATHER_ARMOR_COLOR_RGB_PATTERN = "^type:(?i)(RGB)/value:R->(\\d{1,3}),G->(\\d{1,3}),B->(\\d{1,3})$";
    private static final String LEATHER_ARMOR_COLOR_NAME_PATTERN = "^type:(?i)(NAME)/value:([\\w_]+)$";
    private static final String LEATHER_ARMOR_COLOR_RANDOM_PATTERN = "^type:(?i)(RANDOM)$";

    public static List<Integer> getTableSlots(int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int result = i * 9 + j;
                list.add(result);
            }
        }

        return list;
    }

    public static List<Integer> getBlankCoordinates(int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < CRAFTING_TABLE_TOTAL_SIZE; i++) {
            list.add(i);
        }
        list.removeAll(getTableSlots(size));
        list.removeAll(Arrays.asList(CRAFTING_TABLE_MAKE_BUTTON));
        list.removeAll(Arrays.asList(CRAFTING_TABLE_RESULT_SLOT));
        return list;
    }

    public static void decrementMaterials(Inventory inventory, int amount) {
        // decrement crafting tables material
        // amount -> decrement amount
        List<Integer> slots = getTableSlots(CRAFTING_TABLE_SIZE);
        for (int i : slots) {
            if (inventory.getItem(i) == null) continue;
            int oldAmount = inventory.getItem(i).getAmount();
            int newAmount = Math.max(oldAmount - amount, 0);
            inventory.getItem(i).setAmount(newAmount);
        }
    }

    public static void decrementResult(Inventory inventory, Player player) {
        if (inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) == null) return;
        ItemStack item = inventory.getItem(CRAFTING_TABLE_RESULT_SLOT);
        InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, new ItemStack(Material.AIR));
    }

    public static void returnItems(Recipe recipe, Inventory inventory, int removeAmount, Player player) {
        if (recipe.getReturnItems().isEmpty()) return;
        List<Material> isMassList = new ArrayList<>();
        recipe.getContentsNoAir().forEach(s -> {
            if (s.isMass()) isMassList.add(s.getCandidate().get(0));
        });

        for (ItemStack item : inventory) {
            if (item == null) continue;
            if (!recipe.getReturnItems().containsKey(item.getType())) continue;
            int returnAmount = recipe.getReturnItems().get(item.getType()).getAmount();
            if (!isMassList.contains(item.getType())) returnAmount *= removeAmount;
            ItemStack itemStack = recipe.getReturnItems().get(item.getType()).clone();
            if (!itemStack.getType().equals(Material.AIR)) {
                drop(itemStack, returnAmount, player);
                continue;
            }

            // pass through return
            drop(item, returnAmount, player);
        }
    }

    private static void drop(ItemStack item, int returnAmount, Player player) {
        item.setAmount(returnAmount);
        InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
    }



    public static List<ItemStack> getItemStackFromCraftingMenu(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>();
        for (int i : getTableSlots(CRAFTING_TABLE_SIZE)) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType().equals(Material.AIR)) continue;
            result.add(inventory.getItem(i));
        }
        return result;
    }

    // === book field set ===
    public static void setAuthor(BookMeta meta, String value) {
        meta.setAuthor(value);
    }

    public static void setTitle(BookMeta meta, String value) {
        meta.setTitle(value);
    }

    public void setPage(BookMeta meta, int page, String value) {
        // page specified
        if (page < 0 || 100 < page) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (setPage) failed. (Illegal insert page.)");
            return;
        }
        if (!isValidPage(meta, "setPage")) return;
        meta.setPage(page, value);
    }

    public static void setPages(BookMeta meta, String value) {
        // set page un-specified page
        meta.setPages(value);
    }

    public static void setGeneration(BookMeta meta, String value) {
        // set book-generation
        BookMeta.Generation generation;
        try {
            generation = BookMeta.Generation.valueOf(value.toUpperCase());
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (setGeneration) failed. (Illegal BOOK_GENERATION)");
            return;
        }
        meta.setGeneration(generation);
    }

    public static void addPage(BookMeta meta, String value) {
        // add page
        String section = "addPage";
        if (!isValidPage(meta, section)) return;
        if (!isValidCharacters(value, section)) return;
        if (!isValidCharacters(meta, value, section)) return;
        meta.addPage(value);
    }

    private static boolean isValidPage(BookMeta meta, String section) {
        if (100 <= meta.getPageCount()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (" + section + ") failed. (Over 50 pages.)");
            return false;
        }
        return true;
    }

    private static boolean isValidCharacters(String value, String section) {
        if (320 < value.length()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (" + section + ") failed. (Over 256 characters.)");
            return false;
        }
        return true;
    }

    private static boolean isValidCharacters(BookMeta meta, String value, String section) {
        if (25600 < (meta.getPageCount() * 320) + value.length()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (" + section + ") failed. (Over 25600 characters.)");
            return false;
        }
        return true;
    }

    public static void addLong(BookMeta meta, String value, boolean extend) {
        // 320 -> the characters limit that about one page.
        // 25600 -> the characters limit that about one book.
        // 14 -> the lines limit that about one page.
        int ONE_BOOK_CHAR_LIMIT = 25600;
        String PATTERN = "[a-zA-Z0-9\\-.+*/=%'\"#@_(),;:?!|{}<>§\\[\\]$]";
        String section = "addLong";

        if (extend) {
            try {
                value = String.join(LINE_SEPARATOR, Files.readAllLines(Paths.get(value), StandardCharsets.UTF_8));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (addLong - extend) failed. (About a file read.)");
                Bukkit.getLogger().warning(e.getMessage());
                return;
            }
        }

        if (!isValidCharacters(meta, value, section)) return;

        if (ONE_BOOK_CHAR_LIMIT < value.length()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (addLong) failed. (Over 25600 characters.)");
            meta.addPage("Overflown");
            return;
        }

        int horizontal = 0;
        int vertical = 0;
        StringBuilder element = new StringBuilder();

        // 22 -> horizontal limit
        // 14 -> vertical limit

        for (int i = 0; i < value.length(); i++) {
            String target = String.valueOf(value.charAt(i));
            int evaluation = target.matches(PATTERN) ? 1 : 2;

            if ((22 <= (horizontal + evaluation) || target.equals(LINE_SEPARATOR)) && vertical == 14) {
                // make a new page

                if (meta.getPageCount() == 100) {
                    Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (addLong) failed. (Over 100 pages.)");
                    Bukkit.getLogger().warning("[CustomCrafter] Remaining " + (element.capacity() + (value.length() - i)) + " characters.");
                    return;
                }

                meta.addPage(element.toString());
                element.setLength(0);

                horizontal = evaluation;
                element.append(target);

                vertical = 0;

            } else if ((22 <= (horizontal + evaluation) || target.equals(LINE_SEPARATOR)) && vertical < 14) {
                // make a new line
                horizontal = evaluation;
                element.append(target);

                vertical++;

            } else {
                // a character add
                element.append(target);
                horizontal += evaluation;
            }
        }

        meta.addPage(element.toString()); // add remaining string
    }


    // leather_armor_color modify
    private static Color getColor(String value) {
        // if the value is not a color-name, returns null.
        Color color;
        value = value.toUpperCase();
        if (value.equals("AQUA")) color = Color.AQUA;
        else if (value.equals("BLACK")) color = Color.BLACK;
        else if (value.equals("BLUE")) color = Color.BLUE;
        else if (value.equals("FUCHSIA")) color = Color.FUCHSIA;
        else if (value.equals("GRAY")) color = Color.GRAY;
        else if (value.equals("GREEN")) color = Color.GREEN;
        else if (value.equals("LIME")) color = Color.LIME;
        else if (value.equals("MAROON")) color = Color.MAROON;
        else if (value.equals("NAVY")) color = Color.NAVY;
        else if (value.equals("OLIVE")) color = Color.OLIVE;
        else if (value.equals("ORANGE")) color = Color.ORANGE;
        else if (value.equals("PURPLE")) color = Color.PURPLE;
        else if (value.equals("RED")) color = Color.RED;
        else if (value.equals("SILVER")) color = Color.SILVER;
        else if (value.equals("TEAL")) color = Color.TEAL;
        else if (value.equals("WHITE")) color = Color.WHITE;
        else if (value.equals("YELLOW")) color = Color.YELLOW;
        else {
            Bukkit.getLogger().warning("[CustomCrafter] (ColorName) failed. Input -> " + value);
            return null;
        }
        return color;
    }

    private static String getLeatherArmorColorWarningUnMatchPattern() {
        String result =
                "[CustomCrafter] Set result metadata (LeatherArmorColor) failed. (Illegal data format.)" + LINE_SEPARATOR
                        + "Follow the patterns." + LINE_SEPARATOR
                        + "- " + LEATHER_ARMOR_COLOR_RGB_PATTERN + LINE_SEPARATOR
                        + "- " + LEATHER_ARMOR_COLOR_NAME_PATTERN + LINE_SEPARATOR
                        + "- " + LEATHER_ARMOR_COLOR_RANDOM_PATTERN + LINE_SEPARATOR;
        return result;
    }

    public static void setLeatherArmorColorFromRGB(LeatherArmorMeta meta, String value) {
        Matcher matcher = Pattern.compile(LEATHER_ARMOR_COLOR_RGB_PATTERN).matcher(value);
        if (!matcher.matches()) {
            Bukkit.getLogger().warning(getLeatherArmorColorWarningUnMatchPattern());
            return;
        }

        int RED = Integer.parseInt(matcher.group(2));
        int GREEN = Integer.parseInt(matcher.group(3));
        int BLUE = Integer.parseInt(matcher.group(4));
        Color color;
        try {
            color = Color.fromRGB(RED, GREEN, BLUE);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (LeatherArmorColor) failed. (Illegal RGB elements.)");
            e.printStackTrace();
            return;
        }
        meta.setColor(color);
    }

    public static void setLeatherArmorColorFromName(LeatherArmorMeta meta, String value) {
        Matcher matcher = Pattern.compile(LEATHER_ARMOR_COLOR_NAME_PATTERN).matcher(value);
        if (!matcher.matches()) {
            Bukkit.getLogger().warning(getLeatherArmorColorWarningUnMatchPattern());
            return;
        }
        Color color;
        if ((color = getColor(matcher.group(2))) == null) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (LeatherArmorColor) failed. (Illegal color-name.)" + LINE_SEPARATOR
                    + "You can use 'AQUA', 'BLACK', 'BLUE', 'FUCHSIA', 'GRAY', 'GREEN', 'LIME', 'MAROON', 'NAVY', 'OLIVE', 'ORANGE', 'PURPLE', 'RED', 'SILVER', 'TEAL', 'WHITE' and 'YELLOW'."
                    + LINE_SEPARATOR);
            return;
        }
        meta.setColor(color);
    }

    public static void setLeatherArmorColorRandom(LeatherArmorMeta meta) {
        Random random = new Random();
        int RED = random.nextInt(256);
        int GREEN = random.nextInt(256);
        int BLUE = random.nextInt(256);
        Color color = Color.fromRGB(RED, GREEN, BLUE);
        meta.setColor(color);
    }

    public static void safetyItemDrop(Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            Item dropped = player.getWorld().dropItem(player.getLocation(), item);
            dropped.setOwner(player.getUniqueId());
            dropped.setGravity(false);
        }
    }

    public static void enchantModify(String action, String value, ItemMeta meta) {
        if (!meta.hasEnchants()) return;
        if (action.equals("add")) {
            Matcher v = Pattern.compile("enchant=([\\w_]+),level=([\\d]+)").matcher(value);
            if (!v.matches()) return;
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(v.group(1).toLowerCase()));
            int level = Integer.parseInt(v.group(2));
            meta.removeEnchant(enchant);
            meta.addEnchant(enchant, level, false);
        } else if (action.equals("remove")) {

            //debug
            Bukkit.getLogger().info("enchant REMOVE=" + value);

            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(value.toLowerCase()));
            meta.removeEnchant(enchant);
        }
    }

    public static void enchantLevel(String action, String value, ItemMeta meta) {
        // if the specified enchantment is not contained the item-stack, this method does nothing.
        if (!meta.hasEnchants()) return;
        Matcher v = Pattern.compile("enchant=([\\w_]+),change=(\\d+)").matcher(value);
        if (!v.matches()) return;
        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(v.group(1).toLowerCase()));
        int change = Integer.parseInt(v.group(2));

        int oldLevel = meta.getEnchantLevel(enchant);
        meta.removeEnchant(enchant);
        int newLevel = 1;
        if (action.equals("minus")) newLevel = Math.max(1, oldLevel - change);
        else if (action.equals("plus")) newLevel = Math.min(255, oldLevel + change);
        meta.addEnchant(enchant, newLevel, true);
    }

    public static void loreModify(String action, String value, ItemMeta meta) {
        switch (action) {
            case "add": {
                List<String> lore = meta.getLore();
                meta.setLore(null); // clear the lore

                lore.add(value);
                meta.setLore(lore);
                break;
            }
            case "clear":
                meta.setLore(null);
                break;
            case "modify": {
                List<String> lore = meta.getLore();
                meta.setLore(null); // clear the lore
                Matcher v = Pattern.compile("line=([\\d]+),lore=(.+)").matcher(value);
                if (!v.matches()) return;
                int line = Integer.parseInt(v.group(1));
                String add = v.group(2);

                if (lore.size() < line) return;
                lore.add(line, add);
                meta.setLore(lore);
                break;
            }
        }
    }

    public static void durabilityModify(String action, String value, ItemMeta meta) {
        Damageable damageable;
        try {
            damageable = (Damageable) meta;
        } catch (Exception e) {
            return;
        }

        double oldHealth = damageable.getHealth();
        List<AttributeModifier> modifiers = (List<AttributeModifier>) meta.getAttributeModifiers(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = modifiers.get(0).getAmount();
        double lastOne = maxHealth - 1;
        double change = Double.parseDouble(value);

        if (action.equalsIgnoreCase("minus")) {
            // minus
            double damage = Math.min(lastOne, change);
            damageable.damage(damage);
        } else if (action.equalsIgnoreCase("plus")) {
            // plus
            double newHealth = Math.min(maxHealth, oldHealth + change);
            damageable.setHealth(newHealth);
        }
    }


    public static void armorColor(String action, String value, ItemMeta meta) {
        LeatherArmorMeta leatherMeta;
        try {
            leatherMeta = (LeatherArmorMeta) meta;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] Failed to convert metadata to LeatherArmorMeta. (Not LeatherArmor)");
            return;
        }

        if (action.equalsIgnoreCase("name")) {
            String query = "type:name/value:" + value;
            setLeatherArmorColorFromName(leatherMeta, query);
        } else if (action.equalsIgnoreCase("rgb")) {
            String query = "type:rgb/value:" + value.replace("=", "->");
            setLeatherArmorColorFromRGB(leatherMeta, query);
        } else if (action.equalsIgnoreCase("random")) {
            Bukkit.getLogger().info("[CustomCrafter] The specified value (=" + value + ") is not used.");
            setLeatherArmorColorRandom(leatherMeta);
        }
    }

    public static void textureIdModify(String action, String value, ItemMeta meta) {
        if (action.equals("clear")) {
            meta.setCustomModelData(null);
        } else if (action.equals("modify")) {
            try {
                meta.setCustomModelData(Integer.parseInt(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void displayNameModify(String action, String value, ItemMeta meta) {
        if (action.equals("clear")) {
            meta.setDisplayName(null);
        } else if (action.equals("modify")) {
            if (value.isEmpty()) meta.setDisplayName(null); // clear
            meta.setDisplayName(value);
        }
    }

    public static void itemFlagModify(String action, String value, ItemMeta meta) {
        if (action.equals("clear")) {
            if (meta.getItemFlags().isEmpty()) return;
            meta.getItemFlags().forEach(flag-> meta.removeItemFlags(flag));
        } else if (action.equals("add")) {
            ItemFlag flag;
            try {
                flag = ItemFlag.valueOf(value.toUpperCase());
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] Failed to add item-flag in Pass-through. (Invalid ItemFlag)");
                e.printStackTrace();
                return;
            }

            meta.addItemFlags(flag);
        } else if (action.equals("remove")) {
            ItemFlag target;
            try {
                target = ItemFlag.valueOf(value.toUpperCase());
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] Failed to remove item-flag in Pass-through. (Invalid ItemFlag)");
                e.printStackTrace();
                return;
            }

            meta.removeItemFlags(target);
        }
    }

    public static Map<Coordinate, List<Coordinate>> amorphous(Recipe recipe, Recipe input) {
        Map<Coordinate, List<Coordinate>> map = new HashMap<>();
        for (Coordinate r : recipe.getCoordinateList()) {
            List<Material> rCandidate = recipe.getMatterFromCoordinate(r).getCandidate();
            for (Coordinate i : input.getCoordinateList()) {
                List<Material> iCandidate = input.getMatterFromCoordinate(i).getCandidate();
                if (!rCandidate.isEmpty() && !iCandidate.isEmpty() && new HashSet<>(rCandidate).containsAll(iCandidate)) {
                    if (!map.containsKey(r)) map.put(r, new ArrayList<>());
                    map.get(r).add(i);
                }
            }
        }
        return map;
    }

    public static Map<Coordinate, Map<String, Boolean>> getEachMatterStatus(Recipe recipe) {
        Map<Coordinate, Map<String, Boolean>> map = new HashMap<>();
        for (Coordinate coordinate : recipe.getCoordinateList()) {
            Map<String, Boolean> element = new HashMap<>();
            Matter current = recipe.getMatterFromCoordinate(coordinate);
            element.put("potion", current instanceof Potions);
            element.put("enchant", current.hasWrap());
            element.put("container", current.hasContainer());
            map.put(coordinate, element);
        }
        return map;
    }

    public static Map<Coordinate, Coordinate> combination(List<Map<Coordinate, List<Coordinate>>> input) {
        Map<Coordinate, List<Coordinate>> merged = new HashMap<>();
        for (Map<Coordinate, List<Coordinate>> element : input) {
            for (Map.Entry<Coordinate, List<Coordinate>> entry : element.entrySet()) {
                if (!merged.containsKey(entry.getKey())) {
                    merged.put(entry.getKey(), entry.getValue());
                    continue;
                }

                List<Coordinate> a = merged.get(entry.getKey());
                List<Coordinate> b = entry.getValue();
                List<Coordinate> both = bothContained(a, b);
                if (both.isEmpty()) return new HashMap<>();
                merged.put(entry.getKey(), both);
            }
        }

        Bukkit.getLogger().info("merged map="+merged);

        Map<Coordinate, List<Coordinate>> conflict = new HashMap<>();
        Map<Coordinate, Coordinate> finished = new HashMap<>();
        for (Map.Entry<Coordinate, List<Coordinate>> entry : merged.entrySet()) {
            if (entry.getValue().size() == 1) {
                if (!finished.containsKey(entry.getKey())) {
                    finished.put(entry.getKey(), entry.getValue().get(0));
                    continue;
                }
                // finished has already contained this value
                Bukkit.getLogger().info("combination error (value duplication)");
                return new HashMap<>();
            }

            conflict.put(entry.getKey(), entry.getValue());
        }

        Bukkit.getLogger().info("finished="+finished);
        Bukkit.getLogger().info("conflict="+conflict);

        if (hasDuplicate(finished)) {
            Bukkit.getLogger().info("contains duplicate coordinate.");
            return new HashMap<>();
        }

        // gen combination
        List<Integer> sizes = new ArrayList<>();
        conflict.forEach((k, e) -> sizes.add(e.size()));

        int[] arr = new int[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            arr[i] = sizes.get(i);
        }

        int multi = getMultiply(-1, arr);
        int[][] result = new int[multi][arr.length];
        for (int x = 0; x < arr.length; x++) {
            int times = getMultiply(x, arr);
            for (int y = 0; y < multi; y++) {
                int current = (y / times) % arr[x];
                result[y][x] = current;
            }
        }

        Set<String> non_duplicate = new HashSet<>();
        A : for (int[] ss : result) {
            List<Integer> buffer = new ArrayList<>();
            for (int element : ss) {
                if (buffer.contains(element)) continue A;
                buffer.add(element);
            }
            non_duplicate.add(Arrays.toString(ss));
        }

        Bukkit.getLogger().info("non_duplicate="+non_duplicate);

        List<String> non_duplcicate_list = new ArrayList<>(non_duplicate);
        for (int i = 0; i < non_duplcicate_list.size(); i++) {
            String[] temp = non_duplcicate_list.get(i).split(",");
            int index = 0;
            for (Map.Entry<Coordinate, List<Coordinate>> entry : conflict.entrySet()) {
                Coordinate key = entry.getKey();

                // debug ^ 2
                Bukkit.getLogger().info("conflict loop (i="+i+", temp[index])="+temp[index]);

                String indexParse = temp[index]
                        .replace("[", "")
                        .replace("]", "")
                        .replace(" ", "");
                Coordinate value = entry.getValue().get(Integer.parseInt(indexParse));
                finished.put(key, value);

                for (Map.Entry<Coordinate, List<Coordinate>> e : conflict.entrySet()) {
                    if (e.getValue().contains(value)) {
                        List<Coordinate> conflict_temp = conflict.get(key);
                        conflict_temp.remove(value);
                    }
                }
                index++;
            }

            if (hasDuplicate(finished)) {
                Bukkit.getLogger().info("contains duplicate coordinate (in loop)");
                continue;
            }

            Bukkit.getLogger().info("finished (index="+index+")="+finished);
            return finished;
        }

        Bukkit.getLogger().info("recipe match failed.");
        return new HashMap<>();
    }

    private static boolean hasDuplicate(Map<Coordinate, Coordinate> map) {
        return new HashSet<>(map.values()).size() != map.values().size();
    }

    private static int getMultiply(int current, int[] arr) {
        int result = 1;
        if (current == arr.length - 1) return 1;
        for (int i = current + 1; i < arr.length; i++) {
            result *= arr[i];
        }
        return result;
    }

    private static List<Coordinate> bothContained(List<Coordinate> a, List<Coordinate> b) {
        List<Coordinate> result = new ArrayList<>();
        List<Coordinate> ed = a.size() <= b.size() ? b : a;
        List<Coordinate> er = a.size() <= b.size() ? a : b;
        for (Coordinate coordinate : er) {
            if (ed.contains(coordinate)) result.add(coordinate);
        }
        return result;
    }
}
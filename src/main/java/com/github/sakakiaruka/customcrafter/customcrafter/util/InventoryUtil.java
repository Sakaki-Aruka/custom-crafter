package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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
import java.util.Set;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class InventoryUtil {
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
        list.removeAll(List.of(CRAFTING_TABLE_MAKE_BUTTON));
        list.removeAll(List.of(CRAFTING_TABLE_RESULT_SLOT));
        return list;
    }

    public static void decrementMaterials(Inventory inventory, int amount) {
        // decrement crafting tables material
        // amount -> decrement amount
        List<Integer> slots = getTableSlots(CRAFTING_TABLE_SIZE);
        for (int i : slots) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType().equals(Material.AIR)) continue;
            int oldAmount = inventory.getItem(i).getAmount();
            int newAmount = Math.max(oldAmount - amount, 0);
            inventory.getItem(i).setAmount(newAmount);
        }
    }

    public static void decrementMaterialsForNormalRecipe(Inventory inventory, Recipe input, Recipe recipe) {
        // input list is a list whose size is (9 * 6 =)54.
        for (int i = 0; i < recipe.getContentsNoAir().size(); i++) {
            Matter r = recipe.getContentsNoAir().get(i);
            int slot = input.getCoordinateNoAir().get(i).getY() * 9 + input.getCoordinateNoAir().get(i).getX();
            if (inventory.getItem(slot) == null) continue;
            int amount = Math.max(inventory.getItem(slot).getAmount() -  r.getAmount(), 0);
            inventory.getItem(slot).setAmount(amount);
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
            meta.addPages(Component.text("Overflown"));
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

                meta.addPages(Component.text(element.toString()));
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

        meta.addPages(Component.text(element.toString())); // add remaining string
    }


    // leather_armor_color modify
    public static Color getColor(String value) {
        // if the value is not a color-name, returns null.
        Color color;
        value = value.toUpperCase();
        switch (value) {
            case "AQUA" -> color = Color.AQUA;
            case "BLACK" -> color = Color.BLACK;
            case "BLUE" -> color = Color.BLUE;
            case "FUCHSIA" -> color = Color.FUCHSIA;
            case "GRAY" -> color = Color.GRAY;
            case "GREEN" -> color = Color.GREEN;
            case "LIME" -> color = Color.LIME;
            case "MAROON" -> color = Color.MAROON;
            case "NAVY" -> color = Color.NAVY;
            case "OLIVE" -> color = Color.OLIVE;
            case "ORANGE" -> color = Color.ORANGE;
            case "PURPLE" -> color = Color.PURPLE;
            case "RED" -> color = Color.RED;
            case "SILVER" -> color = Color.SILVER;
            case "TEAL" -> color = Color.TEAL;
            case "WHITE" -> color = Color.WHITE;
            case "YELLOW" -> color = Color.YELLOW;
            default -> {
                Bukkit.getLogger().warning("[CustomCrafter] (ColorName) failed. Input -> " + value);
                return null;
            }
        }
        return color;
    }


    public static void safetyItemDrop(Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            Item dropped = player.getWorld().dropItem(player.getLocation(), item);
            dropped.setOwner(player.getUniqueId());
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
            element.put("container", current.hasContainers());
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

//        Bukkit.getLogger().info("merged map="+merged);

        Map<Coordinate, List<Coordinate>> conflict = new HashMap<>();
        Map<Coordinate, Coordinate> finished = new HashMap<>();
        for (Map.Entry<Coordinate, List<Coordinate>> entry : merged.entrySet()) {
            if (entry.getValue().size() == 1) {
                if (!finished.containsKey(entry.getKey())) {
                    finished.put(entry.getKey(), entry.getValue().get(0));
                    continue;
                }
                // finished has already contained this value
//                Bukkit.getLogger().info("combination error (value duplication)");
                return new HashMap<>();
            }

            conflict.put(entry.getKey(), entry.getValue());
        }

//        Bukkit.getLogger().info("finished="+finished);
//        Bukkit.getLogger().info("conflict="+conflict);

        if (hasDuplicate(finished)) {
//            Bukkit.getLogger().info("contains duplicate coordinate.");
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

//        Bukkit.getLogger().info("non_duplicate="+non_duplicate);

        List<String> non_duplcicate_list = new ArrayList<>(non_duplicate);
        for (int i = 0; i < non_duplcicate_list.size(); i++) {
            String[] temp = non_duplcicate_list.get(i).split(",");
            int index = 0;
            for (Map.Entry<Coordinate, List<Coordinate>> entry : conflict.entrySet()) {
                Coordinate key = entry.getKey();

                // debug
//                Bukkit.getLogger().info("conflict loop (i="+i+", temp[index])="+temp[index]);

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
//                Bukkit.getLogger().info("contains duplicate coordinate (in loop)");
                continue;
            }

//            Bukkit.getLogger().info("finished (index="+index+")="+finished);
            return finished;
        }

//        Bukkit.getLogger().info("recipe match failed.");
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
package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class EnchantUtil {

    public static final Set<Enchantment> CURSED_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(Enchantment::isCursed).collect(Collectors.toSet());
    public static final Set<Enchantment> DISCOVERABLE_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable).collect(Collectors.toSet());
    public static final Set<Enchantment> TRADEABLE_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(Enchantment::isTradeable).collect(Collectors.toSet());
    public static final Set<Enchantment> TREASURE_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(Enchantment::isTreasure).collect(Collectors.toSet());
    public static final Set<Enchantment> COMMON_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(e -> e.getRarity().equals(EnchantmentRarity.COMMON)).collect(Collectors.toSet());
    public static final Set<Enchantment> RARE_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(e -> e.getRarity().equals(EnchantmentRarity.RARE)).collect(Collectors.toSet());
    public static final Set<Enchantment> UNCOMMON_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(e -> e.getRarity().equals(EnchantmentRarity.UNCOMMON)).collect(Collectors.toSet());
    public static final Set<Enchantment> VERY_RARE_ENCHANTMENTS = Registry.ENCHANTMENT.stream().filter(e -> e.getRarity().equals(EnchantmentRarity.VERY_RARE)).collect(Collectors.toSet());

//====
    public static Map<Coordinate, List<Coordinate>> amorphous(Recipe recipe, Recipe input) {
        // returns candidate of correct pattern

        // returns NON_REQUIRED = containers not required
        // returns NULL         = not enough containers in an input
        Map<Coordinate, List<Coordinate>> result = new HashMap<>();
        List<Coordinate> r = recipe.getEnchantedItemCoordinateList();
        List<Coordinate> i = input.getEnchantedItemCoordinateList();

        List<List<EnchantWrap>> x = input.getEnchantedItemList();

        if (r.size() > i.size()) return Search.AMORPHOUS_NULL_ANCHOR;
        if (r.isEmpty()) return Search.AMORPHOUS_NON_REQUIRED_ANCHOR;

        Map<Integer, List<Integer>> map = new HashMap<>();
        for (int j = 0; j < r.size(); j++) {
            List<EnchantWrap> cell = new ArrayList<>();
            for (EnchantWrap element : recipe.getMatterFromCoordinate(r.get(j)).getWrap()) {
                if (element.getStrict().equals(EnchantStrict.NOTSTRICT)) continue;
                cell.add(element);
            }

            List<Boolean> list = getMatchList(cell, x);
            List<Integer> toIndex = new ArrayList<>();
            for (int k = 0; k < list.size(); k++) {
                if (list.get(k)) toIndex.add(k);
            }
            map.put(j, toIndex);

        }

        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Coordinate R = r.get(entry.getKey());
            for (int u : entry.getValue()) {
                Coordinate I = i.get(u);
                if (!result.containsKey(R)) result.put(R, new ArrayList<>());
                result.get(R).add(I);
            }
        }

        return result.isEmpty() ? Search.AMORPHOUS_NULL_ANCHOR : result;
    }

    private static List<Boolean> getMatchList(List<EnchantWrap> recipe, List<List<EnchantWrap>> in) {
        List<Boolean> list = new ArrayList<>();
        for (List<EnchantWrap> cell : in) {
            int a = 0;
            for (EnchantWrap j : recipe) {
                int b = 0;
                for (EnchantWrap element : cell) {
                    if (j.getStrict().equals(EnchantStrict.NOTSTRICT)) b += 1;
                    if (j.getStrict().equals(EnchantStrict.ONLYENCHANT)) {
                        b += j.getEnchant().equals(element.getEnchant()) ? 1 : 0;
                    }
                    if (j.getStrict().equals(EnchantStrict.STRICT)){
                        b += (j.getEnchant().equals(element.getEnchant()) && j.getLevel() == element.getLevel()) ? 1 : 0;
                    }
                }
                a += b > 0 ? 1 : 0;
            }
            list.add(a == recipe.size());
        }
        return list;
    }

    static Enchantment getRandomEnchantment(Set<Enchantment> contained, String formula) {
        // e.g. random[all,!self] (from all, but does not contain self)
        // e.g. random[all] (from all)
        // e.g. random[fortune,lure,looting] (from these 3)
        // e.g. random[all,!fortune,!lure,!looting] (from all that does not contain these 3)

        final String pattern = "random(\\[[!A-Za-z,_]+])";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) {
            return null;
        }

        Set<Enchantment> candidate = new HashSet<>();
        for (String element : parsed.group(1).split(",")) {
            boolean ignore = element.startsWith("!");
            element = element.replace("!", "");
            switch (element) {
                case "all" -> {
                    if (ignore) candidate.clear();
                    else candidate.addAll(Registry.ENCHANTMENT.stream().collect(Collectors.toSet()));
                }

                case "self" -> {
                    if (ignore) candidate.removeAll(contained);
                    else candidate.addAll(contained);
                }

                case "none" -> {
                    if (!ignore) candidate.add(null);
                }

                case "cursed" -> {
                    if (ignore) candidate.removeAll(CURSED_ENCHANTMENTS);
                    else candidate.addAll(CURSED_ENCHANTMENTS);
                }

                case "discoverable" -> {
                    if (ignore) candidate.removeAll(DISCOVERABLE_ENCHANTMENTS);
                    else candidate.addAll(DISCOVERABLE_ENCHANTMENTS);
                }

                case "tradeable" -> {
                    if (ignore) candidate.removeAll(TRADEABLE_ENCHANTMENTS);
                    else candidate.addAll(TRADEABLE_ENCHANTMENTS);
                }

                case "treasure" -> {
                    if (ignore) candidate.removeAll(TREASURE_ENCHANTMENTS);
                    else candidate.addAll(TREASURE_ENCHANTMENTS);
                }

                case "common" -> {
                    if (ignore) candidate.removeAll(COMMON_ENCHANTMENTS);
                    else candidate.addAll(COMMON_ENCHANTMENTS);
                }

                case "rare" -> {
                    if (ignore) candidate.removeAll(RARE_ENCHANTMENTS);
                    else candidate.addAll(RARE_ENCHANTMENTS);
                }

                case "uncommon" -> {
                    if (ignore) candidate.removeAll(UNCOMMON_ENCHANTMENTS);
                    else candidate.addAll(UNCOMMON_ENCHANTMENTS);
                }

                case "very_rare" -> {
                    if (ignore) candidate.removeAll(VERY_RARE_ENCHANTMENTS);
                    else candidate.addAll(VERY_RARE_ENCHANTMENTS);
                }

                default -> {
                    Enchantment enchant;
                    try {
                        enchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(element.toLowerCase()));
                    } catch (Exception e) {
                        ContainerUtil.sendOrdinalWarn(element + " is not a valid enchantment.");
                        continue;
                    }

                    if (ignore) candidate.remove(enchant);
                    else candidate.remove(enchant);
                }
            }
        }
        if (candidate.isEmpty()) return null;
        return new ArrayList<>(candidate).get(new Random().nextInt(candidate.size()));
    }
}

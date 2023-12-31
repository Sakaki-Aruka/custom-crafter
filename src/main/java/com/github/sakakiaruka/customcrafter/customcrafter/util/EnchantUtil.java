package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EnchantUtil {



    public List<String> strValuesNoInput(){
        List<String> list = new ArrayList<>();
        for(EnchantStrict es : EnchantStrict.values()){
            if(es.equals(EnchantStrict.INPUT)) continue;
            list.add(es.toStr());
        }
        return list;
    }

    public List<String> getEnchantmentStrList(){
        List<String> list = new ArrayList<>();
        for(Enchantment enchant : Enchantment.values()){
            list.add(enchant.getName().toUpperCase());
        }
        return list;
    }

//====
    public Map<Coordinate, List<Coordinate>> amorphous(Recipe recipe, Recipe input) {
        // returns candidate of correct pattern
        Map<Coordinate, List<Coordinate>> result = new HashMap<>();
        List<Coordinate> r = recipe.getEnchantedItemCoordinateList();
        List<Coordinate> i = input.getEnchantedItemCoordinateList();

        Map<Coordinate, List<Coordinate>> NULL_ANCHOR_MAP = new HashMap<Coordinate, List<Coordinate>>() {{
            put(Coordinate.NULL_ANCHOR, Collections.emptyList());
        }};

        List<List<EnchantWrap>> x = input.getEnchantedItemList();

        if (r.size() > i.size()) return new HashMap<>();
        if (r.isEmpty()) return NULL_ANCHOR_MAP;

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

        //debug
        System.out.println("[EnchantUtil]r size="+r.size()+", i size="+i.size());
        map.forEach((key, value) -> System.out.printf("index=%s, list=%s%n", key.toString(), value.toString()));
        result.forEach((s, t) -> t.forEach(e -> System.out.printf("index=%s, element=%s%n", s.toString(), e.toString())));
        return result.isEmpty() ? NULL_ANCHOR_MAP : result;
    }

    private List<Boolean> getMatchList(List<EnchantWrap> recipe, List<List<EnchantWrap>> in) {
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
}

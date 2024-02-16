package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EnchantUtil {

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

        //debug
        System.out.println("[EnchantUtil]r size="+r.size()+", i size="+i.size());
        map.forEach((key, value) -> System.out.printf("index=%s, list=%s%n", key.toString(), value.toString()));
        result.forEach((s, t) -> t.forEach(e -> System.out.printf("index=%s, element=%s%n", s.toString(), e.toString())));
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
}

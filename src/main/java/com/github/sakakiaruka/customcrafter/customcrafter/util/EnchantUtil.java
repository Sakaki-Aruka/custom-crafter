package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantUtil {


    public List<Matter> getTargetMaterialMatters(Recipe recipe, Material target){
        // only for recipe
        // If not contains the target, return an empty list.
        List<Matter> list = new ArrayList<>();
        for(Matter matter : recipe.getContentsNoAir()){
            if(!matter.getCandidate().contains(target))continue;
            list.add(matter);
        }
        return list;
    }

    public List<Matter> getTargetEnchantMatters(Recipe recipe, Enchantment target){
        // If not contains the target, return an empty list.
        List<Matter> list = new ArrayList<>();
        for(Matter matter : recipe.getContentsNoAir()){
            if(!matter.hasWrap())continue;

            for(EnchantWrap wrap : matter.getWrap()){
                if(!wrap.getEnchant().equals(target))continue;
                list.add(matter);
                break;
            }
        }
        return list;
    }

    public boolean containsFromDoubleList(List<List<EnchantWrap>> list, Matter matter){
        if(!matter.hasWrap())return false;
        if(list.isEmpty())return false;
        for(List<EnchantWrap> l : list){
            Matter dummy = new Matter(Arrays.asList(Material.AIR),0);
            dummy.addAllWrap(l);
            if(!new Search().getEnchantWrapCongruence(matter,dummy))return false;
        }
        return true;
    }

    public EnchantStrict getStrictByName(String in){
        for(EnchantStrict es : EnchantStrict.values()){
            if(in.equals(es.toStr())) return es;
        }
        return null;
    }

    public List<String> strValues(){
        List<String> list = new ArrayList<>();
        for(EnchantStrict es : EnchantStrict.values()){
            list.add(es.toStr());
        }
        return list;
    }

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

}

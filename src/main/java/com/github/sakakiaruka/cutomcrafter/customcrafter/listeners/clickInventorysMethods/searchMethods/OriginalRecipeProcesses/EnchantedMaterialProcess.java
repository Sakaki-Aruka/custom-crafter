package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.EnchantedMaterial;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.EnchantedMaterialEnum;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.IntegratedEnchant;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnchantedMaterialProcess {

    public boolean isSameItem(ItemStack model,ItemStack real){
        Map<Enchantment,Integer> realEnchants = real.getEnchantments();
        Map<IntegratedEnchant,EnchantedMaterialEnum> modelEnchants = ((EnchantedMaterial)model).getRelation();

        for(Map.Entry<IntegratedEnchant,EnchantedMaterialEnum> entry:modelEnchants.entrySet()){
            if(entry.getValue().equals(EnchantedMaterialEnum.NotStrict))continue;
            if(!realEnchants.keySet().contains(entry.getKey().getEnchant()))return false; // not contains needed enchantment

            if(entry.getValue().equals(EnchantedMaterialEnum.OnlyEnchant) &&
            realEnchants.keySet().contains(entry.getKey().getEnchant())) continue; // real contains needed enchant

            if(entry.getValue().equals(EnchantedMaterialEnum.Strict) &&
            realEnchants.keySet().contains(entry.getKey().getEnchant()) &&
            realEnchants.get(entry.getKey().getEnchant()) == entry.getKey().getLevel())continue;

            return false;
        }
        return true;
    }

    private List<Integer> getEnchantedPlaceList(List<ItemStack> in){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<in.size();i++){
            if(in.get(i) instanceof EnchantedMaterial)list.add(i);
        }
        return list;
    }
}

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
    public boolean isSameItems(RecipeMaterial model,RecipeMaterial real){
        List<Integer> inModel = getEnchantedPlaceList(model.getItemStackListNoAir());
        List<ItemStack> models = model.getItemStackListNoAir();
        List<ItemStack> reals = real.getItemStackListNoAir();

        for(int i=0;i<models.size();i++){
            if(inModel.contains(i))continue;
            if(!models.get(i).equals(reals.get(i)))return false;
        }

        for(int i=0;i< inModel.size();i++){
            //reals
            Map<Enchantment,Integer> realEnchants = reals.get(inModel.get(i)).getEnchantments();
            //models
            Map<IntegratedEnchant,EnchantedMaterialEnum> enchantsAdvanced = ((EnchantedMaterial)models.get(inModel.get(i))).getRelation();

            if(realEnchants.size() != enchantsAdvanced.size())return false;
            List<IntegratedEnchant> modelEnchants = new ArrayList<>(Arrays.asList(enchantsAdvanced.keySet().toArray(new IntegratedEnchant[0])));
            List<EnchantedMaterialEnum> enumTypes = (List<EnchantedMaterialEnum>) enchantsAdvanced.values();

            for(int j=0;j<realEnchants.size();j++){
                EnchantedMaterialEnum enumType = enumTypes.get(i);
                if(enumType.equals(EnchantedMaterialEnum.NotStrict))continue;
                Enchantment require = modelEnchants.get(i).getEnchant();
                if(!realEnchants.keySet().contains(require))return false;
                if(enumType.equals(EnchantedMaterialEnum.OnlyEnchant))continue;
                int modelLevel = modelEnchants.get(i).getLevel();
                int realLevel = realEnchants.get(require);
                if(modelLevel != realLevel)return false;
            }
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

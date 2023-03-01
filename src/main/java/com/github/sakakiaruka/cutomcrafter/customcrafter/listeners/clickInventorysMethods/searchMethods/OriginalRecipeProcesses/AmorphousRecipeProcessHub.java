package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.CommonProcess;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AmorphousRecipeProcessHub {
    public boolean main(OriginalRecipe original,RecipeMaterial realMaterial){
        CommonProcess shared = new CommonProcess();
        AmorphousRecipe model = (AmorphousRecipe) original.getRecipeMaterial();
        AmorphousRecipe real = shared.toAmorphous(realMaterial);
        if(model.getTypeEnum().equals(AmorphousEnum.NEIGHBOR)
                && real.getTypeEnum().equals(AmorphousEnum.ANYWHERE)) return false;

        boolean mixedMaterial = shared.containsMixedMaterial(model);
        boolean enchantedMaterial = shared.containsEnchantedMaterial(model);

        for(ItemStack item:getItemStackList(model)){
            // real does not contain items
            if(!real.getMaterials().keySet().contains(item))return false;
            // if real does not contain amount more than needed
            if(real.getMaterials().get(item) < model.getMaterials().get(item))return false;

            int amount = real.getMaterials().get(item) - model.getMaterials().get(item);
            real.getMaterials().put(item,amount);
        }

        for(ItemStack item:getOtherStackList(model)){
            if(item.getClass().equals(EnchantedMaterial.class)){
                // enchantedMaterial
            }else if(item.getClass().equals(RegexRecipeMaterial.class)){
                // regexRecipeMaterial

            }
        }



        return false;
    }

    private List<ItemStack> getItemStackList(AmorphousRecipe model){
        List<ItemStack> list = new ArrayList<>();
        for(Map.Entry<ItemStack,Integer> entry:model.getMaterials().entrySet()){
            if(entry.getClass().equals(ItemStack.class))list.add(entry.getKey());
        }
        return list;
    }

    private List<ItemStack> getOtherStackList(AmorphousRecipe model){
        List<ItemStack> list = new ArrayList<>();
        for(Map.Entry<ItemStack,Integer> entry:model.getMaterials().entrySet()){
            if(!entry.getClass().equals(ItemStack.class))list.add(entry.getKey());
        }
        return list;
    }


    private AmorphousRecipe getConfirmedAmorphousRecipe(AmorphousRecipe model,List<ItemStack> pure){
        for(ItemStack item:pure){
            model.getMaterials().remove(item);
        }
        return model;
    }
}

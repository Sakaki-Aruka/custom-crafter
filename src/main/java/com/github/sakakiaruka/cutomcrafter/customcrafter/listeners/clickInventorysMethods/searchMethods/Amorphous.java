package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Amorphous {
    public boolean main(AmorphousRecipe amorphousRecipe, RecipeMaterial recipeMaterial){
        if(!enumCongruence(amorphousRecipe.getTypeEnum(),recipeMaterial))return false;
        if(containsMixedMaterial(amorphousRecipe)){
            if(!mixedMaterialCongruence(amorphousRecipe,recipeMaterial))return false;
        }else{
            if(!itemStackCongruence(amorphousRecipe,recipeMaterial))return false;
        }
        return true;
    }

    private boolean containsMixedMaterial(AmorphousRecipe amorphous){
        for(Map.Entry<ItemStack,Integer> entry:amorphous.getMaterials().entrySet()){
            if(entry.getKey() instanceof MixedMaterial) return true;
        }
        return false;
    }

    private boolean mixedMaterialCongruence(AmorphousRecipe model,RecipeMaterial recipeMaterial){
        Map<MixedMaterial,Integer> recipeMap = new HashMap<>();
        Map<MixedMaterial,Integer> realMap = new HashMap<>();
        for(Map.Entry<ItemStack,Integer> entry:model.getMaterials().entrySet()){
            if(!(entry.getKey() instanceof MixedMaterial))continue;
            MixedMaterial material = (MixedMaterial) entry.getKey();
            int amount = entry.getValue();
            recipeMap.put(material,amount);

        }

        for(Map.Entry<MultiKeys,ItemStack> entry:recipeMaterial.getRecipeMaterial().entrySet()){
            if(!(entry.getValue() instanceof MixedMaterial))continue;
            MixedMaterial material = (MixedMaterial) entry.getValue();
            int amount = entry.getValue().getAmount();
            realMap.put(material,amount);
        }

        return recipeMap.equals(realMap);

    }


    private boolean enumCongruence(AmorphousEnum type,RecipeMaterial recipeMaterial){
        List<Integer> xs = new ArrayList<>();
        List<Integer> ys = new ArrayList<>();

        for(Map.Entry<MultiKeys, ItemStack> entry:recipeMaterial.getRecipeMaterial().entrySet()){
            MultiKeys key = entry.getKey();
            xs.add(key.getKey1());
            ys.add(key.getKey2());
        }
        Collections.sort(xs);
        Collections.sort(ys);
        int width = Math.abs(xs.get(0) - xs.get(xs.size()-1));
        int height = Math.abs(ys.get(0) - ys.get(ys.size()-1));
        int squareSize = Math.max(width,height) + 1;

        if(type.equals(AmorphousEnum.NEIGHBOR) && squareSize > 3)return false;
        return true;
    }

    private boolean itemStackCongruence(AmorphousRecipe amorphousRecipe,RecipeMaterial recipeMaterial){
        Map<ItemStack,Integer> model = amorphousRecipe.getMaterials();
        Map<ItemStack,Integer> real = new HashMap<>();

        for(Map.Entry<MultiKeys,ItemStack> entry:recipeMaterial.getRecipeMaterial().entrySet()){
            int amount = entry.getValue().getAmount();
            if(real.containsKey(entry.getValue()))amount +=real.get(entry.getValue());
            real.put(entry.getValue(),amount);
        }
        return model.equals(real);
    }


}

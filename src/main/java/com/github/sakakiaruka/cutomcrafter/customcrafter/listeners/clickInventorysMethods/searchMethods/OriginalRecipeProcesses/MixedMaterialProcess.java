package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.mixedCategories;

public class MixedMaterialProcess {
    private boolean isSameItems(RecipeMaterial model, RecipeMaterial real){
        List<Integer> inModel = getMixedPlaceList(model.getItemStackListNoAir());
        List<ItemStack> models = model.getItemStackListNoAir();
        List<ItemStack> reals = real.getItemStackListNoAir();

        for(int i=0;i<models.size();i++){
            if(inModel.contains(i))continue;
            if(!models.get(i).equals(reals.get(i)))return false;
        }

        for(int i=0;i< inModel.size();i++){
            Material realMaterial = reals.get(inModel.get(i)).getType();
            String category = ((MixedMaterial)models.get(i)).getMaterialCategory();
            List<Material> categorized = mixedCategories.get(category);
            if(!categorized.contains(realMaterial))return false;
        }
        return true;
    }

    private List<Integer> getMixedPlaceList(List<ItemStack> in){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<in.size();i++){
            if(in.get(i) instanceof MixedMaterial)list.add(i);
        }
        return list;
    }
}

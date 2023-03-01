package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.mixedCategories;

public class MixedMaterialProcess {

    public boolean isSameItem(ItemStack model,ItemStack real){
        String category = ((MixedMaterial) model).getMaterialCategory();
        Material material = real.getType();
        return mixedCategories.get(category).contains(material);
    }

    private List<Integer> getMixedPlaceList(List<ItemStack> in){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<in.size();i++){
            if(in.get(i) instanceof MixedMaterial)list.add(i);
        }
        return list;
    }
}

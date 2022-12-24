package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipes;

public class Search {
    public ItemStack search(Inventory inventory, Player player){
        int size = guiOpening.get(player);
        int total = 0;
        RecipeMaterial recipeMaterial = new RecipeMaterial();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(inventory.getItem(i*9+j)!=null && !inventory.getItem(i*9+j).getType().equals(Material.AIR)){
                    continue;
                }
                int x = i;
                int y = j;
                MultiKeys key = new MultiKeys(x,y);
                ItemStack item = inventory.getItem(i*9+j);
                recipeMaterial.put(key,item);
                total++;
            }
        }

        List<OriginalRecipe> totalMatched = getTotalMatchItem(total);
        List<ItemStack> results = getDiffChecked(recipeMaterial,totalMatched,size);

        //debug
        System.out.println("[CustomCrafter Debug Message];search error occurred. matched "+ results.size()+" items are matched that your input.OMG");
        System.out.println("[CustomCrafter Debug Message];matched items are here. -> "+results);
        System.out.println("we will only return item to you that a first item of the return items list.");

        return results.get(0);

    }

    private List<OriginalRecipe> getTotalMatchItem(int total){
        List<OriginalRecipe> list = new ArrayList<>();
        for(OriginalRecipe original:recipes){
            RecipeMaterial material = original.getRecipeMaterial();
            if(material.getMapSize() == total){
                list.add(original);
            }
        }
        return list;
    }

    private List<ItemStack> getDiffChecked(RecipeMaterial send,List<OriginalRecipe> candidates,int sendSize){
        List<ItemStack> result = new ArrayList<>();
        for(OriginalRecipe original:candidates){
            int modelSize = original.getSize();
            RecipeMaterial candidate = original.getRecipeMaterial();
            if(new CheckDiff().diff(candidate,send,modelSize,sendSize))result.add(original.getResult());
        }
        return result;
    }

}

package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.RecipeTypeGuidePost;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipesAmount;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipesMaterial;


public class Search {
    public List<ItemStack> search(Inventory inventory,int size){
        RecipeMaterial real = toRecipeMaterial(inventory,size);
        if(real.isEmpty())return null;
        List<ItemStack> list = new ArrayList<>();

        Set<OriginalRecipe> originals = new HashSet<>();
        Material top_material = real.getLargestMaterial();
        int top_amount = real.getLargestAmount();

        if(recipesMaterial.get(top_material) == null
        && recipesAmount.get(top_amount) == null)return null;

        if(recipesMaterial.get(top_material) != null)recipesMaterial.get(top_material).forEach(s->originals.add(s)); // material
        if(recipesAmount.get(top_amount) != null)recipesAmount.get(top_amount).forEach(s->originals.add(s)); // amount
        if(originals.isEmpty())return null;

        for(OriginalRecipe original:originals){
            if(!new RecipeTypeGuidePost().main(original,real))continue;
            list.add(original.getResult());
        }

        if(list.isEmpty())return null;
        return list;
    }

    private RecipeMaterial toRecipeMaterial(Inventory inventory,int size){
        RecipeMaterial recipeMaterial = new RecipeMaterial();
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                MultiKeys key = new MultiKeys(x,y);
                ItemStack item;
                int slot = x+y*9;
                if(inventory.getItem(slot)==null){
                    item = new ItemStack(Material.AIR);
                }else if(inventory.getItem(slot).getType().equals(Material.AIR)){
                    item = new ItemStack(Material.AIR);
                }else{
                    item = inventory.getItem(slot);
                }

                recipeMaterial.put(key,item);
            }
        }
        return recipeMaterial;
    }
}

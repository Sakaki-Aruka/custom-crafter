package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.Amorphous;
import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.MixedMaterialMethods;
import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.Normal;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.*;

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
            RecipeMaterial model = original.getRecipeMaterial();

            if(containsMixedMaterial(model)){
                // MixedMaterial
                if(!new MixedMaterialMethods().main(model,real))continue;

            }else{
                // Normal Recipe
                if(!new Normal().main(model,real))continue;

            }
            list.add(original.getResult());
        }

        if(list.isEmpty())return null;
        return list;
    }

    private boolean containsMixedMaterial(RecipeMaterial model){
        for(Map.Entry<MultiKeys,ItemStack> entry : model.getRecipeMaterial().entrySet()){
            if(entry.getValue() instanceof MixedMaterial) return true;
        }
        return false;
    }

    private boolean containsAmorphousRecipe(RecipeMaterial in){
        return in instanceof AmorphousRecipe;
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

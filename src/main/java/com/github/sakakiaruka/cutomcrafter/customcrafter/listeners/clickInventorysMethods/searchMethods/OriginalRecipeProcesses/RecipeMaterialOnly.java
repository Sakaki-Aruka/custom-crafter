package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.CommonProcess;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RecipeMaterialOnly {
    public boolean main(OriginalRecipe modelOriginal, Inventory inventory,int size){
        CommonProcess shared = new CommonProcess();
        RecipeMaterial model = modelOriginal.getRecipeMaterial();
        RecipeMaterial real = shared.getRecipeMaterial(inventory,size);

        if(shared.getTotal(model) != shared.getTotal(real))return false;
        if(shared.getSquareSize(model) != shared.getSquareSize(real))return false;
        if(!shared.isSameShape(model.getMultiKeysListNoAir(),real.getMultiKeysListNoAir()))return false;
        if(!isSameItems(model,real))return false;
        return true;
    }

    private boolean isSameItems(RecipeMaterial model,RecipeMaterial real){
        List<ItemStack> modelItems = model.getItemStackListNoAir();
        List<ItemStack> realItems = real.getItemStackListNoAir();
        int size = modelItems.size();
        for(int i=0;i<size;i++){
            if(!modelItems.get(i).equals(realItems.get(i)))return false;
        }
        return true;
    }
}

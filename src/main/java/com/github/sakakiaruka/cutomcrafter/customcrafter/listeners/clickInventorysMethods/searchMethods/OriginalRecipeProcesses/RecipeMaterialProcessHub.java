package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.CommonProcess;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeMaterialProcessHub {
    public boolean main(OriginalRecipe modelOriginal,RecipeMaterial real){
        CommonProcess shared = new CommonProcess();
        RecipeMaterial model = modelOriginal.getRecipeMaterial();

        if(shared.getTotal(model) != shared.getTotal(real))return false;
        if(shared.getSquareSize(model) != shared.getSquareSize(real))return false;
        if(!shared.isSameShape(model.getMultiKeysListNoAir(),real.getMultiKeysListNoAir()))return false;

        boolean mixedMaterial = shared.containsMixedMaterial(model);
        boolean enchantedMaterial = shared.containsEnchantedMaterial(model);

        if(!mixedMaterial && !enchantedMaterial){
            //recipeMaterial only
            return isSameItems(model,real);
        }
        if(mixedMaterial && !enchantedMaterial){
            //mixed only
            return new MixedMaterialProcess().isSameItems(model,real);
        }
        if(!mixedMaterial && enchantedMaterial){
            //enchanted only
            return new EnchantedMaterialProcess().isSameItems(model,real);
        }
        if(mixedMaterial && enchantedMaterial){
            //all
            if(!new MixedMaterialProcess().isSameItems(model,real))return false;
            if(!new EnchantedMaterialProcess().isSameItems(model,real))return false;
            return true;
        }
        return false;
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

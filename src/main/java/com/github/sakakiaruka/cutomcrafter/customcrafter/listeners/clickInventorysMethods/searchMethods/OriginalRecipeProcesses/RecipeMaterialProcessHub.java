package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.CommonProcess;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeMaterialProcessHub {
    public boolean main(OriginalRecipe modelOriginal, Inventory inventory,int size){
        CommonProcess shared = new CommonProcess();
        RecipeMaterial model = modelOriginal.getRecipeMaterial();
        RecipeMaterial real = shared.getRecipeMaterial(inventory,size);

        if(shared.getTotal(model) != shared.getTotal(real))return false;
        if(shared.getSquareSize(model) != shared.getSquareSize(real))return false;
        if(!shared.isSameShape(model.getMultiKeysListNoAir(),real.getMultiKeysListNoAir()))return false;

        boolean mixedMaterial = containsMixedMaterial(model);
        boolean enchantedMaterial = containsEnchantedMaterial(model);

        if(mixedMaterial && !enchantedMaterial){
            //mixed only
        }
        if(!mixedMaterial && enchantedMaterial){
            //enchanted only
        }
        if(!mixedMaterial && !enchantedMaterial){
            //recipeMaterial only
            return isSameItems(model,real);
        }
        if(mixedMaterial && enchantedMaterial){
            //all
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

    private boolean containsMixedMaterial(RecipeMaterial in){
        return getClassSet(in).contains("MixedMaterial");
    }

    private boolean containsEnchantedMaterial(RecipeMaterial in){
        return getClassSet(in).contains("EnchantedMaterial");
    }

    private Set<String> getClassSet(RecipeMaterial in){
        List<ItemStack> items = in.getItemStackListNoAir();
        Set<String> classes = new HashSet<>();
        items.forEach(s->classes.add(s.getClass().toGenericString()));
        return classes;
    }
}

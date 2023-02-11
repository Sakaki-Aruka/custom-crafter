package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.CommonProcess;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AmorphousRecipeProcessHub {
    public boolean main(OriginalRecipe original,RecipeMaterial realMaterial){
        CommonProcess shared = new CommonProcess();
        AmorphousRecipe model = (AmorphousRecipe) original.getRecipeMaterial();
        AmorphousRecipe real = shared.toAmorphous(realMaterial);
        if(model.getTypeEnum().equals(AmorphousEnum.NEIGHBOR)
                && real.getTypeEnum().equals(AmorphousEnum.ANYWHERE)) return false;

        boolean mixedMaterial = shared.containsMixedMaterial(model);
        boolean enchantedMaterial = shared.containsEnchantedMaterial(model);

        if(!mixedMaterial && !enchantedMaterial){
            //amorphousRecipe only
            return model.getMaterials().equals(real.getMaterials());

        }
        if(mixedMaterial && !enchantedMaterial){
            //mixedMaterial only
            return new MixedMaterialProcess().isSameItems(model,real);

        }
        if(!mixedMaterial && enchantedMaterial){
            //enchantedMaterial only
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
}

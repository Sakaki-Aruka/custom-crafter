package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.CommonProcess;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import com.github.sakakiaruka.cutomcrafter.customcrafter.some.RecipeMaterialUtil;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMaterialProcessHub {
    public boolean main(OriginalRecipe modelOriginal,RecipeMaterial real){
        CommonProcess shared = new CommonProcess();
        RecipeMaterial model = modelOriginal.getRecipeMaterial();

        if(model.getTotalItems() != real.getTotalItems())return false;
        if(shared.getSquareSize(model) != shared.getSquareSize(real))return false;
        if(!shared.isSameShape(model.getMultiKeysListNoAir(),real.getMultiKeysListNoAir()))return false;

        for(int i=0;i<real.getMapSize();i++){
            ItemStack ItemStackModel = model.getItemStackListNoAir().get(i);
            ItemStack ItemStackReal = real.getItemStackListNoAir().get(i);
            if(ItemStackModel instanceof MixedMaterial){
                if(!new MixedMaterialProcess().isSameItem(ItemStackModel,ItemStackReal))return false;
            }else if(ItemStackModel instanceof EnchantedMaterial){
                if(!new EnchantedMaterialProcess().isSameItem(ItemStackModel,ItemStackReal))return false;
            }else{
                if(!this.isSameItem(ItemStackModel,ItemStackReal))return false;
            }
        }
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

    private boolean isSameItem(ItemStack model,ItemStack real){
        return model.equals(real);
    }
}

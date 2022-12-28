package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiOriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipes;

public class Search {
    public MultiOriginalRecipe search(MultiOriginalRecipe multiOriginal, Player player){
        try{
            multiOriginal.getClass();
        }catch (NullPointerException e){
            return null;
        }
        OriginalRecipe originalRecipe = multiOriginal.getOriginalRecipe();
        RecipeMaterial model = originalRecipe.getRecipeMaterial();
        int ratio = multiOriginal.getAmount();
        // ---conditions---
        int size = originalRecipe.getSize();
        int total = originalRecipe.getTotal();
        List<Material> rawMaterials = originalRecipe.getRawMaterials();
        // --conditions---
        List<OriginalRecipe> matched = new ArrayList<>();

        for(OriginalRecipe o:recipes){
            if(o.getSize()!=size)continue;
            if(o.getTotal()!=total)continue;
            if(!o.getRawMaterials().equals(rawMaterials))continue;
            matched.add(o);
        }

        List<OriginalRecipe> result = new ArrayList<>();
        for(OriginalRecipe o:matched){
            if(!new CheckDiff().diff(model,o.getRecipeMaterial(),size,o.getSize()))continue;
            result.add(o);
        }

        if(result.isEmpty()) {
            player.sendMessage("The recipe has multi-result-items.\n" +
                    "Failed to create items, because the custom-crafter-system could not judge which you want to.");
            return null;
        }

        MultiOriginalRecipe mop = new MultiOriginalRecipe(ratio,result.get(0),multiOriginal.getRemaining());
        return mop;
    }

}

package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses.AmorphousRecipeProcessHub;
import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses.RecipeMaterialProcessHub;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.AmorphousRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.Peculiar;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;

public class RecipeTypeGuidePost {
    public boolean main(OriginalRecipe model, RecipeMaterial real){
        if(model instanceof Peculiar){
            //Peculiar
            if(model.getRecipeMaterial() instanceof AmorphousRecipe){
                if(model.getRecipeMaterial() instanceof AmorphousRecipe){
                    // Peculiar & Amorphous
                }else{
                    // Peculiar & RecipeMaterial
                }
            }
        }else{
            //Original
            if(model.getRecipeMaterial() instanceof AmorphousRecipe){
                // Original & Amorphous
                return new RecipeMaterialProcessHub().main(model,real);
            }else{
                // Original & RecipeMaterial
                return new AmorphousRecipeProcessHub().main(model,real);
            }
        }
        return false;
    }
}

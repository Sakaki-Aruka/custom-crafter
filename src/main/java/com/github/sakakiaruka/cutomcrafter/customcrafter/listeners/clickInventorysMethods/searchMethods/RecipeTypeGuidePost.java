package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses.AmorphousRecipeProcessHub;
import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses.RecipeMaterialProcessHub;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;

public class RecipeTypeGuidePost {
    public boolean main(OriginalRecipe model, RecipeMaterial real){
        if (model.getRecipeMaterial() instanceof AmorphousRecipe){
            // Original & Amorphous
            return new AmorphousRecipeProcessHub().main(model,real);
        }else{
            // Original & RecipeMaterial
            return new RecipeMaterialProcessHub().main(model,real);
        }
    }
}

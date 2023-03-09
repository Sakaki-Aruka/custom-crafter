package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.Recipes;


public class MassRecipe extends RecipeMaterial implements Recipes {
    // cannot add a RecipeMaterial that contains RegexRecipeMaterial
    private RecipeMaterial modified;
    public MassRecipe(RecipeMaterial source,RecipeMaterial modified){
        super(source);
        this.modified = modified;
    }

    public RecipeMaterial getModified() {
        return modified;
    }

    public void setModified(RecipeMaterial modified) {
        this.modified = modified;
    }
}

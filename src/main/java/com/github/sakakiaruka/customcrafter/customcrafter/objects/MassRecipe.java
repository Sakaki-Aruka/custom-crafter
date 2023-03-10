package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.Recipes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class MassRecipe extends RecipeMaterial implements Recipes {
    // cannot add a RecipeMaterial that contains RegexRecipeMaterial

    // do not send AmorphousRecipeMaterial to this constructor
    private RecipeMaterial rm;
    private List<Material> unrelatedAmount;
    private ItemStack result;

    public MassRecipe(RecipeMaterial recipeMaterial,List<Material> unrelatedAmount,ItemStack result){
        this.rm = recipeMaterial;
        this.unrelatedAmount = unrelatedAmount;
        this.result = result;
    }

    public List<Material> getUnrelatedAmount() {
        return unrelatedAmount;
    }

    public void setUnrelatedAmount(List<Material> unrelatedAmount) {
        this.unrelatedAmount = unrelatedAmount;
    }

    public RecipeMaterial getRm() {
        return rm;
    }

    public void setRm(RecipeMaterial rm) {
        this.rm = rm;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }
}

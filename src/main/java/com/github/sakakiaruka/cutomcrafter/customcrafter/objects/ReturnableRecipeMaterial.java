package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ReturnableRecipeMaterial extends RecipeMaterial{

    private List<ItemStack> returnItems;

    public ReturnableRecipeMaterial(List<ItemStack> list){
        this.returnItems = list;
    }

    public ReturnableRecipeMaterial(){}

    public List<ItemStack> getReturnItems() {
        return returnItems;
    }

    public void setReturnItems(List<ItemStack> returnItems) {
        this.returnItems = returnItems;
    }
}

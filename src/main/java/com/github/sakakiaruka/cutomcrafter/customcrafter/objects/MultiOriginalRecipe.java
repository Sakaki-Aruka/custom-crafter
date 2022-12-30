package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class MultiOriginalRecipe {
    private int amount;
    private OriginalRecipe originalRecipe;
    private Map<Integer, ItemStack> remaining;

    public MultiOriginalRecipe(int amount,OriginalRecipe originalRecipe,Map<Integer,ItemStack> remaining){
        this.amount = amount;
        this.originalRecipe = originalRecipe;
        this.remaining = remaining;
    }

    public String toString(){
        return String.format("amount:%d | OriginalRecipe:%s | remaining:%s",amount,originalRecipe.toString(),remaining);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public OriginalRecipe getOriginalRecipe() {
        return originalRecipe;
    }

    public void setOriginalRecipe(OriginalRecipe originalRecipe) {
        this.originalRecipe = originalRecipe;
    }

    public Map<Integer, ItemStack> getRemaining() {
        return remaining;
    }

    public void setRemaining(Map<Integer, ItemStack> remaining) {
        this.remaining = remaining;
    }
}

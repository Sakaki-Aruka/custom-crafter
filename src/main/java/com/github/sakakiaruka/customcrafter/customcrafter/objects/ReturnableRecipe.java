package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ReturnableRecipe extends OriginalRecipe{
    private Map<Material,Material> relatedReturnItems;


    public ReturnableRecipe(ItemStack result,int size,int total,RecipeMaterial rm,String recipeName,Map<Material,Material> relatedReturnItems){
        super(result,size,total,rm,recipeName);
        this.relatedReturnItems = relatedReturnItems;
    }

    public Map<Material, Material> getRelatedReturnItems() {
        return relatedReturnItems;
    }

    public void setRelatedReturnItems(Map<Material, Material> relatedReturnItems) {
        this.relatedReturnItems = relatedReturnItems;
    }

    public void putAllRelatedReturnItems(Map<Material,Material> in){
        this.relatedReturnItems.putAll(in);
    }

    public void putOneRelatedReturnItems(Material target,Material returnItem){
        this.relatedReturnItems.put(target,returnItem);
    }
}
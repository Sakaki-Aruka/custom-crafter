package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class MixedMaterial extends ItemStack {
    private String materialCategory;

    public MixedMaterial(String materialCategory,Material material,int amount){
        this.materialCategory = materialCategory;
        super.setAmount(amount);
        super.setType(material);
    }

    public MixedMaterial(){
        this.materialCategory = null;
    }

    public String getMaterialCategory() {
        return materialCategory;
    }

    public String info(){
        String material = super.getType().name();
        String amount = String.valueOf(super.getAmount());
        String category = materialCategory;
        String result = String.format("material : %s / amount : %s / category : %s",material,amount,category);
        return result;
    }

}

package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.Materials;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.some.SettingsLoad.mixedCategories;


public class MixedMaterial extends ItemStack implements Materials {
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

    public List<Material> getCandidate(){
        return mixedCategories.get(this.materialCategory);
    }

    public String info(){
        String material = super.getType().name();
        String amount = String.valueOf(super.getAmount());
        String category = materialCategory;
        String result = String.format("material : %s / amount : %s / category : %s",material,amount,category);
        return result;
    }

}

package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AmorphousRecipe extends RecipeMaterial{

    private AmorphousEnum typeEnum;
    private Map<ItemStack,Integer> materials;

    public AmorphousRecipe(AmorphousEnum typeEnum,Map<ItemStack,Integer> materials){
        this.typeEnum = typeEnum;
        this.materials = materials;
    }

    public AmorphousEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(AmorphousEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Map<ItemStack, Integer> getMaterials() {
        return materials;
    }

    public void setMaterials(Map<ItemStack, Integer> materials) {
        this.materials = materials;
    }
}

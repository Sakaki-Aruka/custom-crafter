package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AmorphousRecipe extends RecipeMaterial{

    private AmorphousEnum typeEnum;
    private Map<ItemStack,Integer> materials;

    public AmorphousRecipe(AmorphousEnum typeEnum,Map<ItemStack,Integer> materials){
        super();
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

    public int getTotalItems(){
        int result = 0;
        for(Map.Entry<ItemStack,Integer> entry:materials.entrySet()){
            if(entry.getKey().getType().equals(Material.AIR))continue;
            result += entry.getValue();
        }
        return result;
    }

    public List<ItemStack> getItemStackListNoAir(){
        List<ItemStack> list = new ArrayList<>();
        materials.entrySet().forEach(s->list.add(s.getKey()));
        ItemStack air = new ItemStack(Material.AIR);
        if(list.contains(air))list.remove(air);
        return list;
    }
}

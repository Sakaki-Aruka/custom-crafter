package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EnchantedMaterial extends ItemStack {
    private Map<IntegratedEnchant,EnchantedMaterialEnum> relation;
    public EnchantedMaterial(ItemStack item){
        super(item);
        this.relation = new HashMap<>();
    }

    public void put(IntegratedEnchant integrated,EnchantedMaterialEnum enumType){
        relation.put(integrated,enumType);
    }

    public Map<IntegratedEnchant,EnchantedMaterialEnum> getRelation(){
        return this.relation;
    }

    public void setBrandNew(){
        relation.clear();
    }

    public int getAmount(){
        return super.getAmount();
    }
}

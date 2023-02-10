package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EnchantedMaterial extends ItemStack {
    private Map<IntegratedEnchant,EnchantedMaterialEnum> relation;
    public EnchantedMaterial(){
        this.relation = new HashMap<>();
    }

    public void put(IntegratedEnchant integrated,EnchantedMaterialEnum enumType){
        relation.put(integrated,enumType);
    }

    public Map<IntegratedEnchant,EnchantedMaterialEnum> getRelation(){
        return this.relation;
    }

    public void setBrandNew(){
        relation.entrySet().forEach(s->relation.remove(s.getKey()));
    }
}

package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import org.bukkit.enchantments.Enchantment;

public class IntegratedEnchant {
    private Enchantment enchant;
    private int level;
    public IntegratedEnchant(Enchantment enchant,int level){
        this.enchant = enchant;
        this.level = level;
    }

    public Enchantment getEnchant() {
        return enchant;
    }

    public void setEnchant(Enchantment enchant) {
        this.enchant = enchant;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}

package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter;

import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EnchantWrap {
    private int level;
    private Enchantment enchant;
    private EnchantStrict strict;
    public EnchantWrap(int level,Enchantment enchant,EnchantStrict strict){
        this.level = level;
        this.enchant = enchant;
        this.strict = strict;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Enchantment getEnchant() {
        return enchant;
    }

    public void setEnchant(Enchantment enchant) {
        this.enchant = enchant;
    }

    public EnchantStrict getStrict() {
        return strict;
    }

    public void setStrict(EnchantStrict strict) {
        this.strict = strict;
    }

    public String info(){
        String e = enchant == null ? "null" : enchant.toString();
        int lv = enchant == null ? -1 : level;
        String s = enchant == null ? "null (Strict)" : strict.toString();
        return String.format("Enchant : %s | Level : %d | Strict : %s",e,lv,s);
    }

}

package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackComparison {
    public ItemStack comparison(ItemStack already,ItemStack add){
        if(already==null)return null;
        if(already.getType().equals(Material.AIR))return add;
        if(!already.getType().equals(add.getType()))return null;
        if(!already.getItemMeta().equals(add.getItemMeta()))return null;
        if(already.getAmount()+add.getAmount() > already.getMaxStackSize())return null;
        already.setAmount(already.getAmount()+ add.getAmount());
        return already;
    }

    public boolean notEmpty(ItemStack in){
        if(in==null)return false;
        if(in.getType().equals(Material.AIR))return false;
        return true;
    }
}

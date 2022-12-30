package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.ClickInventory;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemsSubtract {
    public void main(Inventory inventory,int size,int delta){
        //delta is subtract amount.
        for(int i:new ClickInventory().getTableSlots(size)){
            if(inventory.getItem(i)!=null && !inventory.getItem(i).equals(Material.AIR)){
                ItemStack item = inventory.getItem(i);
                int old = item.getAmount();

                item.setAmount(old-delta);
            }
        }
    }
}

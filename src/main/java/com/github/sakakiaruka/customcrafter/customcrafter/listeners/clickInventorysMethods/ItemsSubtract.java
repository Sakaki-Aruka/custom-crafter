package com.github.sakakiaruka.customcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.customcrafter.customcrafter.listeners.ClickInventory;
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

                if(old - delta >= 0){
                    item.setAmount(old-delta);
                }else{
                    item.setAmount(0);
                }

            }
        }
    }
}

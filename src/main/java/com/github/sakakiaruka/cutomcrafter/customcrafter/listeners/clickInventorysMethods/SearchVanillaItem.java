package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SearchVanillaItem {
    public ItemStack getVanillaItem(Inventory inventory, int size, Player player){
        List<Integer> craftingSlots = getTableSlots(size);
        ItemStack[] items = new ItemStack[9];
        int count = 0;
        for(int i:craftingSlots){
            ItemStack item = inventory.getItem(i);
            items[count] = item;
            count++;
        }
        World world = player.getWorld();
        ItemStack result = Bukkit.craftItem(items,world,player);
        return result;
    }

    private List<Integer> getTableSlots(int size){
        List<Integer> slots = new ArrayList<>();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int result = i*9+j;
                slots.add(result);
            }
        }
        return slots;
    }
}

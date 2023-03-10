package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SearchVanilla {
    public ItemStack isThreeSquared(Inventory inventory, int size, Player player) {
        List<Integer> integerPart = new ArrayList<>();
        List<Integer> decimalPart = new ArrayList<>();
        List<Integer> itemCoordinates = new ArrayList<>();
        for (int i : getTableSlots(size)) {
            if (inventory.getItem(i) == null
                    || inventory.getItem(i).getType().equals(Material.AIR)) continue;
            itemCoordinates.add(i);
        }

        for (int i : itemCoordinates) {
            integerPart.add(i/9);
            decimalPart.add(i%9);
        }
        if(integerPart.isEmpty())return null;
        Collections.sort(integerPart);
        Collections.sort(decimalPart);
        int up = integerPart.get(0);
        int down = integerPart.get(integerPart.size()-1);
        int left = decimalPart.get(0);
        int right = decimalPart.get(decimalPart.size()-1);

        int yDistance = Math.abs(up - down);
        int xDistance = Math.abs(left - right);

        if (xDistance >= 3 || yDistance >= 3) return null; //bigger distance

        ItemStack[] items = remapping(inventory,up,left);
        ItemStack result = Bukkit.craftItem(items,player.getWorld(),player);
        return result;
    }

    private List<Integer> getTableSlots(int size) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result.add(i * 9 + j);
            }
        }
        return result;
    }

    private ItemStack[] remapping(Inventory inventory,int up,int left){
        List<Integer> table = new ArrayList<>();

        List<ItemStack> preResult = new ArrayList<>();
        ItemStack[] result = new ItemStack[9];

        for(int y=0;y<6;y++){
            for(int x=0;x<6;x++){
                table.add(y*9+x);
            }
        }

        for(int y=up;y<(up+3);y++){
            for(int x=left;x<(left+3);x++){
                int coordinate = x+y*9;
                if(!table.contains(coordinate)){
                    preResult.add(new ItemStack(Material.AIR));
                    continue;
                }
                if(inventory.getItem(coordinate)==null
                || inventory.getItem(coordinate).getType().equals(Material.AIR)){
                    preResult.add(new ItemStack(Material.AIR));
                    continue;
                }
                preResult.add(inventory.getItem(coordinate));
            }
        }

        for(int i=0;i<9;i++){
            result[i] = preResult.get(i);
        }
        return result;
    }
}


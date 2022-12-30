package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            BigDecimal target = BigDecimal.valueOf(i);
            BigDecimal ratio = BigDecimal.valueOf(9);
            BigDecimal result = target.divide(ratio, 1, RoundingMode.FLOOR);
            int integer = result.intValue();
            int decimal = BigDecimal.valueOf(result.doubleValue()*10 - integer*10).intValue();
            integerPart.add(integer);
            decimalPart.add(decimal);
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

        ItemStack[] items = new ItemStack[9];
        List<ItemStack> itemsArr = new ArrayList<>();

        for (int y = up; y < up+3 && y <size; y++) {
            for (int x = left; x < left+3 && x < size; x++) {

                int rawSlot = x+y*9;
                if (inventory.getItem(rawSlot) != null) {
                    itemsArr.add(inventory.getItem(rawSlot));
                    continue;
                }
                itemsArr.add(null);
            }
        }

        for (int i = 0; i < 9 && i<itemsArr.size(); i++) {
            items[i] = itemsArr.get(i);
        }
        int leftRightDelta = Math.abs(left-right);
        if(leftRightDelta < 2)items = remapping(items,leftRightDelta); //remapping
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


    private ItemStack[] remapping(ItemStack[] list,int LRDistance){
        ItemStack[] result = new ItemStack[9];
        List<ItemStack> items = new ArrayList<>(Arrays.asList(list));
        List<Integer> modifySlots = null;
        if(LRDistance==1)modifySlots = new ArrayList<>(Arrays.asList(2,5,8));
        if(LRDistance==0)modifySlots = new ArrayList<>(Arrays.asList(1,2,4,5,7,8));
        for(int i:modifySlots){
            if(items.get(i)!=null)items.add(i,null);
        }
        for(int i=0;i<9;i++){
            result[i] = items.get(i);
        }
        return result;
    }
}
package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchVanillaItem {

    public ItemStack getVanillaItem(Inventory inventory, int size, Player player){

        ItemStack[] stacks;
        if((stacks = getNine(inventory,getTableSlots(size),size))==null)return new ItemStack(Material.AIR);
        ItemStack item = Bukkit.craftItem(stacks,player.getWorld(),player);
        return item;
    }


    private ItemStack[] getNine(Inventory inventory,List<Integer> ints,int size){

        Map<Integer,Integer> relation = getRelation(size,ints);
        if(!isThreeSquared(size,inventory))return null;
        // the inventory has not include a vanilla item mapping

        int firstItemSlot = firstHorizon*9+firstVertical;
        List<Integer> temporary = new ArrayList<>();
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                int slot = firstItemSlot + (i*9+j);
                temporary.add(slot);
            }
        }

        List<Integer> real = new ArrayList<>();
        temporary.forEach(t->real.add(relation.get(t)));
        //transition from temporary numbers to real numbers
        ItemStack[] stacks = new ItemStack[9];

        int count = 0;
        for(int i:real){
            ItemStack item;
            if((item = inventory.getItem(i)).equals(null)){
                item = new ItemStack(Material.AIR);
            }
            stacks[count] = item;
            count++;
        }
        return stacks;
    }

    private static Map<Integer,List<ItemStack>> vertical;
    private static Map<Integer,List<ItemStack>> horizon;
    private static int firstHorizon;
    private static int firstVertical;
    private static int finalHorizon;
    private static int finalVertical;

    private boolean isThreeSquared(int size,Inventory inventory){
        vertical = getVerticalList(size,inventory);
        horizon = getHorizontalList(size,inventory);
        firstVertical = getLeftItem(vertical);
        firstHorizon = getLeftItem(horizon);
        finalVertical = getRightItem(vertical);
        finalHorizon = getRightItem(horizon);
        if(firstVertical==-1
                || firstHorizon==-1)return false;
        if(Math.abs(firstVertical-finalVertical)>3
                || Math.abs(firstHorizon - finalHorizon) > 3)return false;
        return Math.abs(firstVertical - firstHorizon) > 3;
    }

    private int getLeftItem(Map<Integer,List<ItemStack>> map){
        int first = -1;
        for(Map.Entry<Integer,List<ItemStack>> entry:map.entrySet()){
            for(ItemStack item:entry.getValue()){
                if(item!=null && !item.getType().equals(Material.AIR)){
                    first = entry.getKey();
                    break;
                }
            }
        }
        return first;
    }

    private int getRightItem(Map<Integer,List<ItemStack>> map){
        int _final = -1;
        for(Map.Entry<Integer,List<ItemStack>> entry:map.entrySet()){
            for(ItemStack item:entry.getValue()){
                if(!item.equals(null) && !item.getType().equals(Material.AIR)){
                    _final = entry.getKey();
                }
            }
        }
        return _final;
    }

    private Map<Integer,List<ItemStack>> getVerticalList(int size,Inventory inventory){
        Map<Integer,List<ItemStack>> map = new HashMap<>();
        for(int i=0;i<size;i++){
            List<ItemStack> list = new ArrayList<>();
            for(int j=0;j<size;j++){
                ItemStack item = inventory.getItem(j*9+i);
                list.add(item);
            }
            map.put(i,list);
        }
        return map;
    }

    private Map<Integer,List<ItemStack>> getHorizontalList(int size,Inventory inventory){
        Map<Integer,List<ItemStack>> map = new HashMap<>();
        for(int i=0;i<size;i++){
            List<ItemStack> list = new ArrayList<>();
            for(int j=0;j<size;j++){
                ItemStack item = inventory.getItem(i*9+j);
                list.add(item);
            }
            map.put(i,list);
        }
        return map;
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

    private Map<Integer,Integer> getRelation(int size,List<Integer> list){
        Map<Integer,Integer> map = new HashMap<>();
        for(int i=0;i<Math.pow(size,2);i++){
            map.put(i,list.get(i));
        }
        return map;
    }
}

package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.ClickInventory.anvilSlot;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.ClickInventory.resultSlot;

public class CreateInventory {
    public static Inventory inv(int size){
        Inventory result = Bukkit.createInventory(null,54,"CustomCrafter");
        for(int i=0;i<54;i++){ // 54 = 9slots * 6lines
            result.setItem(i,blank());
        }

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                result.clear(i*9+j); //place black-glass-pane
            }
        }
        CreateInventory ci = new CreateInventory();
        result.setItem(anvilSlot,ci.make());
        result.setItem(resultSlot,new ItemStack(Material.AIR));
        return result;
    }

    public static ItemStack blank(){
        ItemStack result = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName("");
        result.setItemMeta(meta);
        return result;
    }

    private ItemStack itemEdit(String material,List<String> lore,String name){
        ItemStack stack = new ItemStack(Material.getMaterial(material.toUpperCase()));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack make(){
        return this.itemEdit("anvil",Arrays.asList("Making items"),"Make");
    }
}

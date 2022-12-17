package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CreateInventory {
    public static Inventory inv(int size){
        Inventory result = Bukkit.createInventory(null,size*9);
        for(int i=0;i<size*9;i++){
            result.setItem(i,blank());
        }

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                result.clear(i*9+j); //place black-glass-pane
            }
        }
        CreateInventory ci = new CreateInventory();
        Map<Integer,ItemStack> sizes = new HashMap<>();
        sizes.put(3,ci.redstone());
        sizes.put(4,ci.emerald());
        sizes.put(5,ci.diamond());
        sizes.put(6,ci.netherite());
        sizes.remove(size); // remove slot that a player is opening
        List<ItemStack> list = new ArrayList<>();
        sizes.entrySet().forEach(s->list.add(s.getValue()));
        list.add(ci.make());

        for(int i=size*9-4;i<size*9;i++){
            int j = Math.abs(4-(size*9-i));
            result.setItem(i,list.get(j));
        }

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

    private ItemStack netherite(){
        return this.itemEdit("netherite_block",Arrays.asList("6 * 6 Crafting"),"6");
    }

    private ItemStack diamond(){
        return this.itemEdit("diamond_block",Arrays.asList("5 * 5 Crafting"),"5");
    }

    private ItemStack emerald(){
        return this.itemEdit("emerald_block",Arrays.asList("4 * 4 Crafting"),"4");
    }

    private ItemStack redstone(){
        return this.itemEdit("redstone_block",Arrays.asList("3 * 3 Crafting"),"3");
    }
}

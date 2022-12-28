package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.ClickInventory;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.CreateInventory.inv;

public class Transition {
    public Inventory transition(Inventory before,int newer,int beforeSize){
        List<ItemStack> stacks = new ArrayList<>();
        for(int i:new ClickInventory().getTableSlots(beforeSize)){
            if(before.getItem(i)==null
            || before.getItem(i).getType().equals(Material.AIR))continue;
            ItemStack item = before.getItem(i);
            stacks.add(item);
        }
        Inventory inventory = inv(newer);
        if(stacks.isEmpty()){
            return inventory;
        }
        List<ItemStack> normals = new ArrayList<>();
        if(stacks.size() > Math.pow(newer,2)){
            //overflow
            List<ItemStack> overflow = new ArrayList<>();
            for(int i=stacks.size()-1;i>Math.pow(newer,2);i--){
                overflow.add(stacks.get(i));
                stacks.remove(i);
            }
            ItemStack bundle = new ItemStack(Material.BUNDLE);
            BundleMeta meta = (BundleMeta)bundle.getItemMeta();
            meta.setDisplayName("Overflow items.");
            meta.setLore(Arrays.asList("An item container that overflowed."));
            meta.setItems(overflow);
            bundle.setItemMeta(meta);
            inventory.setItem(newer*9-2,bundle);

        }
        stacks.forEach(s->normals.add(s));

        int count = 0;
        for(int i:new ClickInventory().getTableSlots(newer)){
            try{
                inventory.setItem(i,stacks.get(count));
                count++;
            }catch (Exception e){
                break;
            }
        }
        return inventory;
    }
}

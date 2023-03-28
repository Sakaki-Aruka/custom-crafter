package com.github.sakakiaruka.customcrafter.customcrafter.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class InventoryUtil {
    public List<Integer> getTableSlots(int size){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int result = i*9+j;
                list.add(result);
            }
        }

        return list;
    }

    public List<Integer> getBlankCoordinates(int size){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<craftingTableTotalSize;i++){
            list.add(i);
        }
        list.removeAll(getTableSlots(size));
        list.removeAll(Arrays.asList(craftingTableMakeButton));
        list.removeAll(Arrays.asList(craftingTableResultSlot));
        return list;
    }

    public void decrementMaterials(Inventory inventory, Player player, int amount){
        // decrement crafting tables material
        // amount -> decrement amount
        List<Integer> slots = getTableSlots(craftingTableSize);
        for(int i:slots){
            if(inventory.getItem(i) == null)continue;
            int oldAmount = inventory.getItem(i).getAmount();
            int newAmount = oldAmount - amount < 0 ? 0 : oldAmount - amount;
            inventory.getItem(i).setAmount(newAmount);
        }
    }

    public void decrementResult(Inventory inventory,Player player){
        if(inventory.getItem(craftingTableResultSlot) == null)return;
        World world = player.getWorld();
        Location location = player.getLocation();
        ItemStack item = inventory.getItem(craftingTableResultSlot);
        world.dropItem(location,item); // drop
        inventory.setItem(craftingTableResultSlot,new ItemStack(Material.AIR));
    }
}

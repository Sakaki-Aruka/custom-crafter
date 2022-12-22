package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;

public class Search {
    public ItemStack search(Inventory inventory, Player player){
        int size = guiOpening.get(player);
        int total = 0;
        RecipeMaterial rm = new RecipeMaterial();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                try{
                    int key1 = i;
                    int key2 = j;

                    ItemStack item = inventory.getItem(i*9+j);
                    rm.put(new MultiKeys(key1,key2),item);
                }catch (Exception e){
                    rm.put(new MultiKeys(i,j),null);
                }
            }
        }
        ItemStack returnItem;
        if((returnItem = Bukkit.craftItem(rm.formatVanilla(rm),player.getWorld(),player)) != new ItemStack(Material.AIR))return returnItem;
        return null;
    }
}

package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.List;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;

public class CloseCrafter implements Listener {
    public void onInventoryClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        if(guiOpening.containsKey(player)){
            int size = guiOpening.get(player);
            guiOpening.remove(player);
            Inventory inventory = event.getInventory();
            ItemStack in;
            if(!(in = inventory.getItem(inventory.getSize()-2)).getType().equals(null)
            && in.equals(Material.BUNDLE)){
                try{
                    BundleMeta bm = (BundleMeta) in.getItemMeta();
                    List<ItemStack> list = bm.getItems();
                    for(ItemStack i:list){
                        player.getWorld().dropItem(player.getLocation(),i);
                    }
                }catch (Exception e){
                    return;
                }
            }
            for(int i:new ClickInventory().getTableSlots(size)){
                try{
                    ItemStack temporary = inventory.getItem(i);
                    player.getWorld().dropItem(player.getLocation(),temporary);
                }catch (Exception e){
                    continue;
                }
            }
        }
    }
}

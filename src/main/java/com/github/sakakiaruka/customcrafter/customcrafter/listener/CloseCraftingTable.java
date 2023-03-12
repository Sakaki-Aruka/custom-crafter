package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;

public class CloseCraftingTable implements Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        if(!opening.contains(player))return; // not opening crafting -> return

        Inventory inventory = event.getInventory();
        for(int i:new InventoryUtil().getTableSlots(6)){
            if(inventory.getItem(i)==null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            player.getWorld().dropItem(player.getLocation(),inventory.getItem(i));
        }

        if(inventory.getItem(44)==null)return;
        if(inventory.getItem(44).getType().equals(Material.AIR))return;
        player.getWorld().dropItem(player.getLocation(),inventory.getItem(44));
    }
}

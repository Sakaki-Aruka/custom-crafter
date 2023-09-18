package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.CRAFTING_TABLE_RESULT_SLOT;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.CRAFTING_TABLE_SIZE;
import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;

public class CloseCraftingTable implements Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        if(!opening.contains(player))return; // not opening crafting -> return
        opening.remove(player);
        Inventory inventory = event.getInventory();
        close(player,inventory);
    }

    public void close(Player player,Inventory inventory){
        for(int i:new InventoryUtil().getTableSlots(CRAFTING_TABLE_SIZE)){
            if(inventory.getItem(i)==null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            player.getWorld().dropItem(player.getLocation(),inventory.getItem(i));
            inventory.setItem(i,new ItemStack(Material.AIR));
        }

        if(inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) != null)player.getWorld().dropItem(player.getLocation(),inventory.getItem(CRAFTING_TABLE_RESULT_SLOT));
        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,new ItemStack(Material.AIR));

    }
}

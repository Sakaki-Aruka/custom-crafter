package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class ModifyCraftingInventory implements Listener {



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());

        //debug
        System.out.println("opening : "+opening);

        if(!opening.contains(player))return;

        int slot = event.getRawSlot();
        if(slot < 0){
            event.setCancelled(true);
            return;
        }


        ClickType clickType = event.getClick();
        if(slot >= craftingTableTotalSize){
            // click players inventory
            if(clickType.equals(ClickType.LEFT))return;
            if(clickType.equals(ClickType.RIGHT))return;
            if(clickType.equals(ClickType.SHIFT_LEFT))return;
            event.setCancelled(true);
            return;
        }

        Inventory inventory = event.getClickedInventory();
        if (slot == craftingTableMakeButton){
            // click make button
            event.setCancelled(true);
            if(clickType.equals(ClickType.RIGHT))new Search().batchSearch(player,inventory);
            else if(clickType.equals(ClickType.LEFT))new Search().main(player,inventory);
        }else if (slot == craftingTableResultSlot){
            // click result slot
            if(inventory.getItem(craftingTableResultSlot) == null)return;
            ItemStack item = inventory.getItem(craftingTableResultSlot);
            player.getWorld().dropItem(player.getLocation(),item);
        }else if(new InventoryUtil().getBlankCoordinates(craftingTableSize).contains(slot)){
            // click a blank slot
            event.setCancelled(true);
            return;
        }



    }

}

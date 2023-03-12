package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;

public class ModifyCraftingInventory implements Listener {

    private final int pageSize = 54;
    private final int anvil = 35;
    private final int resultSlot = 44;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
        if(!opening.contains(player))return;

        int slot = event.getRawSlot();
        if(slot < 0){
            event.setCancelled(true);
            return;
        }


        ClickType clickType = event.getClick();
        if(slot >= pageSize){
            // click players inventory
            if(clickType.equals(ClickType.LEFT))return;
            if(clickType.equals(ClickType.RIGHT))return;
            if(clickType.equals(ClickType.SHIFT_LEFT))return;
            event.setCancelled(true);
            return;
        }

        Inventory inventory = event.getClickedInventory();
        if (slot == anvil){
            // click make button
            if(clickType.equals(ClickType.RIGHT))new Search().batchSearch(player,inventory);
        }



    }

}

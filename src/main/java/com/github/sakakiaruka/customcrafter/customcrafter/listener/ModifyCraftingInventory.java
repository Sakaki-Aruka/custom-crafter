package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class ModifyCraftingInventory implements Listener {



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
        if(slot >= craftingTableTotalSize){
            // click players inventory
            if(clickType.equals(ClickType.LEFT))return;
            if(clickType.equals(ClickType.RIGHT))return;
            if(clickType.equals(ClickType.SHIFT_LEFT))return;
            event.setCancelled(true);
            return;
        }

        Inventory inventory = event.getClickedInventory();
        InventoryUtil util = new InventoryUtil();
        if (slot == craftingTableMakeButton){
            // click make button
            event.setCancelled(true);
            // replace old result items
            if(inventory.getItem(craftingTableResultSlot) != null){
                player.getWorld().dropItem(player.getLocation(),inventory.getItem(craftingTableResultSlot));
                inventory.setItem(craftingTableResultSlot,new ItemStack(Material.AIR));
            }
            if(clickType.equals(ClickType.RIGHT)){
                // batch
                new Search().batchSearch(player,inventory);
                // result item is null
                if(whatMaking.get(player.getUniqueId()) == null)return;
                int minimal = getMinimalAmount(inventory);

                util.decrementMaterials(inventory,player,minimal);
            }
            else if(clickType.equals(ClickType.LEFT)){
                // normal
                new Search().main(player,inventory);
                // result item is null
                if(whatMaking.get(player.getUniqueId()) == null)return;
                util.decrementMaterials(inventory,player,1);
            }


        }else if (slot == craftingTableResultSlot){
            // click result slot
            event.setCancelled(true);
            if(inventory.getItem(craftingTableResultSlot) == null)return;
            util.decrementResult(inventory,player); // remove result that clicked.

        }else if(new InventoryUtil().getBlankCoordinates(craftingTableSize).contains(slot)){
            // click a blank slot
            event.setCancelled(true);
            return;
        }

        // init making data.
        whatMaking.put(player.getUniqueId(),null);
    }

    private int getMinimalAmount(Inventory inventory){
        List<Integer> list = new ArrayList<>();
        for(int i:new InventoryUtil().getTableSlots(craftingTableSize)){
            if(inventory.getItem(i) == null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            list.add(inventory.getItem(i).getAmount());

            //debug
            System.out.println(String.format("material : %s | amount : %d",inventory.getItem(i).getType().name(),inventory.getItem(i).getAmount()));

        }
        if(list.isEmpty())return -1;
        Collections.sort(list);
        return list.get(0);
    }

}

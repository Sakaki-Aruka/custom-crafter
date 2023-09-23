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
        if(slot >= CRAFTING_TABLE_TOTAL_SIZE){
            // click players inventory
            if(clickType.equals(ClickType.LEFT))return;
            if(clickType.equals(ClickType.RIGHT))return;
            if(clickType.equals(ClickType.SHIFT_LEFT))return;
            event.setCancelled(true);
            return;
        }

        Inventory inventory = event.getClickedInventory();
        InventoryUtil util = new InventoryUtil();
        if (slot == CRAFTING_TABLE_MAKE_BUTTON){
            // click make button
            event.setCancelled(true);
            // <=== empty checker
            List<ItemStack> forCheck = new ArrayList<>();
            for(int i: util.getTableSlots(CRAFTING_TABLE_SIZE)){
                if(inventory.getItem(i) == null)continue;
                forCheck.add(inventory.getItem(i));
            }
            if(forCheck.isEmpty())return;
            // empty checker ===>
            // replace old result items
            if(inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) != null){

                InventoryUtil.safetyItemDrop(player, Collections.singletonList(inventory.getItem(CRAFTING_TABLE_RESULT_SLOT)));
                inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,new ItemStack(Material.AIR));
            }
            if(clickType.equals(ClickType.RIGHT)){
                // mass
                new Search().massSearch(player,inventory,false);
                // result item is null
                if(WHAT_MAKING.get(player.getUniqueId()) == null)return;
                int minimal = getMinimalAmount(inventory);

            }
            else if(clickType.equals(ClickType.LEFT)){
                // normal
                new Search().massSearch(player,inventory,true);
                // result item is null
                if(WHAT_MAKING.get(player.getUniqueId()) == null)return;
            }


        }else if (slot == CRAFTING_TABLE_RESULT_SLOT){
            // click result slot
            event.setCancelled(true);
            if(inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) == null)return;
            util.decrementResult(inventory,player); // remove result that clicked.

        }else if(new InventoryUtil().getBlankCoordinates(CRAFTING_TABLE_SIZE).contains(slot)){
            // click a blank slot
            event.setCancelled(true);
            return;
        }

        // init making data.
        WHAT_MAKING.put(player.getUniqueId(),null);
    }

    private int getMinimalAmount(Inventory inventory){
        List<Integer> list = new ArrayList<>();
        for(int i:new InventoryUtil().getTableSlots(CRAFTING_TABLE_SIZE)){
            if(inventory.getItem(i) == null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            list.add(inventory.getItem(i).getAmount());

        }
        if(list.isEmpty())return -1;
        Collections.sort(list);
        return list.get(0);
    }

}

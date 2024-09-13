package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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



    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event){
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());

        if (player == null || !opening.contains(player) || event.isCancelled()) return;

        int slot = event.getRawSlot();
        if (slot < 0){
            event.setCancelled(true);
            return;
        }

        ClickType clickType = event.getClick();
        if (slot >= CRAFTING_TABLE_TOTAL_SIZE) {
            // click players inventory
            if (clickType.equals(ClickType.LEFT) || clickType.equals(ClickType.RIGHT) || clickType.equals(ClickType.SHIFT_LEFT)) return;
            event.setCancelled(true);
            return;
        }

        Inventory inventory;
        if ((inventory  = event.getClickedInventory()) == null) return;
        if (slot == CRAFTING_TABLE_MAKE_BUTTON) {
            // click make button
            event.setCancelled(true);
            // <=== empty checker
            List<ItemStack> forCheck = new ArrayList<>();
            for(int i : InventoryUtil.getTableSlots(CRAFTING_TABLE_SIZE)) {
                if (inventory.getItem(i) == null) continue;
                forCheck.add(inventory.getItem(i));
            }
            if (forCheck.isEmpty()) return;
            // empty checker ===>

            if (clickType.equals(ClickType.SHIFT_LEFT)) {
                // mass
                new Search().massSearch(player,inventory,false);
                // result item is null
                if (WHAT_MAKING.get(player.getUniqueId()) == null) return;
            } else if(clickType.equals(ClickType.LEFT)) {
                // normal
                new Search().massSearch(player,inventory,true);
                // result item is null
                if (WHAT_MAKING.get(player.getUniqueId()) == null) return;
            }
        } else if (slot == CRAFTING_TABLE_RESULT_SLOT) {
            // click result slot
            event.setCancelled(true);
            if (inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) == null) return;
            InventoryUtil.decrementResult(inventory,player); // remove result that clicked.
        } else if (InventoryUtil.getBlankCoordinates(CRAFTING_TABLE_SIZE).contains(slot)) {
            // click a blank slot
            event.setCancelled(true);
            return;
        }
        // init making data.
        WHAT_MAKING.put(player.getUniqueId(), null);
    }
}
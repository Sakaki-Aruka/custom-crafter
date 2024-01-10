package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class OpenCraftingTable implements Listener {
    public static List<Player> opening = new ArrayList<>();
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if(event.getClickedBlock()==null)return;
        if(!event.getClickedBlock().getType().equals(Material.CRAFTING_TABLE))return;

        int x = event.getClickedBlock().getX();
        int y = event.getClickedBlock().getY() - 1;
        int z = event.getClickedBlock().getZ();
        World world = event.getClickedBlock().getWorld();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!world.getBlockAt(x + dx, y, z + dz).getType().equals(BASE_BLOCK)) return;
            }
        }

        //open crafting inventory (delay 2ticks = 0.10s)
        new BukkitRunnable(){
            @Override
            public void run(){
                event.getPlayer().openInventory(setCraftingInventory());
                opening.add(event.getPlayer());
            }
        }.runTaskLater(getInstance(),2);

    }

    public Inventory setCraftingInventory(){
        Inventory inventory = Bukkit.createInventory(null,9 * CRAFTING_TABLE_SIZE,"Custom Crafter");
        ItemStack blank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = blank.getItemMeta();
        meta.setDisplayName("-");
        blank.setItemMeta(meta);
        for(int i = 0; i< CRAFTING_TABLE_TOTAL_SIZE; i++){
            inventory.setItem(i,blank);
        }
        InventoryUtil.getTableSlots(CRAFTING_TABLE_SIZE).forEach(s->{
            inventory.setItem(s,new ItemStack(Material.AIR));
        });

        //set craft button (anvil)
        ItemStack make = new ItemStack(Material.ANVIL);
        ItemMeta anvilMeta = make.getItemMeta();
        anvilMeta.setDisplayName("§fMake");
        anvilMeta.setLore(Arrays.asList("§fMaking items."));
        make.setItemMeta(anvilMeta);
        inventory.setItem(CRAFTING_TABLE_MAKE_BUTTON,make);

        //set result slot
        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,new ItemStack(Material.AIR));
        return inventory;
    }
}

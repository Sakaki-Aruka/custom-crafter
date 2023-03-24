package com.github.sakakiaruka.customcrafter.customcrafter.listener;

import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    private static final double degrees = 2 * Math.PI / (360 / 30);
    private static final double radius = 1;
    private static Inventory craftingInventory;
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getClickedBlock()==null)return;
        if(!event.getClickedBlock().getType().equals(Material.CRAFTING_TABLE))return;
        Location location = event.getClickedBlock().getLocation();
        for(double d = 0;d<(2 * Math.PI);d+=degrees){
            double x = radius * Math.cos(d) + location.getX();
            double z = radius * Math.sin(d) + location.getZ();
            double y = location.getY() - 1;
            Location loc = new Location(location.getWorld(),x,y,z);

            if(!loc.getBlock().getType().equals(baseBlock))return;
        }

        //open crafting inventory (delay 2ticks = 0.10s)
        new BukkitRunnable(){
            @Override
            public void run(){
                event.getPlayer().openInventory(craftingInventory);
                opening.add(event.getPlayer());
            }
        }.runTaskLater(getInstance(),2l);

    }

    public void setCraftingInventory(){
        Inventory inventory = Bukkit.createInventory(null,9 * craftingTableSize,"Custom Crafter");
        ItemStack blank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = blank.getItemMeta();
        meta.setDisplayName("-");
        blank.setItemMeta(meta);
        for(int i=0;i<craftingTableTotalSize;i++){
            inventory.setItem(i,blank);
        }
        new InventoryUtil().getTableSlots(craftingTableSize).forEach(s->{
            inventory.setItem(s,new ItemStack(Material.AIR));
        });

        //set craft button (anvil)
        ItemStack make = new ItemStack(Material.ANVIL);
        ItemMeta anvilMeta = make.getItemMeta();
        anvilMeta.setDisplayName("§fMake");
        anvilMeta.setLore(Arrays.asList("§fMaking items."));
        make.setItemMeta(anvilMeta);
        inventory.setItem(craftingTableMakeButton,make);

        //set result slot
        inventory.setItem(craftingTableResultSlot,new ItemStack(Material.AIR));

        craftingInventory = inventory;
    }
}

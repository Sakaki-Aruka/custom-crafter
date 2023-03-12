package com.github.sakakiaruka.customcrafter.customcrafter.listener;

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
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.baseBlock;

public class OpenCraftingTable implements Listener {
    public static List<Player> opening = new ArrayList<>();

    private static double degrees = 2 * Math.PI / (360 / 30);
    private static double radius = 1;
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
        Inventory inventory = Bukkit.createInventory(null,9 * 6,"Custom Crafter");
        //set blanks
        for(int y=0;y<6;y++){
            for(int x=0;x<9;x++){
                if(x < 5)continue;
                ItemStack blank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = blank.getItemMeta();
                meta.setDisplayName("blank");
                blank.setItemMeta(meta);
                inventory.setItem((x+y*9),blank);
            }
        }

        //set craft button (anvil)
        ItemStack make = new ItemStack(Material.ANVIL);
        ItemMeta anvilMeta = make.getItemMeta();
        anvilMeta.setDisplayName("§fMake");
        anvilMeta.setLore(Arrays.asList("§fMaking items."));
        make.setItemMeta(anvilMeta);
        inventory.setItem(35,make);

        //set result slot
        inventory.setItem(44,new ItemStack(Material.AIR));

        craftingInventory = inventory;
    }
}

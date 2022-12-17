package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.CreateInventory.inv;

public class OpenCrafter implements Listener {
    public static Map<Player,Integer> guiOpening = new HashMap<>();
    private static double degrees = 2*Math.PI / (360 / 45);
    private static double radius = 1;
    private static Material baseBlock = Material.IRON_BLOCK;

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event){
        if(event.getInventory().getType().equals(InventoryType.CRAFTING)){
            Location location = event.getInventory().getLocation();
            for(double d=0;d<(2*Math.PI);d+=degrees){
                double x = radius*Math.cos(d);
                double z = radius*Math.sin(d);
                double y = location.getY()-1;
                Location loc = new Location(location.getWorld(), x,y,z);
                if(!loc.getBlock().getType().equals(baseBlock)){
                    return;
                }
            }
            int size = 3;
            Player player = (Player) event.getPlayer();
            player.closeInventory();  //to close default crafting-table inventory.
            Inventory inventory = inv(size);
            player.openInventory(inventory);
            guiOpening.put(player,size);
        }
    }
}

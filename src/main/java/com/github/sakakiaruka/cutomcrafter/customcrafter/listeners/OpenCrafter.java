package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.CreateInventory.inv;

public class OpenCrafter extends BukkitRunnable implements Listener{
    public static Map<Player,Integer> guiOpening = new HashMap<>();
    private static double degrees = 2*Math.PI / (360 / 45);
    private static double radius = 1;
    private static Material baseBlock = Material.IRON_BLOCK;
    private static List<Player> closes = new ArrayList<>();
    private static Map<Player,Integer> map = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){

        if(!event.getClickedBlock().equals(null) && event.getClickedBlock().getType().equals(Material.CRAFTING_TABLE)){
            Location location = event.getClickedBlock().getLocation();
            for(double d=0;d<(2*Math.PI);d+=degrees){
                double x = radius*Math.cos(d) + location.getX();
                double z = radius*Math.sin(d) + location.getZ();
                double y = location.getY()-1;
                Location loc = new Location(location.getWorld(), x,y,z);

                //debug
                System.out.println("loc block:"+loc.getBlock().getType()+" / xyz:"+x+","+y+","+z);

                if(!loc.getBlock().getType().equals(baseBlock)){
                    return;
                }
            }

            //debug
            System.out.println("finished");

            int size = 3;
            long delay = 3; // tick
            Player player = event.getPlayer();
            closes.add(player);
            map.put(player,size);
            this.runTaskLater(getInstance(),delay);
        }
    }

    @Override
    public void run(){
        Player player = closes.get(0);
        int size = map.get(player);
        player.closeInventory();
        Inventory inventory = inv(size);
        player.openInventory(inventory);
        guiOpening.put(player,size);
        closes.remove(player);
        map.remove(player);
    }
}

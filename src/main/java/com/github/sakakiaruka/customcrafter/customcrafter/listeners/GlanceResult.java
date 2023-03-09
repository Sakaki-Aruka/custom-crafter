package com.github.sakakiaruka.customcrafter.customcrafter.listeners;

import com.github.sakakiaruka.customcrafter.customcrafter.listeners.clickInventorysMethods.Search;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.listeners.OpenCrafter.guiOpening;
import static com.github.sakakiaruka.customcrafter.customcrafter.some.CreateInventory.blank;

public class GlanceResult extends BukkitRunnable implements Listener {

    public static List<Player> glanceQueue = new ArrayList<>();
    private static final int bundleSlot = 53;
    private static final long delay = 1;

    @EventHandler
    public void onDrag(InventoryDragEvent event){
        Player player = (Player) event.getWhoClicked();
        if(!guiOpening.containsKey(player))return;

        glanceQueue.add(player);
        this.runTaskLater(getInstance(),delay);
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if(!guiOpening.containsKey(player))return;

        int clicked = event.getRawSlot();
        int size = guiOpening.get(player);
        int invSize = size * 9;
        if(clicked > invSize)return;

        glanceQueue.add(player);
        this.runTaskLater(getInstance(),delay);

    }

    @Override
    public void run(){
        Player player = glanceQueue.get(0);
        if(!guiOpening.containsKey(player)){
            guiOpening.remove(player);
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();
        int size = guiOpening.get(player);
        ItemStack item = getGlanceBundle(inventory,size);
        inventory.setItem(bundleSlot,item);
        glanceQueue.remove(player);
    }

    private ItemStack getGlanceBundle(Inventory inventory,int size) {
        //debug
        List<ItemStack> list = new Search().search(inventory, size);
        ItemStack bundle = new ItemStack(Material.BUNDLE);
        BundleMeta meta = (BundleMeta) bundle.getItemMeta();

        ItemStack blank = blank();
        if (list == null) return blank;
        if (list.isEmpty()) return blank;
        if (list.get(0).getType().equals(Material.AIR)) return blank;

        meta.setDisplayName("A glace of result items.");
        meta.setItems(list);
        bundle.setItemMeta(meta);
        return bundle;
    }
}

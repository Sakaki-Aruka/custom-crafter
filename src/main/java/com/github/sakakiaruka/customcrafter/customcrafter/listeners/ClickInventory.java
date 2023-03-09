package com.github.sakakiaruka.customcrafter.customcrafter.listeners;

import com.github.sakakiaruka.customcrafter.customcrafter.listeners.clickInventorysMethods.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.listeners.OpenCrafter.guiOpening;

public class ClickInventory implements Listener {
    private final int page_size = 54;

    public static final int bundleSlot = 34;
    public static final int anvilSlot = 35;
    public static final int resultSlot = 44;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
        if(!guiOpening.containsKey(player))return;

        Inventory inventory = event.getClickedInventory();
        int slot = event.getRawSlot();
        int size = guiOpening.get(player);
        ClickType type = event.getClick();
        if(slot<0){
            event.setCancelled(true);
            return;
        }

        if(!isPermittedClick(slot,event.getClick())){
            event.setCancelled(true);
            return;
        }

        if(slot >= page_size){
            // click players inventory
            if(type.equals(ClickType.LEFT))return;
            if(type.equals(ClickType.RIGHT))return;
            if(type.equals(ClickType.SHIFT_LEFT))return;
            event.setCancelled(true);
            return;
        }

        if(slot == anvilSlot){
            player.playSound(player,Sound.BLOCK_PISTON_EXTEND,1.0f,1.0f);
            if(inventory.getItem(page_size-1-9)!=null){
                // result slot is full.
                event.setCancelled(true);
                return;
            }

            if(type.equals(ClickType.RIGHT)){
                // right click
                new BatchCreate().main(player,inventory,size);
                event.setCancelled(true);
                return;
            }

            // --- left click --- //
            ItemStack result;
            if(new Search().search(inventory,size)!=null){
                //custom recipe search
                result = new Search().search(inventory,size).get(0);
            }else{
                //vanilla recipe search
                result = new SearchVanilla().isThreeSquared(inventory,size,player);
            }

            if(result==null){
                event.setCancelled(true);
                return;
            }

            if(!result.getType().equals(Material.AIR)){
                inventory.setItem(page_size-1-9,result);
                new ItemsSubtract().main(inventory,size,1);
            }

        }else if(slot == resultSlot){
            if(new ItemStackComparison().notEmpty(inventory.getItem(slot))){
                ItemStack result = inventory.getItem(slot);
                resultTake(player.getInventory(), result,player);
                inventory.setItem(slot,new ItemStack(Material.AIR));
            }
        }else if(getTableSlots(size).contains(slot)){
            // click crafting table (Resetting crafting slots when an item are moved by a player.)
            if(!new ItemStackComparison().notEmpty(inventory.getItem(resultSlot)))return;
            inventory.setItem(resultSlot,new ItemStack(Material.AIR));
        }

        event.setCancelled(true);

    }

    public List<Integer> getTableSlots(int size){
        List<Integer> slots = new ArrayList<>();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int result = i*9+j;
                slots.add(result);
            }
        }
        return slots;
    }

    private boolean isPermittedClick(int slot,ClickType type){
        if(slot < page_size){
            if(!type.equals(ClickType.LEFT)){
                if(!type.equals(ClickType.RIGHT)){
                    return false;
                }
            }
        }
        return true;
    }

    private void resultTake(Inventory inventory,ItemStack item,Player player){
        int add = item.getAmount();
        int max = item.getMaxStackSize();
        for(int i=0;i<36;i++){
            ItemStack real;
            if(inventory.getItem(i)==null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            if((real = inventory.getItem(i)).equals(item)){
                int amount = real.getAmount();
                if(amount+add <= max){
                    real.setAmount(amount+add);
                    add =0;
                }else if(amount+add > max){
                    real.setAmount(max);
                    add = max - amount;
                }
            }
            if(add==0)return;
        }
        player.getWorld().dropItem(player.getLocation(),item);
    }
}

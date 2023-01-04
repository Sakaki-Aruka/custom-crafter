package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.*;
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

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;

public class ClickInventory implements Listener {
    public static List<Player> isTransitionMode = new ArrayList<>();
    public static final int bundleSlot = 34;
    public static final int anvilSlot = 35;
    public static final int resultSlot = 44;
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());

        if(!guiOpening.containsKey(player)){
            //not open crafting-gui
            return;
        }

        Inventory inventory = event.getClickedInventory();
        int slot = event.getRawSlot();
        int size = guiOpening.get(player);
        final int absSize = 54;


        if(slot<0){
            event.setCancelled(true);
            return;
        }

        if(slot < absSize){
            if(!event.getClick().equals(ClickType.LEFT)){
                if(!event.getClick().equals(ClickType.RIGHT)){
                    event.setCancelled(true);
                    return;
                }
            }

            // click crafting-gui
            if(slot == anvilSlot){
                // click make button(anvil)
                player.playSound(player, Sound.BLOCK_PISTON_EXTEND,1.0f,1.0f);

                if(inventory.getItem(absSize-1-9)!=null){
                    // result slot is full.
                    event.setCancelled(true);
                    return;
                }

                if(event.getClick().equals(ClickType.RIGHT)){
                    new BatchCreate().main(player,inventory,size);
                    event.setCancelled(true);
                    return;
                }

                ItemStack result;
                if(new Search().search(inventory,size)!=null){
                    //custom recipe item found
                    result = new Search().search(inventory,size);
                }else{
                    //vanilla item found
                    result = new SearchVanilla().isThreeSquared(inventory,size,player);
                }
                if(result==null){
                    event.setCancelled(true);
                    return;
                }
                if(!result.getType().equals(Material.AIR)){
                    inventory.setItem(absSize-1-9*1,result);
                    new ItemsSubtract().main(inventory,size,1);
                }
                event.setCancelled(true);
                return;
            }else if(slot == resultSlot){
                //result slot
                if(new ItemStackComparison().notEmpty(inventory.getItem(slot))){
                    ItemStack result = inventory.getItem(slot);
                    resultTake(player.getInventory(),result,player);
                    inventory.setItem(slot,new ItemStack(Material.AIR));
                    event.setCancelled(true);
                    return;
                }
            }
            else if(new Transition().transitionCondition(slot,inventory.getItem(7),event.getClick())) {
                //click overflow items
                new Transition().dropItems(inventory.getItem(7),player);
                inventory.setItem(7,new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
                return;
            }else if(getTableSlots(size).contains(slot)){
                //click crafting table
                if(new ItemStackComparison().notEmpty(inventory.getItem(resultSlot))){
                    inventory.setItem(resultSlot,new ItemStack(Material.AIR));
                }
                return;
            }
            event.setCancelled(true);

        }else if(slot >= absSize){
            // click players inventory
            if(event.getClick().equals(ClickType.LEFT))return;
            if(event.getClick().equals(ClickType.RIGHT))return;
            if(event.getClick().equals(ClickType.SHIFT_LEFT))return;
            event.setCancelled(true);
        }
    }

    private void close(int after,int before,Inventory old,Player player){
        Inventory newer = new Transition().transition(old,after,before);
        isTransitionMode.add(player);
        player.closeInventory();
        player.openInventory(newer);
        guiOpening.put(player,after);
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

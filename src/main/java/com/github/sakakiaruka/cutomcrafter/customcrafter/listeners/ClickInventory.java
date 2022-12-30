package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.*;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiOriginalRecipe;
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
import org.bukkit.inventory.meta.BundleMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;

public class ClickInventory implements Listener {
    public static List<Player> isTransitionMode = new ArrayList<>();
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
        if(slot < absSize){
            if(!event.getClick().equals(ClickType.LEFT)){
                if(!event.getClick().equals(ClickType.RIGHT)){
                    event.setCancelled(true);
                    return;
                }
            }

            // click crafting-gui
            if(slot == absSize-1-9*2){
                // click make button(anvil)
                player.playSound(player, Sound.BLOCK_PISTON_EXTEND,1.0f,1.0f);

                if(inventory.getItem(absSize-1-9)!=null){
                    // result slot is full.
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
                inventory.setItem(absSize-1-9*1,result);
                if(inventory.getItem(44)!=null
                && !inventory.getItem(44).equals(Material.AIR))
                    new ItemsSubtract().main(inventory,size,1);
                event.setCancelled(true);
                return;
            }else if(slot == 44){
                //result slot
                if(inventory.getItem(slot)!=null
                        && !inventory.getItem(slot).getType().equals(Material.AIR)){
                    ItemStack result = inventory.getItem(slot);
                    resultTake(player.getInventory(),result,player);
                    inventory.setItem(slot,new ItemStack(Material.AIR));
                    event.setCancelled(true);
                    return;
                }
            }else if((slot+1)%9==0 && slot < 3*9 && slot+1 > 0){
                //crafting table size change (3*9 = 3lines * 9slots
                Material mode;
                if(inventory.getItem(slot)==null){
                    event.setCancelled(true);
                    return;
                }
                if((mode = inventory.getItem(slot).getType())==Material.AIR){
                    event.setCancelled(true);
                    return;
                }
                if(mode.equals(Material.NETHERITE_BLOCK)){
                    close(6,size,inventory,player);
                }else if(mode.equals(Material.DIAMOND_BLOCK)){
                    close(5,size,inventory,player);
                }else if(mode.equals(Material.EMERALD_BLOCK)){
                    close(4,size,inventory,player);
                }else if(mode.equals(Material.REDSTONE_BLOCK)){
                    close(3,size,inventory,player);
                }
                return;
            }else if(slot == absSize-2
                    && inventory.getItem(slot).getType().equals(Material.BUNDLE)
                    && event.getClick().equals(ClickType.RIGHT)){
                ItemStack bundle = inventory.getItem(slot);
                BundleMeta meta = (BundleMeta) bundle.getItemMeta();
                List<ItemStack> bundleItems = meta.getItems();
                for(ItemStack item:bundleItems){
                    if(item==null || item.equals(Material.AIR))continue;
                    player.getWorld().dropItem(player.getLocation(),item);
                }
                inventory.setItem(slot,new ItemStack(Material.AIR));
                return;
            }else if(getTableSlots(size).contains(slot))return; // click crafting slots
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

    private void tableClear(Inventory inventory,int size){
        for(int i:getTableSlots(size)){
            inventory.clear(i);
        }
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

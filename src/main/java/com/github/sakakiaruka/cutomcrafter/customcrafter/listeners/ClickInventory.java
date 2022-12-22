package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.Search;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.Recipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipePlace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.*;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.CreateInventory.blank;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.CreateInventory.inv;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipes;

public class ClickInventory implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
        if(guiOpening.containsKey(player) && event.getClick().equals(ClickType.LEFT)){
            //if a player is opening custom-crafter-gui.
            int slot = event.getRawSlot();
            int size = guiOpening.get(player)*9;
            int absSize = 54; //54 = 9slots * 6lines
            if(slot < absSize){

                Inventory inventory = event.getClickedInventory();
                if(getTableSlots(size/9).contains(slot)){
                    event.setCancelled(true);
                    return;
                }
                else if(getTableSlots(size/9).contains(slot)){
                    // a player clicks crafting slots
                    
                } else if(slot == absSize-1-9*2){ //if click anvil (make button)
                    player.playSound(player, Sound.BLOCK_PISTON_EXTEND,1.0f,1.0f);
                    // write search and drop result-items.
                    ItemStack result;
                    if((result = new Search().search(inventory,player))!=null && result!=new ItemStack(Material.AIR)){
                        Inventory init = inv(size/9);
                        init.setItem(size-9-1,result); // size-9-1 = under right corner + 1Line
                        return;
                    }

                }else if(slot == absSize-1*9){ // if click result-slot
                    ItemStack result;
                    if((result = event.getClickedInventory().getItem(absSize-1))!= new ItemStack(Material.AIR)){
                        int empty;
                        if((empty=player.getInventory().firstEmpty())!=-1){
                            player.getInventory().setItem(empty,result); // to put result-item to an empty slot that players.
                        }else{
                            player.getWorld().dropItem(player.getLocation(),result); // to drop a result item cause a player has not an empty slot
                        }
                        event.getClickedInventory().setItem(size-1,new ItemStack(Material.AIR));
                        return;
                    }
                }else if((slot+1)%9==0 && slot < 3*9){ //crafting table size change (3*9 = 3lines * 9slots
                    Material mode;
                    if((mode = event.getClickedInventory().getItem(slot).getType()).equals(null))return;

                    //debug
                    System.out.println("mode change:"+event.getInventory().getItem(slot).getType());

                    int before = guiOpening.get(player);
                    if(mode.equals(Material.NETHERITE_BLOCK)){
                        closeAndOpen(6,before,inventory,player);
                    }else if(mode.equals(Material.DIAMOND_BLOCK)){
                        closeAndOpen(5,before,inventory,player);
                    }else if(mode.equals(Material.EMERALD_BLOCK)){
                        closeAndOpen(4,before,inventory,player);
                    }else if(mode.equals(Material.REDSTONE_BLOCK)){
                        closeAndOpen(3,before,inventory,player);
                    }
                }else if(slot == absSize-2
                        && inventory.getItem(slot).getType().equals(Material.BUNDLE)
                        && event.getClick().equals(ClickType.RIGHT)){ // click overflow container (bundle)
                    ItemStack bundle = inventory.getItem(slot);
                    BundleMeta bundleMeta = (BundleMeta)bundle.getItemMeta();
                    List<ItemStack> bundles = bundleMeta.getItems();
                    int bundleSize = bundles.size();
                    int firstEmpty;
                    if((firstEmpty = player.getInventory().firstEmpty())!=-1){ // contains an empty slot
                        player.getInventory().setItem(firstEmpty,bundles.get(0));
                    }else{
                        player.getWorld().dropItem(player.getLocation(),bundles.get(0));
                    }
                    if(bundleSize<=1){
                        event.getInventory().setItem(slot,blank());
                    }
                }
                event.setCancelled(true); // if a player clicks custom-crafter-gui.
            }
        }else if(guiOpening.containsKey(player)){
            event.setCancelled(true);
        }
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

    private void closeAndOpen(int after,int before,Inventory old,Player player){
        Inventory newer = transition(old,after,before);
        player.closeInventory();
        player.openInventory(newer);
        guiOpening.put(player,after);
    }

    private Inventory transition(Inventory before,int newer,int beforeSize){

        // to part crafting slots items and something else.
        List<ItemStack> list = new ArrayList<>();
        int count = 0;
        for(int i=0;i<beforeSize;i++){
            for(int j=0;j<beforeSize;j++){
                ItemStack crafting;
                try{
                    if(!(crafting=before.getItem(i*9+j)).equals(null) || crafting.equals(new ItemStack(Material.AIR))){
                        list.add(crafting);
                    }
                }catch (NullPointerException npe){
                    continue;
                }
            }
        }
        if(list.isEmpty())return inv(newer);

        Inventory newInv = inv(newer);
        if(list.size() > Math.pow(newer,2)){ //over items
            ItemStack bundle = new ItemStack(Material.BUNDLE);
            BundleMeta bundleMeta = (BundleMeta) bundle.getItemMeta();

            List<ItemStack> inRange = getOverFlow(list,newer).get(true);
            List<ItemStack> outRange = getOverFlow(list,newer).get(false);

            bundleMeta.setItems(outRange);
            bundleMeta.setDisplayName("Over flow items.");
            bundleMeta.setLore(Arrays.asList("A items container that over flowed."));
            bundle.setItemMeta(bundleMeta);

            newInv.setItem(newer*9-2,bundle);
            for(int i=0;i<newer;i++){
                for(int j=0;j<newer;j++){
                    try{
                        newInv.setItem(i*9+j,inRange.get(0));
                        inRange.remove(0);
                    }catch (Exception e){
                        break;
                    }
                }
            }
        }else{ // non-overflow
            for(int i=0;i<newer;i++){
                for(int j=0;j<newer;j++){
                    try{
                        newInv.setItem(i*9+j,list.get(0));
                        list.remove(0);
                    }catch(Exception e){
                        break;
                    }
                }
            }
        }
        return newInv;
    }

    private Map<Boolean,List<ItemStack>> getOverFlow(List<ItemStack> old,int newer){
        Map<Boolean,List<ItemStack>> map = new HashMap<>();
        List<ItemStack> inRange = new ArrayList<>();
        for(int i=0;i<newer;i++){
            inRange.add(old.get(i));
        }
        map.put(true,inRange);

        List<ItemStack> outRange = new ArrayList<>();
        for(int i=newer;i<old.size();i++){
            outRange.add(old.get(i));
        }
        map.put(false,outRange);

        return map;
    }

}

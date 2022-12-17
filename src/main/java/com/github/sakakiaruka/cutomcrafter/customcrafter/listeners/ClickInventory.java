package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.Recipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipePlace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.OpenCrafter.guiOpening;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.CreateInventory.blank;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.CreateInventory.inv;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipes;

public class ClickInventory implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
        if(guiOpening.containsKey(player)){
            //if a player is opening custom-crafter-gui.
            int slot = event.getRawSlot();
            int size = guiOpening.get(player)*9;
            if(slot < size){
                Inventory inventory = event.getClickedInventory();
                if(slot == size-1){
                    player.playSound(player, Sound.BLOCK_PISTON_EXTEND,1.0f,1.0f);
                    // write search and drop result-items.
                    ItemStack result;
                    if((result = search(inventory,player))!=null){
                        Inventory init = inv(size/9);
                        init.setItem(size-9-1,result); // size-9-1 = under right corner + 1Line
                        return;
                    }
                }else if(slot == size-9-1){
                    ItemStack result;
                    if((result = event.getClickedInventory().getItem(size-9-1))!= blank()){
                        int empty;
                        if((empty=player.getInventory().firstEmpty())!=-1){
                            player.getInventory().setItem(empty,result);
                        }else{
                            player.getWorld().dropItem(player.getLocation(),result);
                        }
                        event.getClickedInventory().setItem(size-9-1,blank());
                        return;
                    }
                }else if(size-4 <= slot && slot < size){ //crafting table size change
                    Material mode = event.getClickedInventory().getItem(slot).getType();
                    if(mode.equals(Material.NETHERITE_BLOCK)){
                        closeAndOpen(6,inventory,player);
                    }else if(mode.equals(Material.DIAMOND_BLOCK)){
                        closeAndOpen(5,inventory,player);
                    }else if(mode.equals(Material.EMERALD_BLOCK)){
                        closeAndOpen(4,inventory,player);
                    }else if(mode.equals(Material.COAL_BLOCK)){
                        closeAndOpen(3,inventory,player);
                    }
                }
                event.setCancelled(true); // if a player clicks custom-crafter-gui.
            }
        }
    }

    private void closeAndOpen(int after,Inventory old,Player player){
        Inventory sized = inv(after);
        Inventory newer = transition(old,sized,after,player);
        player.closeInventory();
        player.openInventory(newer);
        guiOpening.put(player,after);
    }

    private Inventory transition(Inventory before,Inventory after,int newer,Player player){
        List<ItemStack> bs = Arrays.asList(before.getContents());
        if(bs.size() > Math.pow(newer,2)){
            for(int i=0;i<newer;i++){
                for(int j=0;j<newer;j++){
                    after.setItem(i*9+j,bs.get(0));
                    bs.remove(0);
                }
            }
            for(ItemStack s:bs){
                player.getWorld().dropItem(player.getLocation(),s);
            }
        }else{
            for(int i=0;i<newer;i++){
                for(int j=0;j<newer;j++){
                    try{
                        after.setItem(i*9+j,bs.get(0));
                        bs.remove(0);
                    }catch (Exception e){
                        return after;
                    }
                }
            }
        }
        return after;
    }

    private ItemStack search(Inventory inventory,Player player){
        int size = guiOpening.get(player);
        int total = 0;
        List<RecipePlace> rps = new ArrayList<>();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                try{
                    RecipePlace rp = new RecipePlace(i,j,inventory.getItem(i*9+j));
                    rps.add(rp);
                    total++;
                }catch (Exception e){
                    RecipePlace rp = new RecipePlace(i,j,null);
                    rps.add(rp);
                }
            }
        }

        List<Recipe> rs = new ArrayList<>();
        for(Recipe r:recipes){
            if(r.getTotal()==total){
                rs.add(r);
            }
        }
        if(rs.isEmpty()) {
            return null; // no suggest recipes are there.
        }

        for(Recipe r:rs){
            if (checkDiff(r.toRecipePlace(r),rps)){
                return r.getResult();
            }
        }
        return null;

    }

    private boolean checkDiff(List<RecipePlace> model, List<RecipePlace> real){
        if(model.size()!=real.size()){
            return false;
        }
        int xDiff = 0;
        int yDiff = 0;
        for(int i=0;i<model.size();i++){
            int mx = model.get(i).getX();
            int rx = real.get(i).getX();

            int my = model.get(i).getY();
            int ry = real.get(i).getY();

            if(model.get(i).getItem()!=real.get(i).getItem()){
                return false;
            }

            if(i==0){
                xDiff = Math.abs(mx-rx);
                yDiff = Math.abs(my-ry);
            }else{
                if(Math.abs(mx-rx)!=xDiff || Math.abs(my-ry)!=yDiff){
                    return false;
                }
            }
        }
        return true;
    }
}

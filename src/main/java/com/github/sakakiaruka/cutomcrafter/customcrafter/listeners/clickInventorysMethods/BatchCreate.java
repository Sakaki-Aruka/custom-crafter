package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchCreate {
    public void main(Player player, Inventory inventory,int size){
        RecipeMaterial rm = new RecipeMaterial();
        List<Integer> amount = new ArrayList<>();
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                MultiKeys key = new MultiKeys(x,y);
                ItemStack item;
                if((item = inventory.getItem(x+y*9))==null){
                    rm.put(key,new ItemStack(Material.AIR));
                }else{
                    rm.put(key,item);
                    if(item.getType().equals(Material.AIR))continue;
                    amount.add(item.getAmount());
                }
            }
        }

        Collections.sort(amount);
        if(amount.isEmpty())return;
        int minAmount = amount.get(0);
        //RecipeMaterial cloned = rm.recipeMaterialClone();
        rm.setAllAmount(1);

        ItemStack result;
        result = new Search().search(inventory,size);
        if(result==null)result=new SearchVanilla().isThreeSquared(inventory,size,player);

        if(result.getType().equals(Material.AIR))return;
        if(result.getAmount()*minAmount <= result.getMaxStackSize()){
            result.setAmount(minAmount* result.getAmount());
            player.getWorld().dropItem(player.getLocation(),result);
        }else{
            int stored = result.getAmount()*minAmount;
            result.setAmount(stored);

            player.getWorld().dropItem(player.getLocation(),result);
        }
        new ItemsSubtract().main(inventory,size,minAmount);
    }
}

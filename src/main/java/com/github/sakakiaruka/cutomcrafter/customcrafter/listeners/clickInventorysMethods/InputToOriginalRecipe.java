package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.ClickInventory;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiOriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputToOriginalRecipe {
    public MultiOriginalRecipe main(Inventory inventory,int size){
        List<Integer> amounts = new ArrayList<>();
        RecipeMaterial rm = new RecipeMaterial();
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                MultiKeys key = new MultiKeys(x,y);
                try{
                    ItemStack item = inventory.getItem(x+y*9);
                    rm.put(key,item);
                    amounts.add(item.getAmount());
                }catch (NullPointerException e){
                    rm.put(key,new ItemStack(Material.AIR));
                }
            }
        }

        if(amounts.isEmpty())return null;
        OriginalRecipe original = new OriginalRecipe(size,getTotal(amounts),rm);
        int originalRecipeAmount = getMinimum(amounts);
        MultiOriginalRecipe multiOriginal = new MultiOriginalRecipe(originalRecipeAmount,original,getRemaining(inventory,size,originalRecipeAmount));
        return multiOriginal;
    }

    private int getTotal(List<Integer> list){
        int total = 0;
        for(int i:list){
            total+=i;
        }
        return total;
    }

    private int getMinimum(List<Integer> list){
        int min = list.get(0);
        for(int i:list){
            if(i < min)min=i;
        }
        return min;
    }

    private Map<Integer,ItemStack> getRemaining(Inventory inventory,int size,int min){
        List<Integer> tables = new ClickInventory().getTableSlots(size);
        Map<Integer,ItemStack> remaining = new HashMap<>();
        for(int i:tables){
            int amount;
            try{
                if((amount = inventory.getItem(i).getAmount()) > min){
                    inventory.getItem(i).setAmount(amount-min);
                    remaining.put(i,inventory.getItem(i));
                }
            }catch (NullPointerException e){
                continue;
            }

        }
        return remaining;
    }
}

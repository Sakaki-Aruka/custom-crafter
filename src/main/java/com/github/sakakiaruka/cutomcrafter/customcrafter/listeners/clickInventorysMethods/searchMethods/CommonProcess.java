package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommonProcess {
    public int getTotal(RecipeMaterial in){
        int result = 0;
        for(Map.Entry<MultiKeys, ItemStack> entry:in.getRecipeMaterial().entrySet()){
            if(entry.getValue().getType().equals(Material.AIR))continue;
            result+=entry.getValue().getAmount();
        }
        return result;
    }

    public int getSquareSize(RecipeMaterial in){
        List<MultiKeys> coordinates = in.getMultiKeysListNoAir();
        List<Integer> xs = new ArrayList<>();
        List<Integer> ys = new ArrayList<>();
        coordinates.forEach(s->{xs.add(s.getKey1());ys.add(s.getKey2());});
        Collections.sort(xs);
        Collections.sort(ys);
        int x = Math.abs(xs.get(0) - xs.get(xs.size()-1))+1;
        int y = Math.abs(ys.get(0) - ys.get(ys.size()-1))+1;
        return Math.max(x,y);
    }

    public RecipeMaterial getRecipeMaterial(Inventory inventory,int size){
        RecipeMaterial recipeMaterial = new RecipeMaterial();
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                int index = x+y*9;
                MultiKeys key = new MultiKeys(x,y);
                ItemStack item;
                if(inventory.getItem(index) == null){
                    item = new ItemStack(Material.AIR);
                }else{
                    item = inventory.getItem(index);
                }
                recipeMaterial.put(key,item);
            }
        }
        return recipeMaterial;
    }

    public boolean isSameShape(List<MultiKeys> models,List<MultiKeys> reals){
        int xGap = models.get(0).getKey1() - reals.get(0).getKey1();
        int yGap = models.get(0).getKey2() - reals.get(0).getKey2();
        int size = models.size();
        for(int i=0;i<size;i++){
            if(models.get(i).getKey1() - reals.get(i).getKey1() != xGap)return false;
            if(models.get(i).getKey2() - reals.get(i).getKey2() != yGap)return false;
        }
        return true;
    }

}

package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Recipe {
    int size;
    Map<Integer, List<ItemStack>> lines;
    int total;
    ItemStack result;
    public Recipe(int size,int total,Map<Integer,List<ItemStack>> lines,ItemStack result){
        this.size = size;
        this.total = total;
        this.lines = lines;
        this.result = result;
    }


    private int getTotalSize(Map<Integer,List<ItemStack>> in){
        int result = 0;
        for(Map.Entry<Integer,List<ItemStack>> entry : in.entrySet()){
            for(ItemStack i : entry.getValue()){
                if(!i.equals(null)){
                    result++;
                }
            }
        }
        return result;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<Integer, List<ItemStack>> getLines() {
        return lines;
    }

    public void setLines(Map<Integer, List<ItemStack>> lines) {
        this.lines = lines;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public List<RecipePlace> toRecipePlace(Recipe r){
        Map<Integer, List<ItemStack>> m = r.getLines();
        List<RecipePlace> rps = new ArrayList<>();
        for(Map.Entry<Integer,List<ItemStack>> e:m.entrySet()){
            int x = e.getKey();
            for(int i=0;i<r.getSize();i++){
                if(!e.getValue().get(i).equals(null)){
                    int y = i;
                    RecipePlace rp = new RecipePlace(x,y,e.getValue().get(i));
                    rps.add(rp);
                }
            }
        }
        return rps;
    }
}

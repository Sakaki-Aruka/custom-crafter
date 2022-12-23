package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class OriginalRecipe {
    private ItemStack result;
    private int size;
    private int total;
    private String recipeName;
    private RecipeMaterial rm;
    private Map<Material,Integer> amountRelation;
    public OriginalRecipe(ItemStack result,int size,int total,RecipeMaterial rm,String recipeName){
        this.result = result;
        this.size = size;
        this.rm = rm;
        this.total = total;
        this.recipeName = recipeName;
    }

    public ItemStack getResult(){
        return result;
    }

    public int getSize(){
        return size;
    }

    public int getTotal(){
        return total;
    }

    public RecipeMaterial getRecipeMaterial(){
        return rm;
    }

    private Map<Material,Integer> getAmountRelation(){
        RecipeMaterial rm = this.rm;
        Map<Material,Integer> map = new HashMap<>();
        for(Map.Entry<MultiKeys,ItemStack> entry:rm.getRecipeMaterial().entrySet()){
            try{
                Material material = entry.getValue().getType();
                if(map.containsKey(material)){
                    map.replace(material,map.get(material)+1);
                }else{
                    map.put(material,1);
                }
            }catch (Exception e){
                continue;
            }
        }
        return map;
    }



}

package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OriginalRecipe {
    private ItemStack result;
    private int size;
    private int total;
    private String recipeName;
    private RecipeMaterial rm;
    private Map<Material,Integer> amountRelation;

    public OriginalRecipe(int size,int total,RecipeMaterial rm){
        this.recipeName = "unknown";
        this.result = new ItemStack(Material.AIR);
        this.rm = rm;
        this.total = total;
        this.size = size;
    }

    public OriginalRecipe(ItemStack result,int size,int total,RecipeMaterial rm,String recipeName){
        this.result = result;
        this.size = size;
        this.total = total;
        this.rm = rm;
        this.recipeName = recipeName;
    }

    public OriginalRecipe(ItemStack result,RecipeMaterial rm){
        this.result = result;
        this.size = -1;
        this.total = -1;
        this.recipeName = null;
        this.rm = rm;
    }

    public String info(){
        return String.format("Name:%s | size:%d | recipeMaterial:%s | total:%d | result:%s",recipeName,size,rm.info(),total,result.toString());
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

    public String getRecipeName(){
        return recipeName;
    }

    public RecipeMaterial getRecipeMaterial(){
        return rm;
    }

    public List<Material> getRawMaterials(){
        List<Material> result = new ArrayList<>();
        for(Map.Entry<MultiKeys, ItemStack> entry:rm.getRecipeMaterial().entrySet()){
            if(!result.contains(entry.getValue().getType()))result.add(entry.getValue().getType());
        }
        return result;
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

    public void setRm(RecipeMaterial rm) {
        this.rm = rm;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setAmountRelation(Map<Material, Integer> amountRelation) {
        this.amountRelation = amountRelation;
    }
}

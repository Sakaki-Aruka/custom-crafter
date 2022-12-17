package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.Recipe;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SettingsLoad {
    private static CustomCrafter cc;
    private static FileConfiguration config;
    public static List<com.github.sakakiaruka.cutomcrafter.customcrafter.objects.Recipe> recipes = new ArrayList<>();
    public void set(){
        cc = CustomCrafter.getInstance();
        load();
    }

    private void load(){
        config = cc.getConfig();
        List<String> recipeList = config.getStringList("recipe-list");

        for(String s:recipeList){
            recipes.add(this.getRecipe(s));
        }
    }

    private Recipe getRecipe(String recipeName){
        String path = "recipe-items."+recipeName;
        int size = config.getInt(path+"size");
        List<String> lines = config.getStringList(path+".recipe");
        Map<Integer,List<ItemStack>> map = new HashMap<>();
        for(int i=0;i<size;i++){
            List<ItemStack> stacks = new ArrayList<>();
            for(String s: lines.get(i).split(",")){
                ItemStack item = this.getRecipeItem(s);
                stacks.add(item);
            }
            map.put(i,stacks);
        }

        int total = this.getTotal(map);
        ItemStack result = this.getResultItem(config.getString(path+".result"));

        com.github.sakakiaruka.cutomcrafter.customcrafter.objects.Recipe recipe = new com.github.sakakiaruka.cutomcrafter.customcrafter.objects.Recipe(size,total,map,result);
        return recipe;

    }

    private ItemStack getRecipeItem(String name){
        String path = "recipe-materials."+name;
        String material = config.getString(path+".material");
        if(material.equalsIgnoreCase("null")){
            return null;
        }
        ItemStack item = new ItemStack(Material.getMaterial(material));
        int amount = config.getInt(path+".amount");
        item.setAmount(amount);

        return item;
    }

    private ItemStack getResultItem(String name){
        String path = "result-items."+name;
        String material = config.getString(path+".material");
        ItemStack item = new ItemStack(Material.getMaterial(material));
        int amount = config.getInt(path+".amount");
        item.setAmount(amount);
        return item;
    }

    private int getTotal(Map<Integer,List<ItemStack>> input){
        int count = 0;
        for(Map.Entry<Integer,List<ItemStack>> entry:input.entrySet()){
            List<ItemStack> list = entry.getValue();
            for(ItemStack i:list){
                if(!i.equals(null)){
                    count++;
                }
            }
        }
        return count;
    }
}

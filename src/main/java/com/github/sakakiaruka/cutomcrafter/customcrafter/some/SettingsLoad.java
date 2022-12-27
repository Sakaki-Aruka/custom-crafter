package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;


public class SettingsLoad {
    private static CustomCrafter cc;
    private static FileConfiguration config;
    public static List<OriginalRecipe> recipes = new ArrayList<>();

    private Map<String,RecipeMaterial> recipeMaterialMap = new HashMap<>();
    public void set(){
        cc = CustomCrafter.getInstance();
        load();
    }

    private void load(){
        config = cc.getConfig();
        //List<String> resultsItemNames = config.getStringList("result-items");
        List<String> recipeNames = config.getStringList("recipe-list");
        //List<String> recipeMaterialNames = config.getStringList("recipe-materials-list");
        Map<String,ItemStack> results = getResult(recipeNames);
        originalRecipeMake(recipeNames,results);
    }

    private Map<String,ItemStack> getResult(List<String> list){
        String path = "result-item.";
        Map<String,ItemStack> map = new HashMap<>();
        for(String s:list){
            if(s.equalsIgnoreCase("null")){
                map.put(s,null);
                continue;
            }
            ItemStack item = new ItemStack(Material.getMaterial(config.getString(path+s+".material").toUpperCase()));
            item.setAmount(config.getInt(path+s+".amount"));
            ItemMeta meta = item.getItemMeta();

            try{
                meta.setDisplayName(config.getString(path+s+".name"));
                meta.setLore(config.getStringList(path+s+".lore"));
                item.addUnsafeEnchantments(getEnchantments(s));
                addItemFlags(s,meta);
                setUnbreakable(s,meta);
                item.setItemMeta(meta);
            }catch (Exception e){
                System.out.println("Config load error occurred.");
            }
            map.put(s,item);
        }
        return map;
    }

    private void originalRecipeMake(List<String> recipeNames,Map<String,ItemStack> map){
        for(String s:recipeNames){
            String recipePath = "recipes."+s;
            String recipeName = s;
            int size = config.getInt(recipePath+".size");
            int total = recipeMaterialMap.get(s).getMapSize();
            ItemStack result = map.get(config.getString(recipePath+".result"));
            getRecipeMaterial(recipeName,map,size);
            OriginalRecipe originalRecipe = new OriginalRecipe(result,size,total,recipeMaterialMap.get(recipeName),recipeName);
            recipes.add(originalRecipe);
        }
        return;
    }

    private void getRecipeMaterial(String recipeName,Map<String,ItemStack> recipeMaterials,int size){
        List<String> list = config.getStringList("recipes."+recipeName+".recipe");
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                MultiKeys key = new MultiKeys(i,j);
                ItemStack item = recipeMaterials.get(Arrays.asList(list.get(i).split(",")).get(j));
                RecipeMaterial material = new RecipeMaterial(key,item);
                recipeMaterialMap.put(recipeName,material);
            }
        }

    }

    @Deprecated
    private Map<Enchantment,Integer> getEnchantments(String resultItemName){
        String path = "result-item."+resultItemName+".enchants";
        List<String> list = config.getStringList(path);
        Map<Enchantment,Integer> map = new HashMap<>();
        for(String s:list){
            List<String> raw = Arrays.asList(s.split(" "));
            int level = Integer.valueOf(raw.get(1));
            Enchantment enchant = Enchantment.getByName(raw.get(0));
            map.put(enchant,level);
        }
        return map;
    }

    private void addItemFlags(String resultItemName,ItemMeta meta){
        String path = "result-item."+resultItemName+".flags";
        List<String> list = config.getStringList(path);
        for(String s:list){
            meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase()));
        }
    }

    private void setUnbreakable(String resultItemName,ItemMeta meta){
        String path = "result-item."+resultItemName+".break";
        boolean bool = config.getBoolean(path);
        meta.setUnbreakable(bool);
    }
}

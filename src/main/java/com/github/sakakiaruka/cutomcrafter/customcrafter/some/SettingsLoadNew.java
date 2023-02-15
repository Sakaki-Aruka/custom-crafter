package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter.getInstance;

public class SettingsLoadNew {
    private static CustomCrafter cc;
    private static FileConfiguration defaultConfig;
    public static List<OriginalRecipe> recipes = new ArrayList<>();
    public static Material baseBlock;
    public static Map<String,List<Material>> mixedCategories = new HashMap<>();

    public static Map<Material,List<OriginalRecipe>> recipesMaterial = new HashMap<>();
    public static Map<Integer,List<OriginalRecipe>> recipesAmount = new HashMap<>();

    private Map<String, ItemStack> recipeResults = new HashMap<>();
    private Map<String,ItemStack> recipeMaterials = new HashMap<>();
    private Map<String,OriginalRecipe> nameAndOriginalRecipe = new HashMap<>();
    private Plugin plugin = getInstance();

    public void set(){

        cc = getInstance();
        defaultConfig = cc.getConfig();

        Path baseBlockPath = Paths.get(defaultConfig.getString("baseBlock"));
        Path resultItemsPath = Paths.get(defaultConfig.getString("resultItems"));
        Path materialCategoryPath = Paths.get(defaultConfig.getString("materialCategory"));
        Path recipeMaterialPath = Paths.get(defaultConfig.getString("recipeMaterials"));
        Path recipesPath = Paths.get(defaultConfig.getString("recipes"));

        getBaseBlock(getFiles(baseBlockPath));
        getResultItems(getFiles(resultItemsPath));
        getMaterialCategory(getFiles(materialCategoryPath));
        getRecipeMaterials(getFiles(recipeMaterialPath));
        getOriginalRecipes(getFiles(recipesPath));
        originalRecipeSort();
    }

    private List<Path> getFiles(Path path){
        Stream<Path> paths;
        try{
            paths = Files.list(path);
        }catch (Exception e){
            System.out.println("Custom-Crafter Error: Cannot get files from "+path);
            return null;
        }

        List<Path> result = new ArrayList<>();
        paths.forEach(s->result.add(s));
        return result;
    }

    private void getBaseBlock(List<Path> paths){
        FileConfiguration config = YamlConfiguration.loadConfiguration(paths.get(0).toFile());
        String materialName = config.getString("material").toUpperCase();
        baseBlock = Material.valueOf(materialName);
    }

    @Deprecated
    private void getResultItems(List<Path> paths){
        for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            ItemStack item = new ItemStack(Material.valueOf(config.getString("material").toUpperCase()));
            item.setAmount(config.getInt("amount"));
            ItemMeta meta = item.getItemMeta();
            String name = config.getString("name");

            if(config.contains("lore")){
                meta.setLore(config.getStringList("lore"));
            }

            if(config.contains("enchants")){
                List<String> raw = config.getStringList("enchants");
                for(String s:raw){
                    List<String> jumble = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(jumble.get(0).toUpperCase());
                    int level = Integer.valueOf(jumble.get(1));
                    item.addUnsafeEnchantment(enchant,level);
                }
            }

            if(config.contains("flags")){
                List<String> flags = config.getStringList("flags");
                for (String s:flags){
                    meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase()));
                }
            }

            if(config.contains("display-name")){
                meta.setDisplayName(config.getString("display-name"));
            }

            item.setItemMeta(meta);
            recipeResults.put(name,item);
        }
    }

    private void getMaterialCategory(List<Path> paths){
        for (Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            String name = config.getString("name");
            List<Material> value = new ArrayList<>();
            List<String> materials = config.getStringList("contents");
            materials.forEach(s->value.add(Material.getMaterial(s.toUpperCase())));
            mixedCategories.put(name,value);
        }
    }

    private void getRecipeMaterials(List<Path> paths){
        for (Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            String name = config.getString("name");
            try{
                ItemStack item = new ItemStack(Material.valueOf(config.getString("material").toUpperCase()));
                item.setAmount(config.getInt("amount"));
                recipeMaterials.put(name,item);
            }catch (Exception e){
                String category = config.getString("mixed-material");
                Material material = mixedCategories.get(category).get(0);

                MixedMaterial item = new MixedMaterial(category,material,1);
                item.setAmount(config.getInt("amount"));
                recipeMaterials.put(name,item);
            }
        }
    }

    private void getOriginalRecipes(List<Path> paths){
        for (Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            String name = config.getString("name");
            int size = config.getInt("size");
            ItemStack result = recipeResults.get(config.getString("result"));

            List<String> itemArr = config.getStringList("recipe");
            RecipeMaterial rm = new RecipeMaterial();
            for(int y=0;y<size;y++){
                List<String> list = Arrays.asList(itemArr.get(y).split(","));
                for (int x=0;x<size;x++){
                    MultiKeys key = new MultiKeys(x,y);
                    ItemStack material;
                    if(list.get(x).equalsIgnoreCase("null")){
                        material = new ItemStack(Material.AIR);
                    }else if(recipeMaterials.containsKey(list.get(x))){
                        material = recipeMaterials.get(list.get(x));
                    }else{
                        Bukkit.getLogger().info("Custom-Crafter config load error.");
                        return;
                    }
                    rm.put(key,material);
                }
            }
            int total = 0;
            for(Map.Entry<MultiKeys,ItemStack> entry:rm.getRecipeMaterial().entrySet()){
                if(entry.getValue().getType().equals(Material.AIR))continue;
                total+=entry.getValue().getAmount();
            }
            OriginalRecipe originalRecipe = new OriginalRecipe(result,size,total,rm,name);
            recipes.add(originalRecipe);
            nameAndOriginalRecipe.put(originalRecipe.getRecipeName(),originalRecipe);
        }
    }

    private void originalRecipeSort(){
        for(OriginalRecipe original : recipes){
            RecipeMaterial rm = original.getRecipeMaterial();
            int top_amount = rm.getLargestAmount();
            Material top_material = rm.getLargestMaterial();

            // --- about material --- //
            if(recipesMaterial.containsKey(top_material)){
                List<OriginalRecipe> originals = recipesMaterial.get(top_material);
                originals.add(original);
                recipesMaterial.put(top_material,originals);
            }else{
                recipesMaterial.put(top_material,new ArrayList<>(Arrays.asList(original)));
            }

            // --- about material end --- //

            // --- about amount --- //
            if(recipesAmount.containsKey(top_amount)){
                List<OriginalRecipe> originals = recipesAmount.get(top_amount);
                originals.add(original);
                recipesAmount.put(top_amount,originals);
            }else{
                recipesAmount.put(top_amount,new ArrayList<>(Arrays.asList(original)));
            }
            // --- about amount end --- //
        }
    }

    private void getFilesFromTheSea(){
        if(!defaultConfig.contains("wget"))return;
        if(defaultConfig.getStringList("wget").isEmpty())return;

        List<String> failed = new ArrayList<>();
        for(String s: defaultConfig.getStringList("wget")){
            List<String> list = new ArrayList<>(Arrays.asList(s.split(",")));
            if(list.size()==1){
                System.out.println("====");
                System.out.println("Custom-Crafter Config File Loader (over the Internet) has an error occurred.");
                System.out.println("Code : "+s);
                System.out.println("cause: Invalid details .(Details limit is only 1.) You wrote "+list.size());
                System.out.println("====");
                continue;
            }
            String command = list.get(0);
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process;
            try{
                process = builder.start();
                process.waitFor();
            }catch (Exception e){
                System.out.println("Custom-Crafter Config File Loader (over the Internet) has an error occurred.");
                System.out.println("Exception occurred when the process start.");
                System.out.println("Process : "+command);
                failed.add(s);
            }
        }
        defaultConfig.set("wget",failed);
    }

}

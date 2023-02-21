package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter.getInstance;

public class SettingsLoad {
    private static FileConfiguration defaultConfig;
    public static List<OriginalRecipe> recipes = new ArrayList<>();
    public static Material baseBlock;
    public static Map<String,List<Material>> mixedCategories = new HashMap<>();

    //=== for sort process ===//
    public static Map<Material,List<OriginalRecipe>> recipesMaterial = new HashMap<>();
    public static Map<Integer,List<OriginalRecipe>> recipesAmount = new HashMap<>();

    //=== for data load process ===//
    private Map<String, ItemStack> recipeResults = new HashMap<>();
    private Map<String,ItemStack> recipeMaterials = new HashMap<>();
    private Map<String,OriginalRecipe> nameAndOriginalRecipe = new HashMap<>();
    private Map<String, EnchantedMaterial> enchantedMaterials = new HashMap<>();

    //=== for runnable task ===//
    private List<String> downloadUri;
    private List<String> failed = new ArrayList<>();
    private int returnCode = -1;
    private int times = 0;
    private int threshold;
    private int load_interval;

    public void set(){

        defaultConfig = getInstance().getConfig();

        // Get element paths from the default config file
        Path baseBlockPath = Paths.get(defaultConfig.getString("baseBlock"));
        Path resultItemsPath = Paths.get(defaultConfig.getString("resultItems"));
        Path materialCategoryPath = Paths.get(defaultConfig.getString("materialCategory"));
        Path recipeMaterialPath = Paths.get(defaultConfig.getString("recipeMaterials"));
        Path recipesPath = Paths.get(defaultConfig.getString("recipes"));

        // Directory check
        configFileDirectoryCheck(baseBlockPath);
        configFileDirectoryCheck(resultItemsPath);
        configFileDirectoryCheck(materialCategoryPath);
        configFileDirectoryCheck(recipeMaterialPath);
        configFileDirectoryCheck(recipeMaterialPath);
        configFileDirectoryCheck(recipesPath);

        BukkitRunnable downloader = new BukkitRunnable() {
            @Override
            public void run() {
                //runnable
                for(String command: downloadUri){
                    if(command.isEmpty())return;
                    ProcessBuilder builder = new ProcessBuilder(Arrays.asList(command.split(" ")));
                    Process process;
                    try{
                        process = builder.start();
                        process.waitFor();
                    }catch (Exception e){
                        System.out.println("===");
                        System.out.println("Custom-Crafter Config File Loader (downloader) error.");
                        System.out.println("An Exception occurred when start the process.");
                        System.out.println("Process : "+command);
                        System.out.println("Current Directory:"+Paths.get("").toUri());
                        e.printStackTrace();
                        System.out.println("===");
                        failed.add(command);
                    }
                }
                returnCode = 0;
            }
        };

        //getFilesFromTheSea();
        if(defaultConfig.contains("download")){
            if(!defaultConfig.getStringList("download").isEmpty()){
                downloadUri = defaultConfig.getStringList("download");
                threshold = defaultConfig.getInt("download_threshold");
                load_interval = defaultConfig.getInt("load_interval");
                downloader.runTaskAsynchronously(getInstance());
                defaultConfig.set("download",failed);
                getInstance().saveConfig();
            }
        }

        BukkitRunnable main = new BukkitRunnable() {
            @Override
            public void run() {
                // Get data from each files
                getBaseBlock(getFiles(baseBlockPath));
                getResultItems(getFiles(resultItemsPath));
                getMaterialCategory(getFiles(materialCategoryPath));
                getRecipeMaterials(getFiles(recipeMaterialPath));
                getOriginalRecipes(getFiles(recipesPath));
                originalRecipeSort();

                System.out.println("===\nCustom-Crafter data loaded.\n===");
            }
        };

        new BukkitRunnable(){
            @Override
            public void run(){
                if(times<=threshold && returnCode==0){
                    main.runTaskLater(getInstance(),20);
                    this.cancel();
                    System.out.println("[CustomCrafter]Config Download complete!");
                    return;
                }else if(times>threshold){
                    System.out.println("[CustomCrafter]Could not load data.");
                    this.cancel();
                    return;
                }
                System.out.println(String.format("[CustomCrafter]Downloading now... %d/%d",times,threshold));
                times++;
            }
        }.runTaskTimer(getInstance(),20,load_interval);


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
                if(config.contains("mixed-material")){
                    String category = config.getString("mixed-material");
                    Material material = mixedCategories.get(category).get(0);

                    MixedMaterial item = new MixedMaterial(category,material,1);
                    item.setAmount(config.getInt("amount"));
                    recipeMaterials.put(name,item);
                }else if(config.contains("enchants")){
                    //enchanted material
                    getEnchantedMaterials(path);
                }

            }
        }
    }

    @Deprecated
    private void getEnchantedMaterials(Path path){
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        String name = config.getString("name");
        EnchantedMaterial item = new EnchantedMaterial();
        item.setType(Material.valueOf(config.getString("material").toUpperCase()));
        item.setAmount(config.getInt("amount"));

        List<String> list = config.getStringList("enchants");
        for (String s:list){
            List<String> data = Arrays.asList(s.split(","));
            if(data.size()!=3)return;
            Enchantment enchant = Enchantment.getByName(data.get(0).toUpperCase());
            int level = Integer.valueOf(data.get(1));
            EnchantedMaterialEnum enumType = EnchantedMaterialEnum.valueOf(data.get(2).toUpperCase());

            IntegratedEnchant integrated = new IntegratedEnchant(enchant,level);
            item.put(integrated,enumType);
        }
        recipeMaterials.put(name,item);
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




    private void configFileDirectoryCheck(Path path){
        if(path.toFile().exists() && path.toFile().isDirectory())return;
        if(!path.toFile().exists()){
            //not exist
            File dir = new File(path.toUri());
            dir.mkdir();
            System.out.println(String.format("Not found the directory->\"%s\".%nSo, the system made the directory named that.",path.toUri().toString()));
        }else if(!path.toFile().isDirectory()){
            System.out.println(String.format("The path ->\"%s\" is not directory.",path.toUri().toString()));
            System.out.println("Fix this problem before to use this plugin.");
        }
    }

}

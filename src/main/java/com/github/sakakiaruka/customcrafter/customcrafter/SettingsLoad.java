package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;

public class SettingsLoad {

    // === defined settings values === //
    public static final int craftingTableSize = 6;
    public static final int craftingTableResultSlot = 44;
    public static final int craftingTableMakeButton = 35;
    public static final int craftingTableTotalSize = 54;
    public static final int vanillaCraftingSlots = 9;
    public static final int vanillaCraftingSquareSize = 3;

    // === recipes public values === //
    public static Material baseBlock;
    public static List<Recipe> recipes = new ArrayList<>();
    public static List<String> allMaterials = new ArrayList<>();
    public static Map<String,Recipe> namedRecipes = new HashMap<>();

    // === for crafting data manage === //
    public static Map<UUID,Material> whatMaking = new HashMap<>();

    // === for data get methods === //
    private static FileConfiguration defaultConfig;
    private static Map<String,Result> results = new HashMap<>();
    private static Map<String, Matter> matters = new HashMap<>();

    // === for runnable task === //
    private List<String> downloadUri;
    private List<String> failed = new ArrayList<>();

    private int returnCode = -1;
    private int times = 0;
    private int threshold;
    private int load_interval;


    public void load(){
        defaultConfig = getInstance().getConfig();
        new OpenCraftingTable().setCraftingInventory();
        getAllMaterialsName();
        main();
    }

    private void getAllMaterialsName(){
        Arrays.stream(Material.values()).forEach(s->allMaterials.add(s.name()));
    }

    private void main(){
        Path baseBlockPath = Paths.get(defaultConfig.getString("baseBlock"));
        Path resultPath = Paths.get(defaultConfig.getString("results"));
        Path matterPath = Paths.get(defaultConfig.getString("matters"));
        Path recipePath = Paths.get(defaultConfig.getString("recipes"));

        configFileDirectoryCheck(baseBlockPath);
        configFileDirectoryCheck(resultPath);
        configFileDirectoryCheck(matterPath);
        configFileDirectoryCheck(recipePath);


        // --- bukkit runnable --- //
        BukkitRunnable downloader = new BukkitRunnable() {
            @Override
            public void run() {
                for(String command:downloadUri){
                    if(command.isEmpty())return;
                    ProcessBuilder builder = new ProcessBuilder(Arrays.asList(command.split(" ")));
                    Process process;
                    try{
                        process = builder.start();
                        process.waitFor();
                    }catch (Exception e){
                        Logger logger = Bukkit.getLogger();
                        logger.info("[CustomCrafter] downloading error.");
                        logger.info("command : "+command);
                        failed.add(command);
                    }
                }
                returnCode = 0;
            }
        };

        BukkitRunnable main = new BukkitRunnable() {
            @Override
            public void run() {
                // get data from each files
                getBaseBlock(getFiles(baseBlockPath));
                getResult(getFiles(resultPath));
                getMatter(getFiles(matterPath));
                getRecipe(getFiles(recipePath));

                Bukkit.getLogger().info("===Custom-Crafter data loaded.===");
            }
        };

        // === bukkit runnable finish ===//

        //getFilesFromTheSea (download section)
        if(defaultConfig.contains("download")){
            if(defaultConfig.getStringList("download").isEmpty()){
                // no download files.
                main.runTaskLater(getInstance(),20l);
                return;
            }
            downloadUri = defaultConfig.getStringList("download");
            List<String> downloadErrorMessageList = defaultConfig.getStringList("errorMessages");
            threshold = defaultConfig.getInt("download_threshold");
            load_interval = defaultConfig.getInt("download_interval");

            downloader.runTaskAsynchronously(getInstance());
            defaultConfig.set("download",failed);
            getInstance().saveConfig();
        }

        new BukkitRunnable(){
            @Override
            public void run(){
                if(times <= threshold && returnCode==0){
                    //finish
                    main.runTaskLater(getInstance(),20); // 1second delay
                    this.cancel();
                    Bukkit.getLogger().info("[CustomCrafter] Config Download complete!");
                    return;
                }else if(times > threshold){
                    Bukkit.getLogger().info("[CustomCrafter] Could not load data.");
                    this.cancel();
                    return;
                }
                Bukkit.getLogger().info("[CustomCrafter] Downloading now...");
                times++;
            }
        }.runTaskTimer(getInstance(),20,load_interval);
    }

    private void configFileDirectoryCheck(Path path){
        if(path.toFile().exists() && path.toFile().isDirectory())return;
        if(!path.toFile().exists()){
            // not exist
            File dir = new File(path.toUri());
            dir.mkdir();
            System.out.println(String.format("Not found the directory \"%s\".\nSo, the system made the directory named that.",path.toUri().toString()));
        }else if(!path.toFile().isDirectory()){
            System.out.println(String.format("The path \"%s\" is not a directory.",path.toUri().toString()));
            System.out.println("You must fix this problem when before you use this plugin.");

            new BukkitRunnable(){
                public void run(){
                    Bukkit.getPluginManager().disablePlugin(getInstance());
                }
            }.runTaskLater(getInstance(),30 * 20l);
        }
    }

    private List<Path> getFiles(Path path){
        Stream<Path> paths;
        try{
            paths = Files.list(path);
        }catch (Exception e){
            System.out.println("[CustomCrafter] Error: Cannot get files from "+path);
            return null;
        }

        List<Path> list = new ArrayList<>();
        paths.forEach(s->list.add(s));
        return list;
    }

    private void getBaseBlock(List<Path> paths){
        FileConfiguration config = YamlConfiguration.loadConfiguration(paths.get(0).toFile());
        String name = config.getString("material").toUpperCase();
        baseBlock = Material.valueOf(name);
    }

    private void getResult(List<Path> paths){
        for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            String name = config.getString("name");
            int amount = config.getInt("amount");
            String nameOrRegex = config.getString("nameOrRegex");
            int matchPoint = config.getInt("matchPoint"); // default value is -1;
            Map<Enchantment,Integer> enchantInfo = null;
            Map<String,List<String>> metadata = null;
            if(config.contains("enchant")){
                enchantInfo = new HashMap<>();
                for(String s:config.getStringList("enchant")){
                    /*
                    * 0,1
                    * 0 : enchantment name (String | lower case OK)
                    * 1 : enchantment level (int)
                     */
                    List<String> list = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(list.get(0).toUpperCase());
                    int level = Integer.valueOf(list.get(1));
                    enchantInfo.put(enchant,level);
                }
            }

            if(config.contains("metadata")){
                metadata = new HashMap<>();
                for(String s:config.getStringList("metadata")){
                    /*
                    * 0,1
                    * 0 : key (String | lore, displayName, enchantment (deprecated), itemFlag, customModelData
                    * 1 : value (Object | List<String>, String, Enchantment & int, boolean, int
                     */
                    List<String> list = Arrays.asList(s.split(","));
                    String key = list.get(0);
                    String value = String.join(",",list.subList(1,list.size()));
                    if(!metadata.containsKey(key))metadata.put(key,new ArrayList<>());
                    metadata.get(key).add(value);
                }
            }

            Result result = new Result(name,enchantInfo,amount,metadata,nameOrRegex,matchPoint);
            results.put(name,result);
        }
    }

    private void getMatter(List<Path> paths){
        for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            String name = config.getString("name");
            int amount = config.getInt("amount");
            boolean mass = config.getBoolean("mass");
            List<Material> candidate = new ArrayList<>();
            for(String s:config.getStringList("candidate")){
                if(s.startsWith("R|")){
                    String pattern = s.substring(2); // remove "R|"

                    //debug
                    System.out.println(String.format("string : %s | pattern : %s",s,pattern));

                    candidate.addAll(getCandidateFromRegex(pattern));
                }else{
                    candidate.add(Material.getMaterial(s.toUpperCase()));
                }

            }
            List<EnchantWrap> wrapList = new ArrayList<>();
            if(config.contains("enchant")){
                for(String s:config.getStringList("enchant")){
                    List<String> list = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(list.get(0));
                    int level = Integer.valueOf(list.get(1));
                    EnchantStrict strict = EnchantStrict.valueOf(list.get(2).toUpperCase());
                    EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
                    wrapList.add(wrap);
                }
            }

            //debug
            System.out.println("matter name : "+name);
            for(Material material:candidate){
                System.out.println(String.format("candidate : %s",material.name()));
            }

            if(wrapList.isEmpty())wrapList = null;

            Matter matter = new Matter(name,candidate,wrapList,amount,mass);
            matters.put(name,matter);
        }
    }

    private List<Material> getCandidateFromRegex(String regexPattern){
        List<Material> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(regexPattern);
        for(Material m:Material.values()){
            String material = m.name();
            Matcher matcher = pattern.matcher(material);
            if(!matcher.find())continue;
            Material matched = Material.valueOf(matcher.group(0).toUpperCase());
            list.add(matched);

            //debug
            System.out.println(String.format("pattern : %s | matcher(0) : %s | material : %s",pattern,matcher.group(0),material));
        }
        return list;
    }

    private void getRecipe(List<Path> paths){
        for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            String name = config.getString("name");
            String tag = config.getString("tag").toUpperCase();
            Result result = results.get(config.getString("result"));

            Map<Coordinate,Matter> coordinates = new LinkedHashMap<>();
            Map<Material, ItemStack> returns = new HashMap<>();

            if(tag.equalsIgnoreCase("normal")){
                // normal recipe load
                int size = config.getStringList("coordinate").size();
                List<String> l = config.getStringList("coordinate");
                for(int y=0;y<size;y++){
                    List<String> list = Arrays.asList(l.get(y).split(","));
                    for(int x=0;x<size;x++){
                        Coordinate coordinate = new Coordinate(x,y);
                        Matter matter = list.get(x).equalsIgnoreCase("null") ? new Matter(Arrays.asList(Material.AIR),0) : matters.get(list.get(x));
                        coordinates.put(coordinate,matter);
                    }
                }
            }else if(tag.equalsIgnoreCase("amorphous")){
                // amorphous (shapeless) recipe load
                List<String> l = config.getStringList("coordinate");
                int x = -1;
                int count = 0;
                for(int i=0;i<l.size();i++){
                    List<String> list = Arrays.asList(l.get(i).split(","));
                    for(int j=0;j<list.size();j++){
                        Coordinate coordinate = new Coordinate(x,count);
                        Matter matter = list.get(j).equalsIgnoreCase("null") ? new Matter(Arrays.asList(Material.AIR),0) : matters.get(list.get(j));
                        coordinates.put(coordinate,matter);
                        count++;
                    }
                }
                //
            }


            if(config.contains("returns")){
                /* 0,1,2
                * 0 : Material Name (String)
                * 1 : return Material Name (String)
                * 2 : return item amount (int)
                 */
                for(String s:config.getStringList("returns")){
                    List<String> list = Arrays.asList(s.split(","));
                    Material material = Material.valueOf(list.get(0).toUpperCase());
                    Material returnMaterial = Material.valueOf(list.get(1).toUpperCase());
                    int amount = Integer.valueOf(list.get(2));
                    ItemStack itemStack  = new ItemStack(returnMaterial,amount);
                    returns.put(material,itemStack);
                }
            }

            Recipe recipe = new Recipe(name,tag,coordinates,returns,result);
            recipes.add(recipe);
            namedRecipes.put(name,recipe);
        }
    }
}

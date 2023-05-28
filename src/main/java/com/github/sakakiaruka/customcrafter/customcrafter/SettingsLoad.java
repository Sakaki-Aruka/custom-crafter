package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionBottleType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.MetadataType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import com.github.sakakiaruka.customcrafter.customcrafter.util.DataCheckerUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.playerPermissions;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.recipePermissionMap;

public class SettingsLoad {

    // === defined settings values === //
    public static final int craftingTableSize = 6;
    public static final int craftingTableResultSlot = 44;
    public static final int craftingTableMakeButton = 35;
    public static final int craftingTableTotalSize = 54;
    public static final int vanillaCraftingSlots = 9;
    public static final int vanillaCraftingSquareSize = 3;
    public static final String nl = System.getProperty("line.separator");
    public static final String bar = String.join("",Collections.nCopies(40,"="));
    public static final String shortBar = String.join("",Collections.nCopies(20,"="));
    public static final String upperArrow = String.format("↑");

    // === recipes public values === //
    public static Material baseBlock;
    public static List<Recipe> recipes = new ArrayList<>();
    public static List<String> allMaterials = new ArrayList<>();
    public static Map<String,Recipe> namedRecipes = new HashMap<>();

    // === for crafting data manage === //
    public static Map<UUID,Material> whatMaking = Collections.synchronizedMap(new HashMap<>());

    // === for data get methods === //
    private static FileConfiguration defaultConfig;
    public static Map<String,Result> results = new HashMap<>();
    public static Map<String, Matter> matters = new HashMap<>();

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
        recipePermissionLoad();
        getAllMaterialsName();
        main();
    }

    private void recipePermissionLoad(){

        if(defaultConfig.contains("permissions") && defaultConfig.contains("relate")){
            // The file that defines RecipePermissions.
            Path path = Paths.get(defaultConfig.getString("permissions"));
            new RecipePermissionUtil().permissionSettingsLoad(path);

            // The file that defines the relate between players and RecipePermissions.
            Path relate = Paths.get(defaultConfig.getString("relate"));
            new RecipePermissionUtil().permissionRelateLoad(relate);

            // Resolve permission duplications.
            synchronized (playerPermissions) {
                Iterator<Map.Entry<UUID,List<RecipePermission>>> iterator = playerPermissions.entrySet().iterator();
                while (iterator.hasNext()) {
                    new RecipePermissionUtil().removePermissionDuplications(iterator.next().getValue());
                }
            }
        }
    }

    private void getAllMaterialsName(){
        Arrays.stream(Material.values()).forEach(s->allMaterials.add(s.name()));
    }

    private void main(){
        results.clear();
        matters.clear();
        failed.clear();

        Path baseBlockPath = Paths.get(defaultConfig.getString("baseBlock"));

        List<Path> resultPaths = new ArrayList<>();
        List<Path> matterPaths = new ArrayList<>();
        List<Path> recipePaths = new ArrayList<>();
        defaultConfig.getStringList("results").forEach(s->resultPaths.add(Paths.get(s)));
        defaultConfig.getStringList("matters").forEach(s->matterPaths.add(Paths.get(s)));
        defaultConfig.getStringList("recipes").forEach(s->recipePaths.add(Paths.get(s)));

        configFileDirectoryCheck(baseBlockPath);
        resultPaths.forEach(p->configFileDirectoryCheck(p));
        matterPaths.forEach(p->configFileDirectoryCheck(p));
        recipePaths.forEach(p->configFileDirectoryCheck(p));


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
                resultPaths.forEach(p->getResult(getFiles(p)));
                matterPaths.forEach(p->getMatter(getFiles(p)));
                recipePaths.forEach(p->getRecipe(getFiles(p)));

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
                    main.runTaskLater(getInstance(),20); // 1 second delay
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
            System.out.println(String.format("Not found the directory \"%s\"."+nl+"So, the system made the directory named that.",path.toUri().toString()));
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
        baseBlock = Material.valueOf(config.getString("material").toUpperCase());
    }

    private void getResult(List<Path> paths){
        addAllVanillaMaterial();
        for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

            //debug
            new DataCheckerUtil().resultCheck(new StringBuilder(),config,path);

            String name = config.getString("name");
            int amount = config.getInt("amount");
            String nameOrRegex = config.getString("nameOrRegex");
            int matchPoint = config.getInt("matchPoint"); // default value is -1;
            Map<Enchantment,Integer> enchantInfo = null;
            Map<MetadataType,List<String>> metadata = null;
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
                    * 0 : key (String | lore, displayName, enchantment (deprecated), itemFlag, customModelData, potionData, potionColor
                    * 1 : value (Object | List<String>, String, Enchantment & int, boolean, int
                     */
                    List<String> list = Arrays.asList(s.split(","));
                    MetadataType key = MetadataType.valueOf(list.get(0).toUpperCase());
                    String value = String.join(",",list.subList(1,list.size()));
                    if(!metadata.containsKey(key))metadata.put(key,new ArrayList<>());
                    metadata.get(key).add(value);
                }
            }

            Result result = new Result(name,enchantInfo,amount,metadata,nameOrRegex,matchPoint);
            results.put(name,result);
        }
    }

    private void addAllVanillaMaterial(){
        for(Material material : Material.values()){
            String name = material.name().toLowerCase();
            Result result = new Result(name,null,1,null,material.name(),-1);
            results.put(name,result);
        }
    }

    public void getMatter(List<Path> paths){
        // add Null (for Override)
        addNull();
        addWaterBottle();

        Top : for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

            //debug
            new DataCheckerUtil().matterCheck(new StringBuilder(),config,path);

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
                }else if (s.equalsIgnoreCase("enchanted_book")){
                    // enchanted book
                    if(!candidate.isEmpty()){
                        //invalid pattern (candidate over only one)
                        Bukkit.getLogger().info("[Custom Crafter] Matter load fail. - Candidate Quarrel -");
                        continue Top;
                    }

                    if(!config.contains("enchant") || config.getStringList("enchant").isEmpty()){
                        // invalid pattern (enchantments not contained -> identity lost)
                        Bukkit.getLogger().info("[Custom Crafter] Matter load fail. - Identity Lost -");
                        continue Top;
                    }

                    candidate.add(Material.ENCHANTED_BOOK);
                    break;

                }else{
                    candidate.add(Material.getMaterial(s.toUpperCase()));
                }

            }
            List<EnchantWrap> wrapList = new ArrayList<>();
            if(config.contains("enchant")){
                for(String s:config.getStringList("enchant")){
                    /*
                    * 0,1,2
                    * 0 : enchant name (String)
                    * 1 : enchant level (int)
                    * 2 : enchant strict (String -> input, notStrict, onlyEnchant, strict)
                     */
                    List<String> list = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(list.get(0).toUpperCase());
                    int level = Integer.valueOf(list.get(1));
                    EnchantStrict strict = EnchantStrict.valueOf(list.get(2).toUpperCase());
                    EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
                    wrapList.add(wrap);

                    //debug
                    System.out.println(String.format("load wrap : %s",wrap.info()));
                    System.out.println(String.format("list : %s",list));
                }
            }

            //debug
            System.out.println("matter name : "+name);
            for(Material material:candidate){
                System.out.println(String.format("candidate : %s",material.name()));
            }

            if(wrapList.isEmpty())wrapList = null;

            Matter matter = new Matter(name,candidate,wrapList,amount,mass);

            //PotionData collect
            if(config.contains("potion") && config.contains("bottleTypeMatch")){
                Potions potions = makeDrug(matter,config);
                if(potions != null) matters.put(name,potions);

                //debug
                System.out.println("SettingsLoad Potion : "+nl+potions.PotionInfo());

                continue;
            }

            matters.put(name,matter);
        }
    }

    private void addNull(){
        Matter matter = new Matter(Arrays.asList(Material.AIR),0);
        matters.put("null",matter);
        //matters.put("NULL",matter);
    }

    private void addWaterBottle(){
        Potions waterBottle = new PotionUtil().water_bottle();
        matters.put("water_bottle",waterBottle);
    }

    private Potions makeDrug(Matter matter, FileConfiguration config){
        // - PotionEffectType, duration, amplifier, strict
        Map<PotionEffect, PotionStrict> map = new HashMap<>();
        for(String str : config.getStringList("potion")){
            List<String> list = Arrays.asList(str.split(","));
            if(list.size() != 4) {
                System.out.println("[Custom Crafter] Potion Configuration Parameter are not enough.");
                return null;
            }
            PotionEffectType effectType = PotionEffectType.getByName(list.get(0).toUpperCase());
            int duration = Integer.valueOf(list.get(1));
            int amplifier = Integer.valueOf(list.get(2));//-1 < 0 ? 0 : Integer.valueOf(list.get(2)) -1;
            PotionEffect effect = new PotionEffect(effectType,duration,amplifier);
            PotionStrict strict = PotionStrict.valueOf(list.get(3).toUpperCase());
            map.put(effect,strict);
        }

        PotionBottleType bottleType = new PotionUtil().getBottleType(matter.getCandidate().get(0));
        boolean bottleTypeMatch = config.getBoolean("bottleTypeMatch");

        Potions potions = new Potions(matter,map,bottleType,bottleTypeMatch);
        return potions;

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

    private boolean containsAllMaterialsIgnoreCase(String in){
        for(String str : allMaterials){
            if(in.equalsIgnoreCase(str)) return true;
        }
        return false;
    }

    private void getRecipe(List<Path> paths){
        for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

            new DataCheckerUtil().recipeCheck(new StringBuilder(),config,path);

            String name = config.getString("name");
            String tag = config.getString("tag").toUpperCase();
            Result result = results.get(config.getString("result"));

            Map<Coordinate,Matter> coordinates = new LinkedHashMap<>();
            Map<Material, ItemStack> returns = new HashMap<>();
            Map<String,String> overrides = new HashMap<>();
            RecipePermission permission = null;

            if(config.contains("override")){
                /* override:
                *  - cobblestone -> c
                *  - stone -> s
                */
                for(String string : config.getStringList("override")){
                    List<String> splitter = Arrays.asList(string.split(" -> "));
                    String source = containsAllMaterialsIgnoreCase(splitter.get(0)) ? splitter.get(0).toUpperCase() : splitter.get(0);
                    String shorter = splitter.get(1);
                    overrides.put(shorter,source);
                }
            }

            //debug
            System.out.println("overrides : "+overrides);

            if(tag.equalsIgnoreCase("normal")){
                // normal recipe load
                int size = config.getStringList("coordinate").size();
                List<String> l = config.getStringList("coordinate");
                for(int y=0;y<size;y++){
                    List<String> list = Arrays.asList(l.get(y).split(","));
                    for(int x=0;x<size;x++){
                        Coordinate coordinate = new Coordinate(x,y);

                        /*
                        * collect matter example
                        * "cobblestone" -> this is a normal ItemID and the parameter 'mass' is not true.
                        * "m-cobblestone" -> this is a normal ItemID and the parameter 'mass' is true.
                        * "modified_cobblestone" -> this is not a normal ItemID. Have to search from 'matters'.
                         */

                        String matterName = list.get(x);
                        Matter matter = getMatterFromString(matterName,overrides);

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
                        String matterName = list.get(j);
                        Matter matter = getMatterFromString(matterName,overrides);

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
                    ItemStack itemStack;
                    Material material = Material.valueOf(list.get(0).toUpperCase());
                    if(list.get(1).equals("water_bottle")){
                        itemStack = new PotionUtil().water_bottle_ItemStack();
                    }else{
                        Material returnMaterial = Material.valueOf(list.get(1).toUpperCase());
                        int amount = Integer.valueOf(list.get(2));
                        itemStack  = new ItemStack(returnMaterial,amount);
                    }

                    returns.put(material,itemStack);
                }
            }

            if(config.contains("permission")){
                String key = config.getString("permission");
                permission = recipePermissionMap.containsKey(key) ? recipePermissionMap.get(key) : null;
            }

            Recipe recipe = new Recipe(name,tag,coordinates,returns,result,permission);
            recipes.add(recipe);
            namedRecipes.put(name,recipe);
        }
    }

    private Matter getMatterFromString(String name,Map<String,String> overrides){
        Matter matter;
        String upper = name.toUpperCase();
        if(name.equalsIgnoreCase("null")){
            // null
            matter = new Matter(Arrays.asList(Material.AIR),0,false);
        }else if(matters.containsKey(name)){
            // 'name' is contained 'matters'
            matter = matters.get(name);
        }else if(overrides.containsKey(name) && matters.containsKey(overrides.get(name))){
            // replaced the shorted name and contained 'matters'.
            matter = matters.get(overrides.get(name));
        }else if(allMaterials.contains(upper)){
            // normal material-name
            Material material = Material.valueOf(name.toUpperCase());
            matter = new Matter(Arrays.asList(material),1,false);
        }else if(overrides.containsKey(name) && allMaterials.contains(overrides.get(name).toUpperCase())){
            // replaced the shorted name and contained 'allMaterials'
            Material material = Material.valueOf(overrides.get(name).toUpperCase());
            matter = new Matter(Arrays.asList(material),1,false);
        }else if(name.startsWith("m-")){
            // normal material-name and 'mass=true'
            name = name.replace("m-","").toUpperCase();
            Material material = Material.valueOf(name);
            matter = new Matter(Arrays.asList(material),1,true);
        }else if (overrides.containsKey(name)){
            // replaced to the shorted name and contained 'matters'
            String before = overrides.get(name).replace("m-","").toUpperCase();
            if(before.equals("NULL")){
                matter = new Matter(Arrays.asList(Material.AIR),0,false);
            }else{
                Material material = Material.valueOf(before);
                matter = new Matter(Arrays.asList(material),1,true);
            }
        }else{
            // nothing other
            matter = new Matter(Arrays.asList(Material.AIR),0);
        }
        return matter;
    }
}

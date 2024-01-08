package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.command.Processor;
import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionBottleType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container.RecipeDataContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container.RecipeDataContainerModifyType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.MetadataType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import com.github.sakakiaruka.customcrafter.customcrafter.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.PLAYER_PERMISSIONS;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.RECIPE_PERMISSION_MAP;

public class SettingsLoad {

    // === defined settings values === //
    public static final int CRAFTING_TABLE_SIZE = 6;
    public static final int CRAFTING_TABLE_RESULT_SLOT = 44;
    public static final int CRAFTING_TABLE_MAKE_BUTTON = 35;
    public static final int CRAFTING_TABLE_TOTAL_SIZE = 54;
    public static final int VANILLA_CRAFTING_SLOTS = 9;
    public static final int VANILLA_CRAFTING_SQUARE_SIZE = 3;
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String BAR = String.join("",Collections.nCopies(40,"="));
    public static final String SHORT_BAR = String.join("",Collections.nCopies(20,"="));
    public static final String UPPER_ARROW = "↑";

    // === recipes public values === //
    public static Material BASE_BLOCK;
    public static List<Recipe> RECIPE_LIST = new ArrayList<>();
    public static List<String> ALL_MATERIALS = new ArrayList<>();
    public static Map<String,Recipe> NAMED_RECIPES_MAP = new HashMap<>();

    // === for crafting data manage === //
    public static Map<UUID,Material> WHAT_MAKING = Collections.synchronizedMap(new HashMap<>());

    // === for data get methods === //
    private static FileConfiguration DEFAULT_CONFIG;
    public static Map<String,Result> RESULTS = new HashMap<>();
    public static Map<String, Matter> MATTERS = new HashMap<>();
    public static Map<String, Result> CUSTOM_RESULTS = new HashMap<>();
    public static Map<String, Matter> CUSTOM_MATTERS = new HashMap<>();
    public static Set<String> COMMAND_ARGS = new HashSet<>();

    // === for runnable task === //
    private List<String> downloadUri;
    private List<String> failed = new ArrayList<>();

    private int returnCode = -1;
    private int times = 0;
    private int threshold;
    private int load_interval;

    // === for recipe load === //
    private static final String USING_CONTAINER_VALUES_METADATA_PATTERN = "^([0-9a-zA-Z_\\-]+) <--> (.+)$";
    private static final String MATTER_OVERRIDE_PATTERN = "^(\\w+) -> (\\w+)$";
    private static final String MATTER_REGEX_COLLECT_PATTERN = "^R\\|(.+)$";
    private static final String RESULT_METADATA_COLLECT_PATTERN = "^([\\w_]+),(.+)$";

    // === unlock registration === //
    public static Map<String, Recipe> REGISTERED_RECIPES = new HashMap<>();
    public static Map<Integer, List<Recipe>> ITEM_PLACED_SLOTS_RECIPE_MAP = new HashMap<>();
    public static Map<Integer, String> UNLOCK_TASK_ID_WITH_RECIPE_NAME = new HashMap<>();

    // === lock registration === //
    public static Map<Integer, String> LOCK_TASK_ID_WITH_RECIPE_NAME = new HashMap<>();

    // === for pass-through ===//
    private static final Result PASS_THROUGH_RESULT = new Result("PASS_THROUGH");

    public void load(){
        DEFAULT_CONFIG = getInstance().getConfig();
        recipePermissionLoad();
        getAllMaterialsName();
        main();
        getCommandArgs();
    }

    private void recipePermissionLoad(){

        if(DEFAULT_CONFIG.contains("permissions") && DEFAULT_CONFIG.contains("relate")){
            // The file that defines RecipePermissions.
            Path path = Paths.get(DEFAULT_CONFIG.getString("permissions"));
            new RecipePermissionUtil().permissionSettingsLoad(path);

            // The file that defines the relate between players and RecipePermissions.
            Path relate = Paths.get(DEFAULT_CONFIG.getString("relate"));
            new RecipePermissionUtil().permissionRelateLoad(relate);
        }
    }

    private void getAllMaterialsName(){
        Arrays.stream(Material.values()).forEach(s-> ALL_MATERIALS.add(s.name()));
    }

    private void main(){
        RESULTS.clear();
        MATTERS.clear();
        failed.clear();

        Path baseBlockPath = Paths.get(DEFAULT_CONFIG.getString("baseBlock"));

        List<Path> resultPaths = new ArrayList<>();
        List<Path> matterPaths = new ArrayList<>();
        List<Path> recipePaths = new ArrayList<>();
        DEFAULT_CONFIG.getStringList("results").forEach(s->resultPaths.add(Paths.get(s)));
        DEFAULT_CONFIG.getStringList("matters").forEach(s->matterPaths.add(Paths.get(s)));
        DEFAULT_CONFIG.getStringList("recipes").forEach(s->recipePaths.add(Paths.get(s)));

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

                matterPaths.forEach(p->getMatter(getFiles(p)));
                resultPaths.forEach(p->getResult(getFiles(p)));
                recipePaths.forEach(p->getRecipe(getFiles(p)));

                new Processor().init();

                Bukkit.getLogger().info("===Custom-Crafter data loaded.===");
            }
        };

        // === bukkit runnable finish ===//

        //getFilesFromTheSea (download section)
        if(DEFAULT_CONFIG.contains("download")){
            if(DEFAULT_CONFIG.getStringList("download").isEmpty()){
                // no download files.
                main.runTaskLater(getInstance(),20l);
                return;
            }
            downloadUri = DEFAULT_CONFIG.getStringList("download");
            List<String> downloadErrorMessageList = DEFAULT_CONFIG.getStringList("errorMessages");
            threshold = DEFAULT_CONFIG.getInt("download_threshold");
            load_interval = DEFAULT_CONFIG.getInt("download_interval");

            downloader.runTaskAsynchronously(getInstance());
            DEFAULT_CONFIG.set("download",failed);
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
            //File dir = new File(path.toUri());
            try{
                Files.createDirectories(path);
            }catch (Exception e) {
                e.printStackTrace();
                return;
            }

            //dir.mkdir();
            Bukkit.getLogger().info(String.format("Not found the directory \"%s\"."+ LINE_SEPARATOR +"So, the system made the directory named that.",path.toUri().toString()));
        }else if(!path.toFile().isDirectory()){
            Bukkit.getLogger().warning(String.format("The path \"%s\" is not a directory.",path.toUri().toString()));
            Bukkit.getLogger().warning("You must fix this problem when before you use this plugin.");

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
            Bukkit.getLogger().warning("[CustomCrafter] Error: Cannot get files from "+path);
            return null;
        }

        List<Path> list = new ArrayList<>();
        paths.forEach(s->list.add(s));
        return list;
    }

    private void getCommandArgs() {
        COMMAND_ARGS.addAll(DEFAULT_CONFIG.getStringList("COMMAND_ARGS"));
    }

    private void getBaseBlock(List<Path> paths){
        FileConfiguration config = YamlConfiguration.loadConfiguration(paths.get(0).toFile());
        BASE_BLOCK = Material.valueOf(config.getString("material").toUpperCase());
    }

    private void getResult(List<Path> paths){
        addAllVanillaMaterial();
        for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

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
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(list.get(0).toLowerCase()));
                    int level = Integer.parseInt(list.get(1));
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
                    Matcher matcher = Pattern.compile(RESULT_METADATA_COLLECT_PATTERN).matcher(s);
                    if (!matcher.matches()) continue;
                    MetadataType key = MetadataType.valueOf(matcher.group(1).toUpperCase());
                    String value = matcher.group(2);
                    if(!metadata.containsKey(key))metadata.put(key,new ArrayList<>());
                    metadata.get(key).add(value);
                }
            }

            Result result = new Result(name,enchantInfo,amount,metadata,nameOrRegex,matchPoint);
            RESULTS.put(name,result);
            CUSTOM_RESULTS.put(name, result);
        }
    }

    private void addAllVanillaMaterial(){
        for(Material material : Material.values()){
            String name = material.name().toLowerCase();
//            Result test = new Result(name,null,1,null,material.name(),-1, new ArrayList<>());
            Result result = new Result().
                    setAmount(1).
                    setName(name).
                    setNameOrRegex(material.name()).
                    setMatchPoint(-1);

            RESULTS.put(name,result);

            Matter matter = new Matter(Arrays.asList(material), 1);
            MATTERS.put(name, matter);
        }
    }

    public void getMatter(List<Path> paths){
        // add Null (for Override)
        addNull();
        addWaterBottle();

        Top : for(Path path:paths){
            FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

            new DataCheckerUtil().matterCheck(new StringBuilder(),config,path);

            String name = config.getString("name");
            int amount = config.getInt("amount");
            boolean mass = config.getBoolean("mass");
            List<Material> candidate = new ArrayList<>();
            for(String s:config.getStringList("candidate")){
                if(s.matches(MATTER_REGEX_COLLECT_PATTERN)){
                    Matcher matcher = Pattern.compile(MATTER_REGEX_COLLECT_PATTERN).matcher(s);
                    if (!matcher.matches()) continue;
                    String pattern = matcher.group(1); // remove "R|"

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
                    Matcher matcher = Pattern.compile("^([\\w_]+),(\\d+),(\\w+)$").matcher(s);
                    if (!matcher.matches()) continue;
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(matcher.group(1).toLowerCase()));
                    int level = Integer.parseInt(matcher.group(2));
                    EnchantStrict strict = EnchantStrict.valueOf(matcher.group(3).toUpperCase());

                    EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
                    wrapList.add(wrap);
                }
            }

            if(wrapList.isEmpty())wrapList = null;


            Matter matter = new Matter(name,candidate,wrapList,amount,mass);

            //PotionData collect
            if(config.contains("potion") && config.contains("bottleTypeMatch")){
                Potions potions = makeDrug(matter,config);
                if(potions != null) MATTERS.put(name,potions);
                continue;
            }

            Map<Integer, ContainerWrapper> elements = new ContainerUtil().mattersLoader(path);
            matter.setContainerWrappers(elements);
            MATTERS.put(name,matter);
            CUSTOM_MATTERS.put(name, matter);
        }
    }

    private void addNull(){
        Matter matter = new Matter(Arrays.asList(Material.AIR),0);
        MATTERS.put("null",matter);
        //matters.put("NULL",matter);
    }

    private void addWaterBottle(){
        Potions waterBottle = new PotionUtil().water_bottle();
        MATTERS.put("water_bottle",waterBottle);
    }

    private Potions makeDrug(Matter matter, FileConfiguration config){
        // - PotionEffectType, duration, amplifier, strict
        Map<PotionEffect, PotionStrict> map = new HashMap<>();
        for(String str : config.getStringList("potion")){
            List<String> list = Arrays.asList(str.split(","));
            if(list.size() != 4) {
                Bukkit.getLogger().warning("[Custom Crafter] Potion Configuration Parameter are not enough.");
                return null;
            }
            PotionEffectType effectType = PotionEffectType.getByName(list.get(0).toUpperCase());
            int duration = Integer.parseInt(list.get(1));
            int amplifier = Integer.parseInt(list.get(2));//-1 < 0 ? 0 : Integer.parseInt(list.get(2)) -1;
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

        }
        return list;
    }

    private boolean containsAllMaterialsIgnoreCase(String in){
        for(String str : ALL_MATERIALS){
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
            Result result = RESULTS.getOrDefault(config.getString("result"), PASS_THROUGH_RESULT);

            Map<Coordinate,Matter> coordinates = new LinkedHashMap<>();
            Map<Material, ItemStack> returns = new HashMap<>();
            Map<String,String> overrides = new HashMap<>();
            RecipePermission permission = null;

            if(config.contains("override")){
                /* override:
                *  - cobblestone -> c
                *  - stone -> s
                *
                * MATTER_OVERRIDE_PATTERN = "^(\\w+) -> (\\w+)$";
                */
                for(String string : config.getStringList("override")){
                    Matcher matcher = Pattern.compile(MATTER_OVERRIDE_PATTERN).matcher(string);
                    if (!matcher.matches()) continue;
                    String source = matcher.group(1);
                    String shorter = matcher.group(2);
                    overrides.put(shorter,source);
                }
            }

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
            }


            if(config.contains("returns")){
                /* 0,1,2
                * 0 : Material Name (String) or regex pattern
                * 1 : return Material Name (String)
                * 2 : return item amount (int)
                 */
                String PASS_THROUGH_PATTERN = "(?i)pass";
                String NORMAL_PATTERN = "([a-zA-Z_]+)";

                for(String s:config.getStringList("returns")){
                    List<String> list = Arrays.asList(s.split(","));
                    ItemStack itemStack;
                    List<Material> materials = new ArrayList<>();

                    if(list.get(0).matches(NORMAL_PATTERN)){
                        materials.add(Material.valueOf(list.get(0).toUpperCase()));
                    }else{
                        // regex collect
                        for(String id : ALL_MATERIALS){
                            if(id.matches(list.get(0))) materials.add(Material.valueOf(id));
                        }
                    }

                    if(list.get(1).equals("water_bottle")){
                        itemStack = new PotionUtil().water_bottle_ItemStack();
                    }else if(list.get(1).matches(PASS_THROUGH_PATTERN)){
                        // pass through
                        int amount = Integer.parseInt(list.get(2));
                        itemStack = new ItemStack(Material.AIR,amount);
                    }else{
                        Material returnMaterial = Material.valueOf(list.get(1).toUpperCase());
                        int amount = Integer.parseInt(list.get(2));
                        itemStack  = new ItemStack(returnMaterial,amount);
                    }

                    materials.forEach(x->returns.put(x,itemStack));
                }
            }

            if(config.contains("permission")){
                String key = config.getString("permission");
                permission = RECIPE_PERMISSION_MAP.containsKey(key) ? RECIPE_PERMISSION_MAP.get(key) : null;
            }

            Map<NamespacedKey, List<RecipeDataContainer>> map = new HashMap<>();
            if (config.contains("container_modify")) {
                int counter = 0;

                while (true) {
                    String address = "container_modify."+counter+".";
                    if (!config.contains(address)) break;
                    NamespacedKey key = new NamespacedKey(getInstance(), config.getString(address+"key"));
                    RecipeDataContainerModifyType modifyType = RecipeDataContainerModifyType.valueOf(config.getString(address+"modify_type").toUpperCase());
                    PersistentDataType type = new ContainerUtil().getDataType(config.getString(address+"type").toUpperCase());
                    String term = config.getString(address+"term");
                    String action = Objects.requireNonNullElse(config.getString(address + "action"), "");
                    boolean end = config.getBoolean(address+"return");
                    counter++;
                    RecipeDataContainer data = new RecipeDataContainer(type, term, action, end, modifyType);

                    if (!map.containsKey(key)) map.put(key, new ArrayList<>());
                    map.get(key).add(data);
                }
            }

            Map<Matter, List<String>> usingContainerValuesMetadata = new HashMap<>();
            if (config.contains("using_container_values_metadata")) {
                for (String s : config.getStringList("using_container_values_metadata")) {
                    Matcher matcher = Pattern.compile(USING_CONTAINER_VALUES_METADATA_PATTERN).matcher(s);
                    if (!matcher.matches()) continue;
                    String matterName = matcher.group(1);
                    if (!MATTERS.containsKey(matterName) && !ALL_MATERIALS.contains(matterName.toUpperCase())) continue;
                    Matter matter = MATTERS.get(matterName);
                    String order = matcher.group(2);
                    if (!usingContainerValuesMetadata.containsKey(matter)) usingContainerValuesMetadata.put(matter, new ArrayList<>());
                    usingContainerValuesMetadata.get(matter).add(order);

                }
            }

            if (config.contains("lock")) {
                String lockTimeRow = config.getString("lock");
                Duration duration;
                if ((duration = getDuration(lockTimeRow, true)) == null) continue;
                if (duration != Duration.ZERO) {
                    long delayTicks = duration.getSeconds() * 20;
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            int id = this.getTaskId();
                            String targetName = LOCK_TASK_ID_WITH_RECIPE_NAME.get(id);
                            Recipe recipe = NAMED_RECIPES_MAP.get(targetName);
                            RECIPE_LIST.remove(recipe);
                            ITEM_PLACED_SLOTS_RECIPE_MAP.get(recipe.getContentsNoAir().size()).remove(recipe);
                            NAMED_RECIPES_MAP.remove(targetName);
                            LOCK_TASK_ID_WITH_RECIPE_NAME.remove(id);

                            Bukkit.getLogger().info("[CustomCrafter] "+targetName+" is disabled now!");
                        }
                    };
                    runnable.runTaskLater(CustomCrafter.getInstance(), delayTicks);
                    int taskID = runnable.getTaskId();
                    LOCK_TASK_ID_WITH_RECIPE_NAME.put(taskID, name);

                    Bukkit.getLogger().info(String.format("[CustomCrafter] Named Recipe=%s will be locked in %s", name, ZonedDateTime.parse(lockTimeRow).toString()));
                }
            }

            if (config.contains("unlock")) {
                String unlockTimeRow = config.getString("unlock");
                Duration duration;
                if ((duration = getDuration(unlockTimeRow, false)) == null) continue;
                if (duration != Duration.ZERO) {
                    long delayTicks = duration.getSeconds() * 20;
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            int id = this.getTaskId();
                            String targetName = UNLOCK_TASK_ID_WITH_RECIPE_NAME.get(id);
                            Recipe target = REGISTERED_RECIPES.get(targetName);
                            RECIPE_LIST.add(target);
                            NAMED_RECIPES_MAP.put(targetName, target);
                            if (!ITEM_PLACED_SLOTS_RECIPE_MAP.containsKey(target.getContentsNoAir().size())) {
                                ITEM_PLACED_SLOTS_RECIPE_MAP.put(target.getContentsNoAir().size(), new ArrayList<>());
                            }
                            ITEM_PLACED_SLOTS_RECIPE_MAP.get(target.getContentsNoAir().size()).add(target);

                            Bukkit.getLogger().info("[CustomCrafter] "+targetName+" is enabled now!");
                            REGISTERED_RECIPES.remove(targetName);
                            UNLOCK_TASK_ID_WITH_RECIPE_NAME.remove(id);
                        }
                    };
                    runnable.runTaskLater(CustomCrafter.getInstance(), delayTicks);
                    int taskID = runnable.getTaskId();
                    REGISTERED_RECIPES.put(name, new Recipe(name, tag, coordinates, returns, result, permission, map, usingContainerValuesMetadata));
                    UNLOCK_TASK_ID_WITH_RECIPE_NAME.put(taskID, name);

                    Bukkit.getLogger().info(String.format("[CustomCrafter] Named Recipe=%s will be unlocked in %s", name, ZonedDateTime.parse(unlockTimeRow).toString()));
                    continue;
                }
            }

            Recipe recipe = new Recipe(name, tag, coordinates, returns, result, permission, map, usingContainerValuesMetadata);
            RECIPE_LIST.add(recipe);
            NAMED_RECIPES_MAP.put(name,recipe);
            if (!ITEM_PLACED_SLOTS_RECIPE_MAP.containsKey(recipe.getContentsNoAir().size())) {
                ITEM_PLACED_SLOTS_RECIPE_MAP.put(recipe.getContentsNoAir().size(), new ArrayList<>());
            }
            ITEM_PLACED_SLOTS_RECIPE_MAP.get(recipe.getContentsNoAir().size()).add(recipe);
        }
    }

    private Duration getDuration(String dateRow, boolean delete) {
        ZonedDateTime zonedDateTime;
        try {
            // for example
            // 2023-10-17T16:45:30+09:00[Asia/Tokyo]
            // yyyy-mm-ddThh:mm:ss+offset with UTC[TIMEZONE]
            zonedDateTime = ZonedDateTime.parse(dateRow);
        } catch (DateTimeParseException e) {
            Bukkit.getLogger().warning("[CustomCrafter] An invalid DateTime format found. (from java.time.ZonedDateTime=DateTimeParseException)");
            Bukkit.getLogger().info("[CustomCrafter] So, the system will not load this recipe and register to the list about auto "+(delete ? "" : "un")+"lock.");
            return null;
        }
        ZonedDateTime now = ZonedDateTime.now();
        if (zonedDateTime.isAfter(now)) {
            // register (unlock) || remove (lock)
            Duration duration;
            try {
                duration = Duration.between(zonedDateTime, now).abs();
            } catch (DateTimeException e) {
                Bukkit.getLogger().warning("[CustomCrafter] The system cannot get times diff about now and unlock date. (DateTimeException)");
                return null;
            } catch (ArithmeticException e) {
                Bukkit.getLogger().warning("[CustomCrafter] The system could not register to "+(delete ? "" : "un")+"lock the recipe. (Diff times over the limit.)");
                return null;
            }
            return duration;
        } else if (delete) return null; // already locked
        return Duration.ZERO;  // normal load
    }

    private Matter getMatterFromString(String name,Map<String,String> overrides){
        Matter matter;
        String upper = name.toUpperCase();
        if(name.equalsIgnoreCase("null")){
            // null
            matter = new Matter(Arrays.asList(Material.AIR),0,false);
        }else if(MATTERS.containsKey(name)){
            // 'name' is contained 'matters'
            matter = MATTERS.get(name);
        }else if(overrides.containsKey(name) && MATTERS.containsKey(overrides.get(name))){
            // replaced the shorted name and contained 'matters'.
            matter = MATTERS.get(overrides.get(name));
        }else if(ALL_MATERIALS.contains(upper)){
            // normal material-name
            Material material = Material.valueOf(name.toUpperCase());
            matter = new Matter(Arrays.asList(material),1,false);
        }else if(overrides.containsKey(name) && ALL_MATERIALS.contains(overrides.get(name).toUpperCase())){
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

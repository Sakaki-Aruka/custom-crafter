package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;

public class SettingsLoad {
    public static Material baseBlock;
    public static List<Recipe> recipes = new ArrayList<>();
    public static List<String> allMaterials = new ArrayList<>();
    public static Map<String,Recipe> namedRecipes = new HashMap<>();

    private static FileConfiguration defaultConfig;
    private static Map<String,Result> results = new HashMap<>();
    private static Map<String, Matter> matters = new HashMap<>();

    // === for runnable task === //
    private int returnCode = -1;
    private int times = 0;
    private int threshold;
    private int load_interval;

    public void load(){
        defaultConfig = getInstance().getConfig();
        new OpenCraftingTable().setCraftingInventory();
        getAllMaterialsName();
    }

    private void getAllMaterialsName(){
        Arrays.stream(Material.values()).forEach(s->allMaterials.add(s.name()));
    }

    private void getPaths(){
        Path baseBlockPath = Paths.get(defaultConfig.getString("baseBlock"));
        Path resultPath = Paths.get(defaultConfig.getString("result"));
        Path matterPath = Paths.get(defaultConfig.getString("matter"));
        Path recipePath = Paths.get(defaultConfig.getString("recipe"));
        List<String> downloadUri = defaultConfig.getStringList("download");
        List<String> downloadErrorMessageList = defaultConfig.getStringList("errorMessages");
        threshold = defaultConfig.getInt("download_threshold");
        load_interval = defaultConfig.getInt("download_interval");

        List<String> failed = new ArrayList<>();
        BukkitRunnable downloader = new BukkitRunnable() {
            @Override
            public void run() {
                for(String command:downloadUri){
                    if(command.isEmpty())continue;
                    ProcessBuilder builder = new ProcessBuilder(Arrays.asList(command.split(" ")));
                    Process process;
                    try{
                        process = builder.start();
                        process.waitFor();
                    }catch (Exception e){
                        downloadErrorMessageList.forEach(System.out::println);
                        e.printStackTrace();
                        failed.add(command);
                    }
                }
            }
        };

        BukkitRunnable loader = new BukkitRunnable() {
            @Override
            public void run() {
                if(times <= threshold && returnCode == 0){
                    //main.runTaskLater(getInstance(),20);
                    this.cancel();
                    System.out.println("[CustomCrafter] Configs download was completed!");
                    return;
                }else if(times > threshold){
                    System.out.println("[CustomCrafter] Could not load date.");
                    this.cancel();
                    return;
                }
                System.out.println("[CustomCrafter]Downloading now ...");
                times++;
            }
        };

        loader.runTaskTimer(getInstance(),20,load_interval);

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

    private void getResult(Path path){
        //
    }

    private void getMatter(Path path){
        //
    }

    private void getRecipe(Path path){
        //
    }

}

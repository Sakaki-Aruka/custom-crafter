package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;

public class SettingsLoad {
    public static Material baseBlock;
    public static List<Recipe> recipes;

    private static FileConfiguration defaultConfig;
    private static Map<String,Result> results;
    private static Map<String, Matter> matters;

    public void load(){
        defaultConfig = getInstance().getConfig();
        new OpenCraftingTable().setCraftingInventory();
    }

}

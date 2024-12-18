package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe;
import com.github.sakakiaruka.customcrafter.customcrafter.api.object.recipe.CRecipeContainer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;
    public static long INITIALIZED;

    static List<CRecipe> RECIPES = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        INITIALIZED = System.currentTimeMillis();
        CustomCrafterAPI.INSTANCE.setup$custom_crafter();

    }

    @Override
    public void onDisable() {}

    public static CustomCrafter getInstance(){
        return instance;
    }
}

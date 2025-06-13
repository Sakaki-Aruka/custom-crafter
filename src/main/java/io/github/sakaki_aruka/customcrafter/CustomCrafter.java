package io.github.sakaki_aruka.customcrafter;

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe;
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

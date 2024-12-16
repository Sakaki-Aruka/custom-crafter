package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;
    public static boolean ENABLED_PLACEHOLDER_API;
    public static long INITIALIZED;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.instance = this;

        INITIALIZED = System.currentTimeMillis();
        CustomCrafterAPI.INSTANCE.setup$custom_crafter();
    }

    @Override
    public void onDisable() {}

    public static CustomCrafter getInstance(){
        return instance;
    }
}

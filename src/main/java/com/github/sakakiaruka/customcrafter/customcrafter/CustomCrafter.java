package com.github.sakakiaruka.customcrafter.customcrafter;

import org.bukkit.plugin.java.JavaPlugin;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static CustomCrafter getInstance(){
        return instance;
    }
}

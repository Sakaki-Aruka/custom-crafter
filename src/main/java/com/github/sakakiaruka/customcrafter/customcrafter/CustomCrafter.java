package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.command.Processor;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.Listener;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;
    public static boolean ENABLED_PLACEHOLDER_API;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.instance = this;
        new SettingsLoad().load();
        getCommand("cc").setExecutor(new Processor());
        getServer().getPluginManager().registerEvents(new Listener(),this);
        ENABLED_PLACEHOLDER_API = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (getConfig().contains("permissions") && getConfig().contains("relate")) {
            Path path = Paths.get(getConfig().getString("relate"));
            RecipePermissionUtil.playerPermissionWriter(path);
        }
    }

    public static CustomCrafter getInstance(){
        return instance;
    }
}

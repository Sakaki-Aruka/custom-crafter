package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI;
import com.github.sakakiaruka.customcrafter.customcrafter.command.HistoryDatabase;
import com.github.sakakiaruka.customcrafter.customcrafter.command.Processor;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.Listener;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PlaceholderUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;
    public static boolean ENABLED_PLACEHOLDER_API;
    public static long INITIALIZED;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.instance = this;

        INITIALIZED = System.currentTimeMillis();
        CustomCrafterAPI.INSTANCE.setup$custom_crafter();

//        new SettingsLoad().load();
//        getCommand("cc").setExecutor(new Processor());
//        getCommand("history_database").setExecutor(HistoryDatabase.INSTANCE);
//        getServer().getPluginManager().registerEvents(new Listener(),this);
//        ENABLED_PLACEHOLDER_API = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
//        if (ENABLED_PLACEHOLDER_API) {
//            new PlaceholderUtil().register();
//        }
    }

    @Override
    public void onDisable() {
//        // Plugin shutdown logic
//        if (getConfig().contains("permissions") && getConfig().contains("relate")) {
//            Path path = Paths.get(getConfig().getString("relate"));
//            RecipePermissionUtil.playerPermissionWriter(path);
//        }
    }

    public static CustomCrafter getInstance(){
        return instance;
    }
}

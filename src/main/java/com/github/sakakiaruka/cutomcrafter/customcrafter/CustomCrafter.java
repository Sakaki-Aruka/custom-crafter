package com.github.sakakiaruka.cutomcrafter.customcrafter;

import com.github.sakakiaruka.cutomcrafter.customcrafter.commands.CheckObject;
import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.Listeners;
import com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.instance = this;
        new SettingsLoad().set();
        getServer().getPluginManager().registerEvents(new Listeners(),this);
        getCommand("cc").setExecutor(new CheckObject());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static CustomCrafter getInstance(){
        return instance;
    }
}

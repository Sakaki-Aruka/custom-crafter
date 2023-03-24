package com.github.sakakiaruka.customcrafter.customcrafter;

import com.github.sakakiaruka.customcrafter.customcrafter.command.Check;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.instance = this;
        new SettingsLoad().load();
        getCommand("cc").setExecutor(new Check());
        getServer().getPluginManager().registerEvents(new Listener(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static CustomCrafter getInstance(){
        return instance;
    }
}

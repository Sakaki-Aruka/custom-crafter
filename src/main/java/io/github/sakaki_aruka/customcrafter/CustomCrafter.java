package io.github.sakaki_aruka.customcrafter;

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe;
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI;
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryClickListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryCloseListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.PlayerInteractListener;
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

        Bukkit.getPluginManager().registerEvents(InventoryClickListener.INSTANCE, instance);
        Bukkit.getPluginManager().registerEvents(InventoryCloseListener.INSTANCE, instance);
        Bukkit.getPluginManager().registerEvents(PlayerInteractListener.INSTANCE, instance);

        // TODO: write to register GUI id (CustomCrafterGUI.PAGES)

        InternalAPI.INSTANCE.runTests();
    }

    @Override
    public void onDisable() {}

    public static CustomCrafter getInstance(){
        return instance;
    }
}

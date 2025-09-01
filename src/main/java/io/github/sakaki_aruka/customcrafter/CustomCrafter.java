package io.github.sakaki_aruka.customcrafter;

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe;
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI;
import io.github.sakaki_aruka.customcrafter.internal.command.CC;
import io.github.sakaki_aruka.customcrafter.internal.listener.AutoCraftPowerOnListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.BlockBreakEventListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.BlockPhysicsListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryClickListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryCloseListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryMoveItemListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.NoPlayerListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.PlayerInteractListener;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CustomCrafter extends JavaPlugin {

    private static CustomCrafter instance;

    static List<CRecipe> RECIPES = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        Bukkit.getPluginManager().registerEvents(InventoryClickListener.INSTANCE, instance);
        Bukkit.getPluginManager().registerEvents(InventoryCloseListener.INSTANCE, instance);
        Bukkit.getPluginManager().registerEvents(PlayerInteractListener.INSTANCE, instance);
        Bukkit.getPluginManager().registerEvents(NoPlayerListener.Companion, instance);

        Bukkit.getPluginManager().registerEvents(AutoCraftPowerOnListener.INSTANCE, this);
        Bukkit.getPluginManager().registerEvents(BlockPhysicsListener.INSTANCE, this);
        Bukkit.getPluginManager().registerEvents(InventoryMoveItemListener.INSTANCE, this);
        Bukkit.getPluginManager().registerEvents(BlockBreakEventListener.INSTANCE, this);

        InternalAPI.INSTANCE.setupAutoCraftDatabase(false);

        this.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                commands -> {
                    commands.registrar().register(CC.INSTANCE.getCommand().build());
                }
        );
    }

    @Override
    public void onDisable() {
        InternalAPI.INSTANCE.shutdown();
    }

    public static CustomCrafter getInstance(){
        return instance;
    }
}

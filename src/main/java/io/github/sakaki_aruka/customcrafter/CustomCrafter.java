package io.github.sakaki_aruka.customcrafter;

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe;
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI;
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.AutoCraft;
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI;
import io.github.sakaki_aruka.customcrafter.internal.gui.OldWarnGUI;
import io.github.sakaki_aruka.customcrafter.internal.gui.PredicateProvider;
import io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate.AllCandidateGUI;
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.CBlockInfoGUI;
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.RecipeModifyGUI;
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.SlotsModifyGUI;
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftingGUI;
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryClickListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryCloseListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.NoPlayerListener;
import io.github.sakaki_aruka.customcrafter.internal.listener.PlayerInteractListener;
import kotlin.jvm.JvmClassMappingKt;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        Bukkit.getPluginManager().registerEvents(NoPlayerListener.Companion, instance);

        // TODO: register GUI class/object to CustomCrafterGUI.PAGES (ALL GUIs)
        CustomCrafterGUI.Companion.getPAGES().put(JvmClassMappingKt.getKotlinClass(SlotsModifyGUI.class), UUID.randomUUID());
        CustomCrafterGUI.Companion.getPAGES().put(JvmClassMappingKt.getKotlinClass(CBlockInfoGUI.class), UUID.randomUUID());
        CustomCrafterGUI.Companion.getPAGES().put(JvmClassMappingKt.getKotlinClass(RecipeModifyGUI.class), UUID.randomUUID());
        CustomCrafterGUI.Companion.getPAGES().put(JvmClassMappingKt.getKotlinClass(OldWarnGUI.class), UUID.randomUUID());
        CustomCrafterGUI.Companion.getPAGES().put(JvmClassMappingKt.getKotlinClass(AllCandidateGUI.class), UUID.randomUUID());

        // TODO: register CustomCrafterGUI.GuiDeserializer
        CustomCrafterGUI.Companion.getDESERIALIZERS().add(CBlockInfoGUI.Companion);
        CustomCrafterGUI.Companion.getDESERIALIZERS().add(SlotsModifyGUI.Companion);
        CustomCrafterGUI.Companion.getDESERIALIZERS().add(CraftingGUI.Companion);
        CustomCrafterGUI.Companion.getDESERIALIZERS().add(OldWarnGUI.Companion);
        CustomCrafterGUI.Companion.getDESERIALIZERS().add(AllCandidateGUI.Companion);

        // TODO: register PredicateProvider<T: out CustomCrafterGUI>::class to PredicateProvider.PROVIDERS
        PredicateProvider.Companion.getPROVIDERS().add(CBlockInfoGUI.Companion);
        PredicateProvider.Companion.getPROVIDERS().add(SlotsModifyGUI.Companion);
        PredicateProvider.Companion.getPROVIDERS().add(CraftingGUI.Companion);

        // TODO: register NoPlayerListener (NoPlayerListener.LISTENERS)
        NoPlayerListener.Companion.getLISTENERS().add(AutoCraft.AutoCraftRedstoneSignalReceiver.INSTANCE);
        NoPlayerListener.Companion.getLISTENERS().add(AutoCraft.AutoCraftItemInputSignalReceiver.INSTANCE);

        InternalAPI.INSTANCE.runTests();
    }

    @Override
    public void onDisable() {}

    public static CustomCrafter getInstance(){
        return instance;
    }
}

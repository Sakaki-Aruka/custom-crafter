package online.aruka.custom_crafter.demo

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import online.aruka.custom_crafter.demo.listener.CraftFailureListener
import online.aruka.custom_crafter.demo.listener.CreateCustomItemListener
import online.aruka.custom_crafter.demo.listener.CustomCrafterAPIPropertiesChangeListener
import online.aruka.custom_crafter.demo.listener.CustomItemRegisteredListener
import online.aruka.custom_crafter.demo.recipe.shaped.OverLimitEnchantedBook
import online.aruka.custom_crafter.demo.recipe.shapeless.DyeMixin
import online.aruka.custom_crafter.demo.register.ShapedRecipeProvider
import online.aruka.custom_crafter.demo.register.ShapelessRecipeProvider
import online.aruka.custom_crafter.demo.ui.CraftUI
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Demo : JavaPlugin() {

    companion object {
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        // Plugin startup logic
        plugin = this

        this.logger.info("Custom Crafter API found: ${CustomCrafterAPI.API_VERSION}")

        // register listeners to Bukkit api
        Bukkit.getPluginManager().registerEvents(CustomItemRegisteredListener, this)
        Bukkit.getPluginManager().registerEvents(CustomCrafterAPIPropertiesChangeListener, this)
        Bukkit.getPluginManager().registerEvents(CreateCustomItemListener(), this)
        Bukkit.getPluginManager().registerEvents(CraftFailureListener(), this)

        // register shaped recipes
        CustomCrafterAPI.registerRecipe(listOf(
            ShapedRecipeProvider.enchantedGoldenApple(),
            ShapedRecipeProvider.wateredBottles(),
            ShapedRecipeProvider.moreWateredBottles(),
            ShapedRecipeProvider.infinityIronBlockCore(),
            ShapedRecipeProvider.infinityIronBlock(),
            OverLimitEnchantedBook.onlyEfficiency()
        ), plugin)

        // register shapeless recipes
        CustomCrafterAPI.registerRecipe(listOf(
            ShapelessRecipeProvider.glowBerry(),
            ShapelessRecipeProvider.infinityIronBlockExtract(),
            ShapelessRecipeProvider.extractPotion(),
            DyeMixin.mixWithoutBlack()
        ), plugin)

        CustomCrafterAPI.setCraftUIDesigner(CraftUI())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

package online.aruka.demo

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import online.aruka.demo.listener.CreateCustomItemListener
import online.aruka.demo.listener.CustomCrafterAPIPropertiesChangeListener
import online.aruka.demo.listener.CustomItemRegisteredListener
import online.aruka.demo.recipe.shaped.OverLimitEnchantedBook
import online.aruka.demo.recipe.shapeless.DyeMixin
import online.aruka.demo.register.ShapedRecipeProvider
import online.aruka.demo.register.ShapelessRecipeProvider
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

        // register shaped recipes
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.enchantedGoldenApple())
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.wateredBottles())
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.moreWateredBottles())
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.infinityIronBlockCore())
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.infinityIronBlock())
        CustomCrafterAPI.registerRecipe(OverLimitEnchantedBook.onlyEfficiency())

        // register shapeless recipes
        CustomCrafterAPI.registerRecipe(ShapelessRecipeProvider.glowBerry())
        CustomCrafterAPI.registerRecipe(ShapelessRecipeProvider.infinityIronBlockExtract())
        CustomCrafterAPI.registerRecipe(ShapelessRecipeProvider.extractPotion())
        CustomCrafterAPI.registerRecipe(DyeMixin.mixWithoutBlack())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

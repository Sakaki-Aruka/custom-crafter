package online.aruka.demo

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import online.aruka.demo.listener.CustomItemRegisteredListener
import online.aruka.demo.recipe.ShapedRecipeProvider
import online.aruka.demo.recipe.ShapelessRecipeProvider
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

        // register shaped recipes
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.enchantedGoldenApple())
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.wateredBottles())
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.moreWateredBottles())
        CustomCrafterAPI.registerRecipe(ShapedRecipeProvider.infinityIronBlock())

        // register shapeless recipes
        CustomCrafterAPI.registerRecipe(ShapelessRecipeProvider.glowBerry())
        CustomCrafterAPI.registerRecipe(ShapelessRecipeProvider.infinityIronBlock())
        CustomCrafterAPI.registerRecipe(ShapelessRecipeProvider.infinityIronBlockExtract())
        CustomCrafterAPI.registerRecipe(ShapelessRecipeProvider.extractPotion())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

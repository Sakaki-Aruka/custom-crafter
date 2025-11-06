package online.aruka.demo

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import online.aruka.demo.listener.CustomItemRegisteredListener
import online.aruka.demo.recipe.ShapedRecipeProvider
import online.aruka.demo.recipe.ShapelessRecipeProvider
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Demo : JavaPlugin() {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var API: CustomCrafterAPI
    }

    override fun onEnable() {
        // Plugin startup logic
        plugin = this
        API = (Bukkit.getPluginManager().getPlugin("Custom_Crafter") as? CustomCrafter)
            ?.api
            ?: run {
                Bukkit.getPluginManager().disablePlugin(this)
                return
            }

        this.logger.info("Custom Crafter API found: ${API.API_VERSION}")

        // register listeners to Bukkit api
        Bukkit.getPluginManager().registerEvents(CustomItemRegisteredListener, this)

        // register shaped recipes
        API.registerRecipe(ShapedRecipeProvider.enchantedGoldenApple())
        API.registerRecipe(ShapedRecipeProvider.wateredBottles())

        // register shapeless recipes
        API.registerRecipe(ShapelessRecipeProvider.glowBerry())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

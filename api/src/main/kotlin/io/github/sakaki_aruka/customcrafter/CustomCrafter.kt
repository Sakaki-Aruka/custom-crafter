package io.github.sakaki_aruka.customcrafter

import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.command.CC
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryClickListener
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryCloseListener
import io.github.sakaki_aruka.customcrafter.internal.listener.PlayerInteractListener
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.Collections

class CustomCrafter : JavaPlugin() {

    companion object {
        private var instance: CustomCrafter? = null

        internal val RECIPES: MutableList<CRecipe> = Collections.synchronizedList(mutableListOf())

        @JvmStatic
        fun getInstance(): CustomCrafter = instance ?: CustomCrafter().also { instance = it }
    }

    override fun onEnable() {
        instance = this
        InternalAPI.startup()

        Bukkit.getPluginManager().registerEvents(InventoryClickListener, this)
        Bukkit.getPluginManager().registerEvents(InventoryCloseListener, this)
        Bukkit.getPluginManager().registerEvents(PlayerInteractListener, this)
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(CC.command.build())
        }
    }

    override fun onDisable() {
        InternalAPI.shutdown()
    }
}

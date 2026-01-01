package io.github.sakaki_aruka.customcrafter.impl.util

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.Callable
import java.util.concurrent.Future

object AsyncUtil {
    fun <T> Callable<T>.fromBukkitMainThread(
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): T {
        return Bukkit.getScheduler().callSyncMethod(plugin, this).get()
    }

    fun <T> Callable<T>.futureFromBukkitMainThread(
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): Future<T> {
        return Bukkit.getScheduler().callSyncMethod(plugin, this)
    }

    @JvmStatic
    @JvmOverloads
    fun <T> getFromBukkitMainThread(
        callable: Callable<T>,
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): T {
        return Bukkit.getScheduler().callSyncMethod(plugin, callable).get()
    }

    @JvmStatic
    @JvmOverloads
    fun <T> getFutureFromBukkitMainThread(
        callable: Callable<T>,
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): Future<T> {
        return Bukkit.getScheduler().callSyncMethod(plugin, callable)
    }
}
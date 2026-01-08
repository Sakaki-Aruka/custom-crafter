package io.github.sakaki_aruka.customcrafter.impl.util

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.Callable
import java.util.concurrent.Future

object AsyncUtil {
    /**
     * Returns value from bukkit main thread.
     *
     * ```kotlin
     * // Example (In Async Thread)
     * val players = Callable { Bukkit.getOnlinePlayers() }.fromBukkitMainThread()
     * ```
     *
     * @return[T] Specified type value
     * @since 5.0.17
     */
    fun <T> Callable<T>.fromBukkitMainThread(
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): T {
        return if (Bukkit.isPrimaryThread()) {
            this.call()
        } else {
            InternalAPI.scheduler().callSyncMethod(plugin, this).get()
        }
    }

    /**
     * Returns `Future<T>` from bukkit main thread.
     *
     * ```kotlin
     * // Example (In Async Thread)
     * val futureTask = Callable { Bukkit.getOnlinePlayers() }.futureFromBukkitMainThread()
     * val players = futureTask.get()
     * ```
     * @return[Future] Future task
     * @since 5.0.17
     */
    fun <T> Callable<T>.futureFromBukkitMainThread(
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): Future<T> {
        return InternalAPI.scheduler().callSyncMethod(plugin, this)
    }

    @JvmStatic
    @JvmOverloads
    fun <T> getFromBukkitMainThread(
        callable: Callable<T>,
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): T {
        return callable.fromBukkitMainThread(plugin)
    }

    @JvmStatic
    @JvmOverloads
    fun <T> getFutureFromBukkitMainThread(
        callable: Callable<T>,
        plugin: JavaPlugin = CustomCrafter.getInstance()
    ): Future<T> {
        return callable.futureFromBukkitMainThread(plugin)
    }
}
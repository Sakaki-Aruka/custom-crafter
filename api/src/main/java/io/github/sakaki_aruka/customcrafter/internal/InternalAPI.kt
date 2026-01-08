package io.github.sakaki_aruka.customcrafter.internal

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitScheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object InternalAPI {

    @JvmStatic
    fun startup() {
        EXECUTOR = Executors.newVirtualThreadPerTaskExecutor()
        SCHEDULER = Bukkit.getScheduler()
    }

    @JvmStatic
    fun shutdown() {
        for (player in Bukkit.getOnlinePlayers()) {
            val holder: InventoryHolder = player.openInventory.topInventory.holder ?: continue
            if (holder is CustomCrafterUI) {
                holder.onClose(
                    InventoryCloseEvent(player.openInventory)
                )
                player.closeInventory()
            }
        }
        EXECUTOR.shutdown()
    }

    fun warn(str: String) = CustomCrafter.getInstance().logger.warning(str)
    fun info(str: String) = CustomCrafter.getInstance().logger.info(str)

    fun asyncExecutor(): ExecutorService {
        if (!::EXECUTOR.isInitialized) {
            EXECUTOR = Executors.newVirtualThreadPerTaskExecutor()
        }
        return EXECUTOR
    }

    /**
     * @suppress
     * CustomCrafterAPI default async executor
     */
    private lateinit var EXECUTOR: ExecutorService

    fun scheduler(server: Server = Bukkit.getServer()): BukkitScheduler {
        if (!::SCHEDULER.isInitialized) {
            SCHEDULER = server.scheduler
        }
        return SCHEDULER
    }

    private lateinit var SCHEDULER: BukkitScheduler
}
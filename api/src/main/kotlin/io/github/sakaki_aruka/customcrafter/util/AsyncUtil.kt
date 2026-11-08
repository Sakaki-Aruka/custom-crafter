package io.github.sakaki_aruka.customcrafter.util

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import org.bukkit.Location
import org.bukkit.entity.Entity

/**
 * Utility for easily invoking synchronous operations from asynchronous contexts.
 *
 * Wrap of FolibLib functions
 *
 * @see[CustomCrafterAPI.getAsyncAPIInstance]
 * @since 5.5.0
 */
object AsyncUtil {
    /**
     * Run function on next tick.
     * (Wait for finish a given processing.)
     *
     * @param[lambda] function
     * @since 5.5.0
     */
    fun runNextTick(lambda: Runnable) {
        CustomCrafterAPI.getAsyncAPIInstance().scheduler.runNextTick { lambda.run() }.get()
    }

    /**
     * Run function at specified location.
     * (Wait for finish a given processing.)
     *
     * @param[location] Where to run
     * @param[lambda] function
     * @since 5.5.0
     */
    fun runAt(location: Location, lambda: Runnable) {
        CustomCrafterAPI.getAsyncAPIInstance().scheduler.runAtLocation(location) { lambda.run() }.get()
    }

    /**
     * Run function as specified entity.
     * (Wait for finish a given processing.)
     *
     * @param[entity] Run with
     * @param[lambda] function
     * @since 5.5.0
     */
    fun runAs(entity: Entity, lambda: Runnable) {
        CustomCrafterAPI.getAsyncAPIInstance().scheduler.runAtEntity(entity) { lambda.run() }.get()
    }
}
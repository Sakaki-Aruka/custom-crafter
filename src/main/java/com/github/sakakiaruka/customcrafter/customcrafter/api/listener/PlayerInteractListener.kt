package com.github.sakakiaruka.customcrafter.customcrafter.api.listener

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * @suppress
 */
object PlayerInteractListener: Listener {
    @EventHandler
    fun PlayerInteractEvent.onInteract() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        val clicked: Block = clickedBlock.takeIf { clickedBlock?.type == Material.CRAFTING_TABLE } ?: return

        val x: Int = clicked.x
        val y: Int = clicked.y
        val z: Int = clicked.z
        val world: World = clicked.world
        val halfSideSize: Int = CustomCrafterAPI.BASE_BLOCK_SIDE / 2
        val range: IntRange = (-1 * halfSideSize..halfSideSize)
        for (dx: Int in range) {
            for (dz: Int in range) {
                if (world.getBlockAt(x + dx, y, z + dz).type != CustomCrafterAPI.BASE_BLOCK) return
            }
        }

        isCancelled = true
        player.openInventory(CustomCrafterAPI.getCraftingGUI())
    }
}
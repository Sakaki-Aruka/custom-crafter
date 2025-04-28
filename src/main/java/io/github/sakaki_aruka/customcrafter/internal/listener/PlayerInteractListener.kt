package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.SlotsModifyGUI
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory

/**
 * @suppress
 */
object PlayerInteractListener: Listener {
    @EventHandler
    fun PlayerInteractEvent.onInteract() {

        val inv: Inventory = PageOpenTrigger.getGUI(this)
            ?.takeIf { gui -> gui is PageOpenTrigger }
            ?.let { gui -> (gui as PageOpenTrigger).getFirstPage(this) }
            ?: return
        isCancelled = true
        player.openInventory(inv)

//        val inv: Inventory = (instance as PageOpenTrigger).getFirstPage(this) ?: return
//        player.openInventory(inv)

//        if (action == Action.RIGHT_CLICK_BLOCK
//            && clickedBlock?.type == Material.CRAFTING_TABLE) {
//            playerCrafting(this)
//        }
    }

    private fun playerCrafting(
        event: PlayerInteractEvent
    ) {
        val clicked: Block = event.clickedBlock!!
//        val x: Int = clicked.x
//        val y: Int = clicked.y
//        val z: Int = clicked.z
//        val world: World = clicked.world
//        val halfSideSize: Int = CustomCrafterAPI.BASE_BLOCK_SIDE / 2
//        val range: IntRange = (-1 * halfSideSize..halfSideSize)
//        for (dx: Int in range) {
//            for (dz: Int in range) {
//                if (world.getBlockAt(x + dx, y - 1, z + dz).type != CustomCrafterAPI.BASE_BLOCK) return
//            }
//        }
        if (!baseBlockCheck(
            size = CustomCrafterAPI.BASE_BLOCK_SIDE,
            types = setOf(CustomCrafterAPI.BASE_BLOCK),
            block = clicked,
            ignoreCenter = false
        )) return

        event.isCancelled = true
        event.player.openInventory(CustomCrafterAPI.getCraftingGUI(dropItemsOnClose = true))
    }

    private fun baseBlockCheck(
        size: Int,
        types: Set<Material>,
        block: Block,
        ignoreCenter: Boolean,
        yRange: IntRange = -1..-1
    ): Boolean {
        val half: Int = size / 2
        val range: IntRange = (-1 * half..half)
        for (dy: Int in yRange) {
            for (dx: Int in range) {
                for (dz: Int in range) {
                    if (ignoreCenter && dx == 0 && dz == 0) continue
                    if (block.world.getBlockAt(block.x + dx, block.y + dy, block.z + dz).type !in types) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
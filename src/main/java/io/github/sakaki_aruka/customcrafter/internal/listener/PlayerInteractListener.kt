package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import kotlin.reflect.full.allSuperclasses

/**
 * @suppress
 */
object PlayerInteractListener: Listener {
    @EventHandler
    fun PlayerInteractEvent.onInteract() {

        // debug
//        val instance: CustomCrafterGUI = CustomCrafterGUI.PAGES.entries
//            .filter { (_, clazz) -> clazz.allSuperclasses.contains(PageOpenTrigger::class) }
//            .firstNotNullOfOrNull { (_, clazz) ->
//                (clazz as PageOpenTrigger).predicate(this)
//            } ?: return
        val instance: CustomCrafterGUI = PageOpenTrigger.getGUI(this) ?: return

        val inv: Inventory = (instance as PageOpenTrigger).getFirstPage(this) ?: return
        player.openInventory(inv)

        if (action == Action.RIGHT_CLICK_BLOCK
            && clickedBlock?.type == Material.CRAFTING_TABLE) {
            playerCrafting(this)
        }
//        else if (action == Action.RIGHT_CLICK_BLOCK
//            && clickedBlock?.type in InternalAPI.AUTO_CRAFTING_BLOCKS
//            && useInteractedBlock() == Event.Result.ALLOW) {
//            autoCrafting(this)
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

    private fun autoCrafting(
        event: PlayerInteractEvent
    ) {
        val clicked: Block = event.clickedBlock!!
        if (!baseBlockCheck(
            size = InternalAPI.AUTO_CRAFTING_BASE_BLOCK_SIDE,
            types = setOf(CustomCrafterAPI.getAutoCraftingBaseBlock()),
            block = clicked,
            ignoreCenter = true
        )) return

//        val cBlock: CBlock = CBlock.fromBlock(clicked)
//            ?.let { b ->
//                b.takeIf { block ->
//                    block.recipes.isNotEmpty()
//                }
//            } ?: return
//
//        val recipe: CRecipe = cBlock.getRecipes(CustomCrafterAPI.AUTO_CRAFTING_SOURCE_RECIPES_PROVIDER(event))
//            .let { recipes ->
//                if (recipes.size == 1) recipes.first()
//                else CustomCrafterAPI.AUTO_CRAFTING_PICKUP_RESOLVER(recipes) ?: return
//            }
//

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
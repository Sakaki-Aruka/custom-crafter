package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.listener.NoPlayerListener
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Crafter
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.ItemStack

object AutoCraft {
    object AutoCraftItemInputSignalReceiver: NoPlayerListener {
        override fun <T : Event> func(event: T) {
            if (event !is InventoryMoveItemEvent) return
            else if (event.isCancelled) return
            else if (event.destination.holder == null) return

            val crafter: Crafter = event.destination.holder!! as? Crafter ?: return
            val cBlock: CBlock = CBlock.of(crafter) ?: return

            if (!cBlock.addItems(event.item)) {
                crafter.block.world.dropItem(
                    crafter.block.getRelative(BlockFace.DOWN, 1).location,
                    event.item
                )
            }
            event.item = ItemStack.empty()
        }

        override fun <T : Event> predicate(event: T): Boolean? {
            return event is InventoryMoveItemEvent
                    && event.destination.holder is Crafter
                    && baseBlockCheck((event.destination.holder as Crafter).block)
                    && CustomCrafterAPI.getUseAutoCraftingFeature()
        }
    }

    internal fun baseBlockCheck(crafter: Block): Boolean {
        val crafterLoc: Location = crafter.location
        val crafterWorld: World = crafter.world
        val underCenter = Location(crafterWorld, crafterLoc.x, crafterLoc.y - 1, crafterLoc.z)
        if (crafterWorld.getBlockAt(underCenter).type != Material.AIR) return false
        val half: Int = InternalAPI.AUTO_CRAFTING_BASE_BLOCK_SIDE / 2
        for (dz in (-half..half)) {
            for (dx in (-half..half)) {
                if (dx == 0 && dz == 0) continue
                val loc = Location(crafterWorld, underCenter.x + dx, underCenter.y, underCenter.z + dz)
                if (crafterWorld.getBlockAt(loc).type != CustomCrafterAPI.getAutoCraftingBaseBlock()) return false
            }
        }
        return true
    }
}
package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.ContainedItemsUI
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.ItemStack

object InventoryMoveItemListener: Listener {
    @EventHandler
    fun InventoryMoveItemEvent.onMove() {
        if (!CustomCrafterAPI.getUseAutoCraftingFeature()) return
        else if (this.isCancelled) return
        else if (this.item.isEmpty) return
        else if (this.destination.location == null) return
        else if (this.destination.location!!.block.type != Material.CRAFTER) return

        val crafter: Crafter = this.destination.location!!.block.state as? Crafter ?: return
        if (!CBlock.hasEssentialKeys(crafter)) return
        else if (!baseBlockCheck(crafter.block)) return

        val cBlock: CBlock = CBlock.of(crafter) ?: return

        (0..<9).forEach { index ->
            crafter.setSlotDisabled(index, false)
        }
        crafter.update()

        if (cBlock.isItemModifyCacheModeEnabled()) {
            val ui = ContainedItemsUI.of(cBlock, createNewIfNotExist = false) ?: return
            ui.merge(this.item).takeUnless { i -> i.isEmpty }?.let { i -> this.item = i }
            return
        }

        if (!cBlock.addItems(this.item)) {
            this.isCancelled = true
            return
        }
        this.item = ItemStack.empty()
    }

    private fun baseBlockCheck(crafter: Block): Boolean {
        val crafterLoc: Location = crafter.location
        val crafterWorld: World = crafter.world
        val underCenter = Location(crafterWorld, crafterLoc.x, crafterLoc.y - 1, crafterLoc.z)
        if (!crafterWorld.getBlockAt(underCenter).type.isEmpty) return false
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
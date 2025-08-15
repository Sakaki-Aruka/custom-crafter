package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent

object BlockBreakEventListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBreak() {
        if (this.isCancelled) return
        getAutoCraftingTables(this.block)
            .forEach { c -> CBlockDB.unlink(c.block) }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockExplodeEvent.onExplode() {
        if (this.isCancelled) return
        getAutoCraftingTables(*this.blockList().toTypedArray())
            .forEach { c -> CBlockDB.unlink(c.block) }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityExplodeEvent.onExplode() {
        if (this.isCancelled) return
        getAutoCraftingTables(*this.blockList().toTypedArray())
            .forEach { c -> CBlockDB.unlink(c.block) }
    }

    private fun getAutoCraftingTables(vararg candidates: Block): List<CBlock> {
        return candidates
            .filter { block -> block.type == Material.CRAFTER }
            .filter { block -> block.state is Crafter }
            .map { block -> block.state as Crafter }
            .filter { crafter -> CBlock.hasEssentialKeys(crafter) }
            .mapNotNull { crafter -> CBlock.of(crafter) }
    }
}
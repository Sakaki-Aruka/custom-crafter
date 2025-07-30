package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.giveItems
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

object PlayerBreakBlockListener: Listener {
    @EventHandler
    fun BlockBreakEvent.on() {
        if (block.type !in InternalAPI.AUTO_CRAFTING_BLOCKS) return
        val cBlock: CBlock = CBlock.fromBlock(block) ?: return
        val containedItems: List<ItemStack> = cBlock.containedItems.values.toList()
        player.giveItems(true, *containedItems.toTypedArray())
        object: BukkitRunnable() {
            override fun run() {
                CBlockDB.allDelete(block)
            }
        }.runTaskAsynchronously(CustomCrafter.getInstance())
    }
}
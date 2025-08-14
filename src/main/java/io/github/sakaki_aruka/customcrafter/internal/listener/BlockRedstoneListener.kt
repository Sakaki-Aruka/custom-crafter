package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.event.AutoCraftPowerOnEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent

object BlockRedstoneListener: Listener {
    @EventHandler
    fun BlockRedstoneEvent.onSignal() {
        if (!CustomCrafterAPI.getUseAutoCraftingFeature()) return
        else if (this.oldCurrent != 0 && this.newCurrent != 0) return

        //var crafter: Crafter? = null // state
        for (dx in (-1..1)) {
            for (dy in (-1..1)) {
                for (dz in (-1..1)) {
                    if (dx + dy + dz == 0) continue
                    val b: Block = this.block.getRelative(dx, dy, dz)
                    if (b.type != Material.CRAFTER) continue
                    else if (b.state !is Crafter) continue
                    else if (!baseBlockCheck(b)) continue
                    else if (!CBlockDB.isLinked(b)) continue
                    AutoCraftPowerOnEvent(b.state as Crafter).callEvent()
                    return
                }
            }
        }
    }

    private fun baseBlockCheck(crafter: Block): Boolean {
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
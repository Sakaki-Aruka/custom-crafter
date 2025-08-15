package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.internal.event.AutoCraftPowerOnEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent

object BlockPhysicsListener: Listener {

    /**
     * @suppress
     * A block what is not contained this, means not called before.
     */
    internal val STATE_CACHE: MutableMap<Location, CrafterSignalState> = mutableMapOf()

    internal class CrafterSignalState(
        val location: Location,
        val isTriggered: Boolean,
        val updatedUnixEpochMill: Long
    )

    @EventHandler
    fun BlockPhysicsEvent.onPhysics() {
        if (this.block.type != Material.CRAFTER) {
            return
        }

        val crafterBlock: org.bukkit.block.Crafter = this.block.state as? org.bukkit.block.Crafter ?: return
        val crafterType: org.bukkit.block.data.type.Crafter = this.block.blockData as? org.bukkit.block.data.type.Crafter ?: return

        STATE_CACHE[this.block.location]?.let { state ->
            if (crafterType.isTriggered
                && state.updatedUnixEpochMill <= System.currentTimeMillis()
                && crafterType.isTriggered != state.isTriggered)
            {
                AutoCraftPowerOnEvent(crafterBlock).callEvent()
            }
        }

        STATE_CACHE[this.block.location] = CrafterSignalState(
            this.block.location,
            (this.block.blockData as org.bukkit.block.data.type.Crafter).isTriggered,
            System.currentTimeMillis()
        )

    }
}
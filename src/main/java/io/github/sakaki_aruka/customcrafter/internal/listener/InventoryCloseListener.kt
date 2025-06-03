package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * @suppress
 */
object InventoryCloseListener: Listener {

    internal val ALL_CANDIDATE_DO_NOT_DROP_MARKED_NK = NamespacedKey(
        CustomCrafter.getInstance(), "all_candidate_do_not_drop_marked")

    @EventHandler
    fun InventoryCloseEvent.onClose() {
        CustomCrafterGUI.getGUI(this.inventory)?.onClose(this)
    }

    internal fun deleteNoDropMarker(item: ItemStack) {
        item.editMeta { meta ->
            meta.persistentDataContainer.remove(ALL_CANDIDATE_DO_NOT_DROP_MARKED_NK)
        }
    }

    internal fun setNoDropMarker(item: ItemStack) {
        item.editMeta { meta ->
            meta.persistentDataContainer.set(
                ALL_CANDIDATE_DO_NOT_DROP_MARKED_NK,
                PersistentDataType.STRING,
                ""
            )
        }
    }

    private fun containsNoDropMarker(inventory: Inventory): Boolean {
        return inventory.contents
            .filterNotNull()
            .any { item ->
                item.itemMeta.persistentDataContainer.has(
                    ALL_CANDIDATE_DO_NOT_DROP_MARKED_NK,
                    PersistentDataType.STRING
                )
            }
    }
}
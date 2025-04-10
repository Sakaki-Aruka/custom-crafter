package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.internal.processor.Converter
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
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
        if (CustomCrafterAPI.isCustomCrafterGUI(inventory)) craftingGUI(this)
        else if (CustomCrafterAPI.isAllCandidatesPageGUI(inventory)) allCandidateGUI(this)
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

    private fun allCandidateGUI(event: InventoryCloseEvent) {
        if (containsNoDropMarker(event.inventory)) return
        val player = event.player
        val signature: ItemStack = event.inventory.getItem(CustomCrafterAPI.ALL_CANDIDATE_SIGNATURE_SLOT)
            ?: throw NoSuchElementException("Couldn't find a signature item.")
        val view: CraftView = signature.itemMeta.persistentDataContainer.get(
            CustomCrafterAPI.ALL_CANDIDATE_INPUT_NK,
            PersistentDataType.STRING
        )?.let { json -> CraftView.fromJson(json) }
            ?: throw NoSuchElementException("Couldn't find a view's json.")
        player.inventory.addItem(*view.materials.values.toTypedArray()).forEach { (_, over) ->
            player.world.dropItem(player.location, over)
        }
    }

    private fun craftingGUI(event: InventoryCloseEvent) {
        if (containsNoDropMarker(event.inventory)) return
        val player = event.player
        val world: World = player.world
        val location: Location = player.location
        Converter.getAvailableCraftingSlotIndices().forEach { slot ->
            event.inventory.getItem(slot)?.let { item ->
                player.inventory.addItem(item).forEach { (_, over) ->
                    world.dropItem(location, over)
                }
            }
        }
        event.inventory.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT)?.let { result ->
            player.inventory.addItem(result).forEach { (_, over) ->
                player.world.dropItem(player.location, over)
            }
        }
    }
}
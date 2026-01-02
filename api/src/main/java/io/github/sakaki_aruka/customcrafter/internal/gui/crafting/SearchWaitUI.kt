package io.github.sakaki_aruka.customcrafter.internal.gui.crafting

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import java.util.function.Consumer

class SearchWaitUI(
    private val view: CraftView
): InventoryHolder, CustomCrafterUI {
    private val inventory: Inventory = Bukkit.createInventory(this, 27, "Just a minute".toComponent())

    init {
        (0..<27).forEach { index -> this.inventory.setItem(index, blank) }
        this.inventory.setItem(searchingIconSlot, searchingIcon)
        this.inventory.setItem(backToCraftSlot, backToCrafting)
    }

    companion object {
        private const val searchingIconSlot = 13
        private const val backToCraftSlot = 18
        private val blank = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.empty())
                persistentDataContainer.set(
                    NamespacedKey("custom_crafter", UUID.randomUUID().toString()),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString()
                )
            }
        }

        private val searchingIcon = ItemStack.of(Material.BOOK).apply {
            itemMeta = itemMeta.apply {
                displayName("<green>Now Searching...".toComponent())
                persistentDataContainer.set(
                    NamespacedKey("custom_crafter", UUID.randomUUID().toString()),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString()
                )
            }
        }

        private val backToCrafting = ItemStack.of(Material.CRAFTING_TABLE).apply {
            itemMeta = itemMeta.apply {
                displayName("Back to Craft".toComponent())
                persistentDataContainer.set(
                    NamespacedKey("custom_crafter", UUID.randomUUID().toString()),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString()
                )
            }
        }
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        event.isCancelled = true
        if (event.slot == backToCraftSlot) {
            backToCraft(event.whoClicked)
        }
    }

    private fun backToCraft(player: HumanEntity) {
        val ui = (player as? Player)?.let { CraftUI(caller = player) } ?: return
        val craftSlots: List<CoordinateComponent> = ui.bakedDesigner.craftSlots().sortedBy { it.toIndex() }
        val sortedView: List<CoordinateComponent> = this.view.materials.keys.sortedBy { it.toIndex() }

        val dx: Int = craftSlots.first().x - sortedView.first().x
        val dy: Int = craftSlots.first().y - sortedView.first().y

        sortedView.forEach { c ->
            val destination = CoordinateComponent(c.x + dx, c.y + dy)
            ui.inventory.setItem(destination.toIndex(), this.view.materials.getValue(c))
        }

        Bukkit.getScheduler().runTask(CustomCrafter.getInstance(), Consumer {
            player.openInventory(ui.inventory)
        })
    }

    override fun onClose(event: InventoryCloseEvent) {
        backToCraft(event.player)
    }

    override fun getInventory(): Inventory = this.inventory
}
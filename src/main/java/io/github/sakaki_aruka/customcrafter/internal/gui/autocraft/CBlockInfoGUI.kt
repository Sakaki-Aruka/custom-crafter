package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import io.github.sakaki_aruka.customcrafter.internal.gui.PredicateProvider
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

internal data class CBlockInfoGUI(
    override val id: String = CustomCrafterGUI.getID(CBlockInfoGUI::class).toString(),
    override val contextComponentSlot: Int = 45
): CustomCrafterGUI.UnPageableGUI, PageOpenTrigger {
    companion object: PredicateProvider<CustomCrafterGUI> {
        override fun <T : Event> predicate(event: T): CustomCrafterGUI? {
            TODO("Not yet implemented")
        }
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        return contextItem
    }

    override fun from(contextItem: ItemStack): CustomCrafterGUI? {
        return null
    }

    override fun eventReaction(
        event: InventoryClickEvent,
        ui: CustomCrafterGUI,
        inventory: Inventory
    ) {
        TODO("Not yet implemented")
    }

    override fun <T : Event> getFirstPage(event: T): Inventory? {
        TODO("Not yet implemented")
    }
}

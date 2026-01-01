package io.github.sakaki_aruka.customcrafter.internal.gui.crafting

import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class SearchWaitUI(): InventoryHolder, CustomCrafterUI {
    private val inventory: Inventory = Bukkit.createInventory(this, 27, "Just a minute".toComponent())

    init {
        inventory.setItem(i, ItemStack.empty())
    }

    companion object {
        private val i = 1
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        //
    }

    override fun getInventory(): Inventory = this.inventory
}
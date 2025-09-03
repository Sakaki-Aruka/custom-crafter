package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.Collections
import java.util.UUID
import kotlin.math.min

internal class ContainedItemsUI private constructor(
    val placeableSlots: Set<Int>,
    private val location: Location
): CustomCrafterUI, InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(
        this,
        54,
        "<b>Contained Items".toComponent()
    )

    init {
        ((0..<54) - placeableSlots).forEach { i ->
            this.inventory.setItem(i, BLANK)
        }
    }

    companion object {
        val BLANK: ItemStack = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.empty())
                // An additional datum to prevent accidental menu operation
                persistentDataContainer.set(
                    NamespacedKey("custom_crafter", UUID.randomUUID().toString()),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString()
                )
            }
        }
        private val INV_CACHE: MutableMap<Location, ContainedItemsUI> = Collections.synchronizedMap<Location, ContainedItemsUI>(mutableMapOf())

        fun of(cBlock: CBlock, createNewIfNotExist: Boolean = true): ContainedItemsUI? {
            return synchronized(INV_CACHE) {
                INV_CACHE[cBlock.block.location]?.let { ui -> return@synchronized ui }

                if (!createNewIfNotExist) {
                    return null
                }

                val recipe: AutoCraftRecipe = cBlock.getRecipe() ?: run {
                    throw IllegalArgumentException("Could not get a cBlock-contained recipe. 'cBlock#getRecipe' returns null.")
                }

                val placeableSlots: Set<Int> = recipe.items.keys.map { c -> c.toIndex() }.toSet()
                val ui = ContainedItemsUI(placeableSlots, cBlock.block.location)
                INV_CACHE[cBlock.block.location] = ui
                ui
            }
        }

        fun contains(location: Location): Boolean {
            return synchronized(INV_CACHE) {
                INV_CACHE.keys.contains(location)
            }
        }

        fun clear() {
            synchronized(INV_CACHE) {
                INV_CACHE.clear()
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        if ((event.inventory.viewers - event.player).isEmpty()) {
            synchronized(INV_CACHE) {
                INV_CACHE.remove(this.location)
            }

            if (CBlockDB.isLinked(this.location.block)) {
                CBlockDB.clearContainedItems(this.location.block)
                CBlockDB.addItems(this.location.block, *getNoBlankItems().toTypedArray())
            }
        }
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        if (event.isCancelled) {
            return
        } else if (event.rawSlot !in this.placeableSlots) {
            event.isCancelled = true
            return
        }
    }


    override fun onPlayerInventoryClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        if (event.isCancelled
            || event.action != InventoryAction.MOVE_TO_OTHER_INVENTORY
            || event.currentItem == null
            || event.currentItem!!.isEmpty) {
            return
        }

        event.isCancelled = true
        event.currentItem = merge(event.currentItem!!)
    }

    /**
     * @suppress
     * returns = Remaining Item
      */
    fun merge(item: ItemStack): ItemStack {
        val sourceCloned: ItemStack = item.clone()

        for (slot in Converter.getAvailableCraftingSlotIndices()) {
            val slotCloned: ItemStack = this.inventory.getItem(slot) ?: run {
                this.inventory.setItem(slot, sourceCloned)
                return ItemStack.empty()
            }

            if (!slotCloned.asOne().isSimilar(sourceCloned)) {
                continue
            }

            if (slotCloned.amount >= slotCloned.maxStackSize) {
                continue
            }

            val canAddAmount: Int = slotCloned.maxStackSize - slotCloned.amount
            val addAmount: Int = min(canAddAmount, sourceCloned.amount)
            if (addAmount <= 0) {
                continue
            }
            this.inventory.setItem(slot, slotCloned.asQuantity(slotCloned.amount + addAmount))
            sourceCloned.amount -= addAmount
            if (sourceCloned.amount <= 0) {
                return ItemStack.empty()
            }
        }

        return sourceCloned.takeIf { i -> !i.isEmpty && i.amount > 0 } ?: ItemStack.empty()
    }

    fun getNoBlankItems(): List<ItemStack> {
        return this.placeableSlots.sorted().map { i ->
            this.inventory.getItem(i)?.clone() ?: ItemStack.empty()
        }
    }

    override fun getInventory(): Inventory = inventory
}
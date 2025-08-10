package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

// Only for Check, Delete.
// Not for CREATE, MODIFY.
internal class AutoCraftUI(
    private val block: Block,
    private val player: Player,
): CustomCrafterUI.Static, InventoryHolder {

    private val inventory: Inventory = Bukkit.createInventory(
        this,
        9,
        "<aqua><b><u>Auto Craft".toComponent()
    )

    init {
        this.inventory.setItem(0, ItemStack.of(Material.SHEARS).apply {
            itemMeta = itemMeta.apply {
                displayName("Delete AutoCraft Settings".toComponent())
            }
        })

        CBlock.of(block.state as Crafter)?.let { cBlock ->
            this.inventory.setItem(
                4,
                cBlock.getRecipe()?.autoCraftDisplayItemProvider(player)
                    ?: UNDEFINED
            )
        } ?: run {
            this.inventory.setItem(4, UNDEFINED)
        }
    }

    companion object {
        val UNDEFINED: ItemStack = ItemStack.of(Material.BARRIER).apply {
            itemMeta = itemMeta.apply {
                displayName("<red><b><u>RECIPE UNDEFINED".toComponent())
                persistentDataContainer.set(
                    NamespacedKey(CustomCrafter.getInstance(), "auto_craft_recipe_undefined"),
                    PersistentDataType.STRING,
                    ""
                )
            }
        }

        // TODO: impl InteractTriggered
    }

    override fun getClickableType(slot: Int): CustomCrafterUI.ClickableType {
        return when (slot) {
            0, 4 -> CustomCrafterUI.ClickableType.DYNAMIC_TOGGLED
            else -> CustomCrafterUI.ClickableType.ALWAYS_UNCLICKABLE
        }
    }

    override fun onClick(clicked: Inventory, event: InventoryClickEvent) {
        event.isCancelled = true
        when (event.rawSlot) {
            0 -> {
                if (!CBlockDB.isLinked(this.block)) {
                    return
                }

                CBlockDB.unlink(block)
                this.inventory.setItem(4, UNDEFINED)
            }

            4 -> {
                val item: ItemStack = event.currentItem ?: return
                if (!item.isSimilar(UNDEFINED)) {
                    return
                }

                val recipeSetUI: Inventory = RecipeSetUI(this.block, this.player).inventory
                if (recipeSetUI.isEmpty) {
                    return
                }

                this.player.openInventory(recipeSetUI)
            }
        }
    }

    override fun getInventory(): Inventory = inventory
}
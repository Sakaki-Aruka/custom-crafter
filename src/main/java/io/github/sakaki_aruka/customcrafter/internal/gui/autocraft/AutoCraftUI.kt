package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

// Only for Check, Delete.
// Not for CREATE, MODIFY.
internal class AutoCraftUI(
    cBlock: CBlock,
    private val player: Player
): CustomCrafterUI.Static, InventoryHolder {

    private val block: Block = cBlock.block
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

        this.inventory.setItem(
            4,
            cBlock.getRecipe()?.autoCraftDisplayItemProvider(this.player, this.block)
                ?: UNDEFINED
        )
    }

    companion object: CustomCrafterUI.InteractTriggered {
        val UNDEFINED: ItemStack = ItemStack.of(Material.BARRIER).apply {
            itemMeta = itemMeta.apply {
                displayName("<red><b><u>RECIPE UNDEFINED".toComponent())
                lore(listOf("<red><b>UNDEFINED".toComponent()))
            }
        }

        override fun isTrigger(event: PlayerInteractEvent): Boolean {
            /*
             * xxx
             * x x
             * xxx
             * (in default)
             */
            val clicked: Block = event.clickedBlock?.takeIf { b ->
                b.type == Material.CRAFTER
                        && event.action.isRightClick
                        && (event.item == null || event.item!!.type != Material.HOPPER)
            } ?: return false
            val underCenter: Block = clicked.getRelative(0, -1, 0)
            val half: Int = InternalAPI.AUTO_CRAFTING_BASE_BLOCK_SIDE / 2
            for (dx in (-half..half)) {
                for (dz in (-half..half)) {
                    if (dx == 0 && dz == 0) {
                        continue
                    } else if (underCenter.getRelative(dx, 0, dz).type != CustomCrafterAPI.getAutoCraftingBaseBlock()) {
                        return false
                    }
                }
            }
            return true
        }

        override fun open(event: PlayerInteractEvent) {
            event.isCancelled = true
            of(event.clickedBlock!!, event.player)?.let { ui ->
                event.player.openInventory(ui.inventory)
            }
        }

        fun of(block: Block, player: Player): AutoCraftUI? {
            if (block.type != Material.CRAFTER || block.state !is Crafter) {
                return null
            } else if (!CBlockDB.isLinked(block)) {
                return null
            }

            val cBlock: CBlock = CBlock.of(block.state as Crafter) ?: return null
            return AutoCraftUI(cBlock, player)
        }
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
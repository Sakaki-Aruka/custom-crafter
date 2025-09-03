package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
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
import org.jetbrains.annotations.TestOnly

// Only for Check, Delete.
// Not for CREATE, MODIFY.
internal class AutoCraftUI private constructor(
    private val block: Block,
    private val player: Player
): CustomCrafterUI.Static, InventoryHolder {

    private val inventory: Inventory = Bukkit.createInventory(
        this,
        9,
        "<aqua><b><u>Auto Craft".toComponent()
    )

    init {
        this.inventory.setItem(
            RESET,
            ItemStack.of(Material.SHEARS).apply {
                itemMeta = itemMeta.apply {
                    displayName("Delete AutoCraft Settings".toComponent())
                }
            }
        )
    }

    companion object: CustomCrafterUI.InteractTriggered {

        const val RESET = 0
        const val SET = 4
        const val CONTAINED_ITEMS = 8

        val UNDEFINED: ItemStack = ItemStack.of(Material.BARRIER).apply {
            itemMeta = itemMeta.apply {
                displayName("<red><b><u>RECIPE UNDEFINED".toComponent())
                lore(listOf("<red><b>UNDEFINED".toComponent()))
            }
        }

        val NOT_FOUND: ItemStack = ItemStack.of(Material.COMMAND_BLOCK).apply {
            itemMeta = itemMeta.apply {
                displayName("<red><b><u>RECIPE NOT FOUND".toComponent())
                lore(listOf("<red><b>NOT FOUND".toComponent()))
            }
        }

        val CONTAINED_ITEM: ItemStack = ItemStack.of(Material.CHEST).apply {
            itemMeta = itemMeta.apply {
                displayName("<b>CONTAINED ITEMS".toComponent())
                lore(listOf("<aqua><b>CONTAINED ITEMS".toComponent()))
            }
        }

        @TestOnly
        fun of(cBlock: CBlock, player: Player): AutoCraftUI {
            val ui = AutoCraftUI(cBlock.block, player)
            ui.inventory.setItem(
                SET,
                cBlock.getRecipe()?.let { recipe ->
                    recipe.autoCraftDisplayItemProvider(player, cBlock.block)
                        .takeIf { item -> !item.isEmpty && item.type.isItem }
                        ?: AutoCraftRecipe.getDefaultDisplayItemProvider(recipe.name)(player, cBlock.block)
                } ?: NOT_FOUND
            )

            return ui
        }

        fun of(block: Block, player: Player): AutoCraftUI {
            val ui = AutoCraftUI(block, player)
            val cBlock: CBlock? = CBlock.of(block.state as Crafter)
            val setSlotItem: ItemStack = cBlock?.let { c ->
                c.getRecipe()?.let { recipe ->
                    recipe.autoCraftDisplayItemProvider(player, c.block)
                        .takeIf { item -> !item.isEmpty && item.type.isItem }
                        ?: AutoCraftRecipe.getDefaultDisplayItemProvider(recipe.name)(player, c.block)
                } ?: NOT_FOUND
            } ?: UNDEFINED
            ui.inventory.setItem(SET, setSlotItem)
            val containedSlotItem: ItemStack =
                if (!setSlotItem.isSimilar(NOT_FOUND) && !setSlotItem.isSimilar(UNDEFINED)) {
                    CONTAINED_ITEM
                } else {
                    ItemStack.empty()
                }
            ui.inventory.setItem(CONTAINED_ITEMS, containedSlotItem)

            return ui
        }

        override fun isTrigger(event: PlayerInteractEvent): Boolean {
            if (!CustomCrafterAPI.getUseAutoCraftingFeature()) {
                return false
            }
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
            event.player.openInventory(of(event.clickedBlock!!, event.player).inventory)
        }
    }

    override fun getClickableType(slot: Int): CustomCrafterUI.ClickableType {
        return when (slot) {
            RESET, SET, CONTAINED_ITEMS -> CustomCrafterUI.ClickableType.DYNAMIC_TOGGLED
            else -> CustomCrafterUI.ClickableType.ALWAYS_UNCLICKABLE
        }
    }

    override fun onClick(clicked: Inventory, event: InventoryClickEvent) {
        event.isCancelled = true
        when (event.rawSlot) {
            RESET -> {
                if (!CBlockDB.isLinked(this.block)) {
                    return
                }

                CBlockDB.unlink(block)
                this.inventory.setItem(4, UNDEFINED)
            }

            SET -> {

                //debug
                println("event.currentItem=${event.currentItem}")
                println("similar undefined=${event.currentItem?.isSimilar(UNDEFINED)}")

                val item: ItemStack = event.currentItem ?: return
                if (!item.isSimilar(UNDEFINED) || item.type.isEmpty) {
                    return
                }

                //debug
                println("RecipeSetUI=${RecipeSetUI(this.block, this.player)}")
                println("Inv.isEmpty=${RecipeSetUI(this.block, this.player).inventory.isEmpty}")

                val recipeSetUI: Inventory = RecipeSetUI(this.block, this.player).inventory
                if (recipeSetUI.isEmpty) {
                    return
                }

                this.player.openInventory(recipeSetUI)
            }

            CONTAINED_ITEMS -> {
                val setSlotItem: ItemStack = this.inventory.getItem(SET) ?: return
                if (setSlotItem.isSimilar(NOT_FOUND)) {
                    return
                } else if (setSlotItem.isSimilar(UNDEFINED)) {
                    return
                } else if (!CBlockDB.isLinked(this.block)) {
                    return
                }

                if (event.currentItem?.let{ item -> !item.isSimilar(CONTAINED_ITEM) } ?: true) {
                    return
                }
                val cBlock: CBlock = CBlock.of(this.block.state as? Crafter ?: return) ?: return
                val ui = ContainedItemsUI.of(cBlock) ?: return
                this.player.openInventory(ui.inventory)
            }
        }
    }

    override fun getInventory(): Inventory = inventory
}
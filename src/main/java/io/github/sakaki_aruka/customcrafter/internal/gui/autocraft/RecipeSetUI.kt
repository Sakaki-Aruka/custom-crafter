package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

internal class RecipeSetUI(
    private val block: Block,
    private val player: Player
): CustomCrafterUI.Pageable, InventoryHolder {

    private val inventory: Inventory = Bukkit.createInventory(
        this,
        54,
        "<u><b>Auto Craft Recipe Set".toComponent()
    )

    override var currentPage: Int = 0

    // Map<PageNumber, Map<SlotNumber, Pair<DisplayItem, Recipe>>>
    private val elements: Map<Int, Map<Int, Pair<ItemStack, AutoCraftRecipe>>>

    init {
        val autoRecipes: List<AutoCraftRecipe> = CustomCrafterAPI.AUTO_CRAFTING_SETTING_PAGE_SUGGESTION(this.block, this.player)
            .sortedBy { recipe -> recipe.name }

        val e: List<Pair<ItemStack, AutoCraftRecipe>> = autoRecipes.map { recipe ->
            Pair(
                recipe.autoCraftDisplayItemProvider(this.player, this.block)
                    .takeIf { i -> !i.isEmpty && i.type.isItem }
                    ?: AutoCraftUI.UNDEFINED,
                recipe
            )
        }

        this.elements = e.chunked(45)
            .withIndex()
            .associate { (index, list) -> index to list }
            .map { (index, list) ->
                index to list.withIndex().associate { (i, l) -> i to l }
            }.toMap()
        this.elements[0]?.let { page ->
            (0..<45).forEach { index ->
                val displayItem: ItemStack = page[index]?.first ?: AutoCraftUI.UNDEFINED
                this.inventory.setItem(index, displayItem)
            }
        }
        if (this.elements.size > 1) {
            this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
        }
    }

    companion object {
        const val NEXT = 53
        const val PREVIOUS = 45
        const val BACK_TO_AUTO_CRAFT = 49
    }

    override fun flipPage() {
        if (!canFlipPage()) return
        this.currentPage++
        val items: Map<Int, Pair<ItemStack, AutoCraftRecipe>> = this.elements[this.currentPage] ?: return
        (0..<this.inventory.size).forEach { index ->
            this.inventory.setItem(
                index,
                items[index]?.first ?: ItemStack.empty()
            )
        }
        this.inventory.setItem(PREVIOUS, CustomCrafterUI.PREVIOUS_BUTTON)
        if (canFlipPage()) {
            this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
        }
    }

    override fun canFlipPage(): Boolean {
        return this.currentPage < this.elements.size - 1
    }

    override fun flipBackPage() {
        if (!canFlipBackPage()) return
        this.currentPage--
        val items: Map<Int, Pair<ItemStack, AutoCraftRecipe>> = this.elements[this.currentPage] ?: return
        (0..<this.inventory.size).forEach { index ->
            this.inventory.setItem(
                index,
                items[index]?.first ?: ItemStack.empty()
            )
        }
        if (canFlipBackPage()) {
            this.inventory.setItem(PREVIOUS, CustomCrafterUI.PREVIOUS_BUTTON)
        }
        this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
    }

    override fun canFlipBackPage(): Boolean {
        return this.currentPage > 0
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        event.isCancelled = true
        when (event.rawSlot) {

            PREVIOUS -> if (canFlipBackPage()) flipBackPage()
            NEXT -> if (canFlipPage()) flipPage()
            BACK_TO_AUTO_CRAFT -> {
                this.player.openInventory(AutoCraftUI.of(this.block, this.player).inventory)
            }

            in PREVIOUS + 1..<NEXT -> {
                return
            }

            // Normal Slots
            else -> {
                val pageItems: Map<Int, Pair<ItemStack, AutoCraftRecipe>> = this.elements[this.currentPage] ?: return
                val recipe: AutoCraftRecipe = pageItems[event.rawSlot]?.second ?: return
                CBlockDB.linkWithoutItems(this.block, recipe)?.let { c ->
                    c.writeToContainer()
                    this.player.sendMessage("<green>Set AutoCraft recipe successful. (Recipe = ${recipe.publisherPluginName}:${recipe.name})".toComponent())
                }
                this.player.openInventory(AutoCraftUI.of(this.block, this.player).inventory)
            }
        }
    }

    override fun getInventory(): Inventory = this.inventory
}
package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class RecipeSetUI: CustomCrafterUI.Pageable, InventoryHolder {

    private lateinit var inventory: Inventory
    private lateinit var block: Block
    private lateinit var player: Player

    var recipes: List<AutoCraftRecipe> = listOf()

    companion object {
        const val NEXT = 53
        const val PREVIOUS = 45
    }

    override var currentPage: Int = 0

    override fun flipPage() {
        if (!canFlipPage()) return
        this.currentPage++
        val chunked: List<List<AutoCraftRecipe>> = CustomCrafterAPI.getRecipes()
            .filterIsInstance<AutoCraftRecipe>()
            .sortedBy { recipe -> recipe.name }
            .chunked(45)
        this.recipes = chunked.getOrNull(this.currentPage) ?: return
        (0..<this.inventory.size).forEach { index ->
            this.inventory.setItem(
                index,
                this.recipes.getOrNull(index)?.autoCraftDisplayItemProvider(this.player)
                    ?: ItemStack.empty()
            )
        }
        this.inventory.setItem(PREVIOUS, CustomCrafterUI.PREVIOUS_BUTTON)
        if (canFlipPage()) {
            this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
        }
    }

    override fun canFlipPage(): Boolean {
        val chunked: List<List<AutoCraftRecipe>> = CustomCrafterAPI.getRecipes()
            .filterIsInstance<AutoCraftRecipe>()
            .sortedBy { recipe -> recipe.name }
            .chunked(45)
        return this.currentPage >= chunked.size - 1
    }

    override fun flipBackPage() {
        if (!canFlipBackPage()) return
        this.currentPage--
        val chunked: List<List<AutoCraftRecipe>> = CustomCrafterAPI.getRecipes().filterIsInstance<AutoCraftRecipe>().chunked(45)
        this.recipes = chunked.getOrNull(this.currentPage) ?: return
        (0..<this.inventory.size).forEach { index ->
            this.inventory.setItem(
                index,
                this.recipes.getOrNull(index)?.autoCraftDisplayItemProvider(this.player)
                    ?: ItemStack.empty()
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
            in PREVIOUS + 1..<NEXT -> {
                return
            }

            PREVIOUS -> if (canFlipBackPage()) flipBackPage()
            NEXT -> if (canFlipPage()) flipPage()

            // Normal Slots
            else -> {
                val recipe: AutoCraftRecipe = this.recipes.getOrNull(event.rawSlot) ?: return
                val cBlock: CBlock = CBlock.of(this.block.state as Crafter)
                    ?: run {
                        CBlockDB.linkWithoutItems(this.block, recipe)
                            ?.writeToContainer()
                        return
                    }
                cBlock.updateRecipe(recipe)
            }
        }
    }

    override fun getInventory(): Inventory = this.inventory

    fun of(player: Player, block: Block): Inventory? {
        this.block = block
        this.player = player
        this.inventory = Bukkit.createInventory(this, 54, "<green><u><b>Auto Craft Recipe Set".toComponent())
        val autoRecipes: MutableList<AutoCraftRecipe> = CustomCrafterAPI.getRecipes().filterIsInstance<AutoCraftRecipe>().toMutableList()
        autoRecipes.sortedBy { recipe -> recipe.name }
        for ((index, recipe) in autoRecipes.chunked(45).firstOrNull()?.withIndex() ?: return null) {
            this.inventory.setItem(index, recipe.autoCraftDisplayItemProvider(player).takeIf { i -> !i.isEmpty } ?: AutoCraftUI.UNDEFINED)
        }

        if (autoRecipes.size > 45) {
            this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
        }

        return this.inventory
    }
}
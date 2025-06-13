package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * A block class for Auto Crafting feature.
 *
 * @param[recipes] A UUID list of [AutoCraftRecipe] id
 * @param[ignoreSlots] Ignored slots numbers when item auto-placed
 * @since 5.0.10
 */
internal data class CBlock(
    val recipes: MutableSet<String> = mutableSetOf(),
    val ignoreSlots: MutableSet<Int> = Converter.getAvailableCraftingSlotIndices().toMutableSet(),
    val containedItems: MutableMap<Int, ItemStack> = mutableMapOf()
) {
    companion object {
        fun fromBlock(block: Block): CBlock? = CBlockDB.read(block)
        fun create(block: Block): CBlock? = CBlockDB.create(block)
        fun clear(block: Block) = CBlockDB.allDelete(block)
    }

    fun getCRecipes(
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): List<CRecipe> {
        val list: MutableList<CRecipe> = mutableListOf()
        sourceRecipes
            .filter { r -> r is AutoCraftRecipe }
            .filter { r -> (r as AutoCraftRecipe).getCBlockTitle() in this.recipes }
            .forEach { r -> list.add(r) }
        return list
    }

    fun update(block: Block, types: Set<CBlockDB.CBlockTableType>) {
        CBlockDB.update(block, this, types)
    }

    fun updateOrCreate(block: Block, types: Set<CBlockDB.CBlockTableType>) {
        CBlockDB.updateOrCreate(block, this, types)
    }

    fun reset(block: Block): List<ItemStack> = CBlockDB.reset(block, this)
}

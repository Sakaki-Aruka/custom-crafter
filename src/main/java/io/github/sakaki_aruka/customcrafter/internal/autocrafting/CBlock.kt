package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.block.Block
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue

/**
 * A block class for Auto Crafting feature.
 *
 * @param[recipes] A UUID list of [AutoCraftRecipe] id
 * @param[ignoreSlots] Ignored slots numbers when item auto-placed
 * @since 5.0.10
 */
@Serializable
internal data class CBlock(
    val recipes: MutableSet<String>,
    val ignoreSlots: MutableSet<Int> = Converter.getAvailableCraftingSlotIndices().toMutableSet(),
    val containedItems: MutableList<ByteArray> = mutableListOf()
) {
    companion object {
        private const val KEY = "custom_crafter_auto_crafting_key"

        /**
         * Get a CBlock instance from a block.
         * @param[block] A block instance.
         * @return[CBlock] A CBlock instance what was written loaded data from the given block. If the given block has not CBlock data, returns null.
         * @since 5.0.10
         */
        fun fromBlock(block: Block): CBlock? {
            if (!block.hasMetadata(KEY)) return null

            val data: List<MetadataValue> = block.getMetadata(KEY)
            val json: String = data.firstOrNull()?.asString() ?: return null
            return Json.decodeFromString(json)
        }
    }

    fun getCRecipes(
        sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
    ): List<CRecipe> {
        val list: MutableList<CRecipe> = mutableListOf()
        sourceRecipes
            .filter { r -> r is AutoCraftRecipe }
            .filter { r -> (r as AutoCraftRecipe).autoCraftID.toString() in this.recipes }
            .forEach { r -> list.add(r) }
        return list
    }

    fun write(
        block: Block,
        debugMode: Boolean = false
    ): Boolean {
        val cBlock: CBlock? = fromBlock(block)
        if (cBlock != null
            && (cBlock.recipes.isNotEmpty() || cBlock.ignoreSlots.isNotEmpty())) {
            if (debugMode) {
                CustomCrafter.getInstance().logger.warning("The block what you want to write, already has any CBlock data.")
                CustomCrafter.getInstance().logger.warning("Block: $block")
            }
            return false
        }
        block.removeMetadata(KEY, CustomCrafter.getInstance()) // delete empty data
        val metadata = FixedMetadataValue(CustomCrafter.getInstance(), Json.encodeToString(this))
        block.setMetadata(KEY, metadata)

        return fromBlock(block)?.let { c -> c == this } ?: false
    }
}

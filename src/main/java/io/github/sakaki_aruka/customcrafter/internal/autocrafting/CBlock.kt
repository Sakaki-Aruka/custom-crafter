package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.persistence.PersistentDataType

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
        private val KEY = NamespacedKey(CustomCrafter.getInstance(), "custom_crafter_auto_crafting_key")

        /**
         * Get a CBlock instance from a block.
         * @param[block] A block instance.
         * @return[CBlock] A CBlock instance what was written loaded data from the given block. If the given block has not CBlock data, returns null.
         * @since 5.0.10
         */
        fun fromBlock(block: Block): CBlock? {
            if (block.type != Material.CRAFTER) return null
            val crafter: Crafter = block.state as Crafter
            val json: String = crafter.persistentDataContainer.get(KEY, PersistentDataType.STRING) ?: return null
            return Json.decodeFromString(json)
        }
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

    fun write(block: Block): Boolean {
        if (block.type != Material.CRAFTER) {
            throw IllegalArgumentException("'block' type must be 'Material.CRAFTER'.")
        }
        val crafter: Crafter = block.state as Crafter
        crafter.apply {
            persistentDataContainer.set(KEY, PersistentDataType.STRING, Json.encodeToString(this@CBlock))
        }.update(true)
        return fromBlock(block)?.let { c -> c == this } ?: false
    }
}

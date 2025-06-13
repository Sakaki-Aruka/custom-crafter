package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A subinterface of [CRecipe] for Auto Crafting feature.
 *
 * @since 5.0.10
 */
interface AutoCraftRecipe: CRecipe {

    companion object {
        private val titleRegex: Regex = Regex("([a-z0-9A-Z-]{36})_(AMORPHOUS|NORMAL)_((_?[0-9]{1,2})+)")

        /**
         * Returns recipe-name what is loaded and matches id, type, slots.
         *
         * If the given string is illegal, returns an empty string.
         *
         * ```
         * // Title format
         * <AutoCraftRecipe.autoCraftID>_<"AMORPHOUS" or "NORMAL">_<Item exist slot(s)>
         *
         * // Example-1
         * // Valid UUID, Valid Type (AMORPHOUS), Item slot amount (what is the recipe requires on AMORPHOUS.)
         * 79804475-89ab-4197-90c6-3638e0ddf202_AMORPHOUS_3
         *
         * // Example-2
         * // Valid UUID, Valid Type (NORMAL), Item slots (what are required by the recipe on NORMAL.)
         * // The last section ("0_1_2_3") means "This recipe requires items what are placed on slot index are 0, 1, 2, 3.".
         * f4d66d26-0098-42fe-87ff-2ab8daa1711c_NORMAL_0_1_2_3
         * ```
         * @param[title] Title formatted string
         * @return[String] Recipe(implements AutoCraftRecipe) name or empty string
         */
        fun getRecipeNameFromCBlockTitle(title: String): String {
            val result: MatchResult = titleRegex.find(title)
                ?: throw IllegalArgumentException("'title' must follow the formates. ('ID_NORMAL_SLOT...' or 'ID_AMORPHOUS_AMOUNT', but given '${title}')")

            val id: UUID = UUID.fromString(result.groups[1]?.value)
            val type: CRecipeType = CRecipeType.entries
                .firstOrNull { e ->
                    e.name == result.groups[2]?.value!!.uppercase()
                } ?: throw IllegalArgumentException("Unknown CRecipeType '${result.groups[2]?.value!!.uppercase()}'. CRecipeType must be 'AMORPHOUS' or 'NORMAL'.")

            val candidate: AutoCraftRecipe = CustomCrafterAPI.getRecipes()
                .filterIsInstance<AutoCraftRecipe>()
                .map { r: CRecipe -> r as AutoCraftRecipe }
                .filter { r: AutoCraftRecipe -> r.type == type }
                .firstOrNull { r: AutoCraftRecipe -> r.autoCraftID == id }
                ?: return ""

            when (type) {
                CRecipeType.AMORPHOUS -> {
                    val slotAmount: Int = result.groups[3]?.value!!.toInt()
                    if (candidate.items.size != slotAmount) {
                        return ""
                    }
                }
                CRecipeType.NORMAL -> {
                    val slots: Set<Int> = result.groups[3]?.value!!.split("_")
                        .filter { s: String -> s.isNotEmpty() }
                        .map { s: String -> s.toInt() }
                        .toSet()
                    if (!slots.containsAll(candidate.items.keys.map { i -> i.toIndex() }.toSet())) {
                        return ""
                    }
                }
            }

            return candidate.name
        }
    }

    /**
     * An identifier of this recipe.
     *
     * You do not change this value without changing [CRecipe.type] and [CRecipe.items] coordinates.
     */
    val autoCraftID: UUID

    /**
     * This lambda provides a displayed item to players when they set auto crafting recipes.
     */
    val autoCraftDisplayItemProvider: (Player) -> ItemStack

    /**
     * [ResultSupplier]s to provide result items on auto crafting.
     */
    val autoCraftResults: List<ResultSupplier>?

    /**
     * Returns a string for CBlock data write.
     * ```
     * ID_TYPE_SLOT_SLOT...
     * or
     * ID_AMORPHOUS_(SLOT-AMOUNT)
     * ```
     *
     * ```
     * Example
     *
     * e44f78a2-fda9-4d59-8ec3-5dfeb86d5e85_NORMAL_0_1_2_3
     * 35fa2dd2-92cf-45ea-8443-9d13b8d1a4bd_AMORPHOUS_4
     * ```
     * @return[String] Formatted string.
     * @since 5.0.10
     */
    fun getCBlockTitle(): String {
        return buildString {
            append(this@AutoCraftRecipe.name)
            append("_")
            append(this@AutoCraftRecipe.type.name.uppercase())
            append("_")
            append(
                when (this@AutoCraftRecipe.type) {
                CRecipeType.AMORPHOUS -> {
                    this@AutoCraftRecipe.items.keys.map { c -> c.toIndex() }.joinToString { "_" }
                }
                CRecipeType.NORMAL -> {
                    this@AutoCraftRecipe.items.size }
                }
            )
        }
    }

    /**
     * Returns items what are made by [autoCraftResults].
     * @param[block] AutoCrafter block
     * @param[relate] An input inventory and [CRecipe] coordinates relation.
     * @param[mapped] Input coordinates and input items relation.
     * @param[calledTimes] Called times ( = minimal input item's amount).
     * @return[MutableList] (MutableList<ItemStack>) Generated items list. If no item supplier applied, returns an empty list.
     * @since 5.0.10
     */
    fun getAutoCraftResults(
        block: Block,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        calledTimes: Int
    ): MutableList<ItemStack> {
        return mutableListOf() // TODO impl
    }
}
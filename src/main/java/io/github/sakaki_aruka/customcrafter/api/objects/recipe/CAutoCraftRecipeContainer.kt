package io.github.sakaki_aruka.customcrafter.api.objects.recipe

import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * A recipe container for [io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe]
 * @param[predicate] Predicate of this container
 * @param[consumer] The body of this container and is executed only when predicate returns true
 * @since 5.0.12
 */
class CAutoCraftRecipeContainer(
    val predicate: (Context) -> Boolean,
    val consumer: (Context) -> Unit
) {

    companion object {
        /**
         * A predicate always returns true
         * ```kotlin
         * val AlwaysTrue: (Context) -> Boolean = { _ -> true }
         * ```
         *
         * @since 5.0.12
         */
        val AlwaysTrue: (Context) -> Boolean = { _ -> true }

        /**
         * Empty container
         * ```kotlin
         * val None: (Context) -> Unit = { _ -> }
         * ```
         *
         * @since 5.0.12
         */
        val None: (Context) -> Unit = { _ -> }
    }

    /**
     * Context of **AutoCraft**RecipeContainer
     * @param[block] Crafter Block
     * @param[relation] Relation of input and recipe
     * @param[mapped] Input items map
     * @param[results] Results of crafting
     * @since 5.0.12
     */
    class Context internal constructor(
        val block: Block,
        val relation: MappedRelation,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val results: MutableList<ItemStack>,
    )
}
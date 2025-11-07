package io.github.sakaki_aruka.customcrafter.api.objects.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Containers for [CRecipe].
 *
 * each container has a predicate and a consumer.
 *
 * When crafting and a predicate is true, consumer runs.
 */
data class CRecipeContainer(
    val predicate: (Context) -> Boolean,
    val consumer: (Context) -> Unit
) {

    companion object {
        /**
         * ```kotlin
         * val AlwaysTrue: (Context) -> Boolean = { _ -> true }
         * ```
         * @since 5.0.12
         */
        val AlwaysTrue: (Context) -> Boolean = { _ -> true }

        /**
         * ```kotlin
         * val None: (Context) -> Unit = { _ -> }
         * ```
         * @since 5.0.12
         */
        val None: (Context) -> Unit = { _ -> }
    }

    /**
     * Context of CRecipeContainer
     * @param[userID] Crafter's ID
     * @param[relation] Relation of input and recipe
     * @param[mapped] Input items map
     * @param[results] Results of crafting
     * @param[isAllCandidateDisplayCall] Called from AllCandidateDisplay feature or not
     * @since 5.0.12
     */
    class Context internal constructor(
        val userID: UUID,
        val relation: MappedRelation,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val results: MutableList<ItemStack>,
        val isAllCandidateDisplayCall: Boolean
    )
}
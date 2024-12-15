package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.MappedRelation
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Containers for [CRecipe].
 * each container has a predicate and a consumer.
 * When crafting and a predicate is true, consumer runs.
 */
data class CRecipeContainer(
    val consumers: List<Pair<Predicate, Consumer>>
) {
    /**
     * A predicate of [CRecipeContainer].
     * crafterID: a crafter's uuid.
     * relate: a coordinate mapping between a [CRecipe] and an input Inventory.
     * mapped: a coordinate and input items mapping
     * list: result items that are made by a [CRecipe]
     *
     * @param[func] a function what checks elements.
     */
    data class Predicate(
        val func: (
            crafterID: UUID,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
                ) -> Boolean
    )

    /**
     * A consumer of [CRecipeContainer].
     * crafterID: a crafter's uuid.
     * relate: a coordinate mapping between a [CRecipe] and an input Inventory
     * mapped: a coordinate and input items mapping
     * list: result items that are made by a [CRecipe]
     *
     * @param[func] a function what consume input data
     */
    data class Consumer(
        val func: (
            crafterID: UUID,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
                ) -> Unit
    )

    /**
     * run all containers
     * ```
     *         consumers
     *             .filter { (p, _) -> p.func(player, relate, mapped, list) }
     *             .forEach { (_, c) -> c.func(player, relate, mapped, list) }
     * ```
     *
     * @param[crafterID] a crafter's uuid
     * @param[relate] a coordinate mapping between a [CRecipe] and an input Inventory
     * @param[mapped] a coordinate and input items mapping
     * @param[list] result items that are made by a [CRecipe]
     * @return[Unit] no return elements
     */
    fun run(
        crafterID: UUID,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        list: MutableList<ItemStack>
    ) {
        consumers
            .filter { (p, _) -> p.func(crafterID, relate, mapped, list) }
            .forEach { (_, c) -> c.func(crafterID, relate, mapped, list) }
    }
}
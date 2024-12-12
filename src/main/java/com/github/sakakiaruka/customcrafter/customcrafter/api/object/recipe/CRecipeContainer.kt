package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.MappedRelation
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
     * player: a crafter
     * relate: a coordinate mapping between a [CRecipe] and an input Inventory.
     * mapped: a coordinate and input items mapping
     * list: result items that are made by a [CRecipe]
     *
     * @param[func] a function what checks elements.
     */
    data class Predicate(
        val func: (
            player: Player,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
                ) -> Boolean
    )

    /**
     * A consumer of [CRecipeContainer].
     * player: a crafter
     * relate: a coordinate mapping between a [CRecipe] and an input Inventory
     * mapped: a coordinate and input items mapping
     * list: result items that are made by a [CRecipe]
     *
     * @param[func] a function what consume input data
     */
    data class Consumer(
        val func: (
            player: Player,
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
     * @param[player] a crafter
     * @param[relate] a coordinate mapping between a [CRecipe] and an input Inventory
     * @param[mapped] a coordinate and input items mapping
     * @param[list] result items that are made by a [CRecipe]
     * @return[Unit] no return elements
     */
    fun run(
        player: Player,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        list: MutableList<ItemStack>
    ) {
        consumers
            .filter { (p, _) -> p.func(player, relate, mapped, list) }
            .forEach { (_, c) -> c.func(player, relate, mapped, list) }
    }
}
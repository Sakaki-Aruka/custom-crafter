package io.github.sakaki_aruka.customcrafter.api.objects.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import org.bukkit.block.Block
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
    val predicate: Predicate,
    val consumer: Consumer
) {
    sealed interface Predicate
    sealed interface Consumer

    /**
     * A predicate of [CRecipeContainer].
     *
     * predicate params
     * - crafterID([UUID]): crafter's UUID.
     * - relate([MappedRelation]): coordinate relation with recipes and inputs
     * - mapped([Map]<[CoordinateComponent], [ItemStack]>): a mapping of input inventory
     * - list([MutableList]<[ItemStack]>): provided result items contained list. you can modify its components.
     * - isMultipleDisplayCall: called from multiple craft result candidate collector or not (since 5.0.10)
     * predicate return
     * - [Boolean]: a result of this predicate.
     *
     * ```kotlin
     * // Kotlin
     * val predicate = CRecipeContainer.NormalPredicate { crafterID, relate, mapped, list ->
     *   Bukkit.getPlayer(crafterID) != null
     * }
     * ```
     *
     * @param[func] a function what checks elements.
     */
    data class NormalPredicate(
        val func: (UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, Boolean) -> Boolean
    ): Predicate {
        operator fun invoke(
            crafterID: UUID,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>,
            isMutableDisplayCall: Boolean
        ): Boolean = func(crafterID, relate, mapped, list, isMutableDisplayCall)

        companion object {
            /**
             * this predicate is always true.
             *
             * ```
             * val True: Predicate = Predicate { _, _, _, _ -> true }
             * ```
             */
            val True: NormalPredicate = NormalPredicate { _, _, _, _, _ -> true }
        }
    }

    /**
     * @since 5.0.10-1
     */
    data class AutoCraftPredicate(
        val func: (Block, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>) -> Boolean
    ): Predicate {


        operator fun invoke(
            block: Block,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
        ): Boolean = func(block, relate, mapped, list)
    }

    /**
     * A processor of [CRecipeContainer].
     * consumer prams
     * - crafterID([UUID]): crafter's UUID.
     * - relate([MappedRelation]): coordinate relation with recipes and inputs
     * - mapped([Map]<[CoordinateComponent], [ItemStack]>): a mapping of input inventory
     * - list([MutableList]<[ItemStack]>): provided result items contained list. you can modify its components.
     * - isMultipleDisplayCall: called from multiple craft result candidate collector or not (since 5.0.10)
     * consumer return
     * - [Unit]: Unit likes void.
     * ```kotlin
     * // call example from Kotlin
     * val consumer = CRecipeContainer.NormalConsumer { crafterID, relate, mapped, list ->
     *   println("foo~~~!!!")
     * }
     * ```
     *
     * @param[func] a function what consume input data
     */
    data class NormalConsumer(
        val func: (UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, Boolean) -> Unit
    ): Consumer {


        operator fun invoke(crafterID: UUID,
                            relate: MappedRelation,
                            mapped: Map<CoordinateComponent, ItemStack>,
                            list: MutableList<ItemStack>,
                            isMultipleDisplayCall: Boolean
        ): Unit = func(crafterID, relate, mapped, list, isMultipleDisplayCall)
    }

    data class AutoCraftConsumer(
        val func: (Block, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>) -> Unit
    ): Consumer {


        operator fun invoke(
            block: Block,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
        ) = func(block, relate, mapped, list)
    }
}
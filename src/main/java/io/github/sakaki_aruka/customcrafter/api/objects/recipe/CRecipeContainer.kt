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
    val consumers: List<Pair<Predicate, Consumer>>
) {

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
     * ```
     * // Java
     * CRecipeContainer.Predicate predicate = new CRecipeContainer.Predicate((crafterID, relate, mapped, list) ->
     *   Bukkit.getPlayer(crafterID) != null
     * );
     *
     * // Kotlin
     * val predicate = CRecipeContainer.Predicate = CRecipeContainer.Predicate { crafterID, relate, mapped, list ->
     *   Bukkit.getPlayer(crafterID) != null
     * }
     * ```
     *
     * @param[func] a function what checks elements.
     */
    data class Predicate(
        val func: Function5<UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, Boolean, Boolean>
    ) {
        operator fun invoke(
            crafterID: UUID,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>,
            isMultipleDisplayCall: Boolean
        ): Boolean = func(crafterID, relate, mapped, list, isMultipleDisplayCall)

        companion object {
            /**
             * this predicate is always true.
             *
             * ```
             * val True: Predicate = Predicate { _, _, _, _ -> true }
             * ```
             */
            val True: Predicate = Predicate { _, _, _, _, _ -> true }
        }
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
     * ```
     * // call example from Java
     * Consumer consumer = new CRecipeContainer.Consumer((crafterID, relate, mapped, list) -> {
     *   System.out.println("foo~~!!!");
     *
     *   // kotlin's "Unit" likes Java's "void".
     *   return kotlin.Unit.INSTANCE
     * });
     *
     * // call example from Kotlin
     * val consumer = CRecipeContainer.Consumer { crafterID, relate, mapped, list ->
     *   println("foo~~~!!!")
     * }
     * ```
     *
     * @param[func] a function what consume input data
     */
    data class Consumer(
        val func: Function5<UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, Boolean, Unit>
    ) {
        operator fun invoke(
            crafterID: UUID,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>,
            isMultipleDisplayCall: Boolean
        ): Unit = func(crafterID, relate, mapped, list, isMultipleDisplayCall)
    }

    /**
     * run all containers
     * ```
     *         consumers
     *             .filter { (p, _) -> p.func.invoke(player, relate, mapped, list) }
     *             .forEach { (_, c) -> c.func.invoke(player, relate, mapped, list) }
     * ```
     *
     * @param[crafterID] a crafter's uuid
     * @param[relate] a coordinate mapping between a [CRecipe] and an input Inventory
     * @param[mapped] a coordinate and input items mapping
     * @param[list] result items that are made by a [CRecipe]
     * @param[isMultipleDisplayCall] called from multiple craft result candidate collector or not (since 5.0.10)
     * @return[Unit] no return elements
     */
    fun run(
        crafterID: UUID,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        list: MutableList<ItemStack>,
        isMultipleDisplayCall: Boolean
    ) {
        consumers
            .filter { (p, _) -> p.func.invoke(crafterID, relate, mapped, list, isMultipleDisplayCall) }
            .forEach { (_, c) -> c.func.invoke(crafterID, relate, mapped, list, isMultipleDisplayCall) }
    }
}
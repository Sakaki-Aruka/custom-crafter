package io.github.sakaki_aruka.customcrafter.api.objects.recipe

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Containers for [CRecipe].
 *
 * each container has a predicate and a consumer.
 *
 * When crafting and a predicate is true, consumer runs.
 * @param[predicate] A predicate of this container.
 * @param[consumer] A consumer (processor) of this container. ([NormalConsumer] or [CraftingGUIAccessor])
 */
data class CRecipeContainer(
    val predicate: Predicate,
    val consumer: Consumer
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
     * A normal container consumer.
     * @see [Consumer]
     */
    data class NormalConsumer(
        override val func: Function5<UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, Boolean, Unit>
    ): Consumer

    /**
     * A crafting gui accessor consumer.
     *
     * This consumer triggered after all [NormalConsumer] run completed.
     *
     * If [CRecipe.containers] has more than 2 crafting gui accessors, only first one accessor run whose predicate returns true.
     * @see [Consumer]
     */
    data class CraftingGUIAccessor(
        override val func: (UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, Boolean) -> Unit
    ): Consumer {
        companion object {
            /**
             * Get simple players crafting gui item replacer.
             *
             * - CoordinateComponent: A coordinate of crafting gui (If this is out of range [Converter.getAvailableCraftingSlotComponents], skipped run.)
             * - ItemStack: Replace item
             * - Boolean: Force-replace. If false, a target slots item must be null or empty.
             *
             * @param[mapping] A players crafting gui item replacer
             * @return[CraftingGUIAccessor] Simple gui accessor (item replacer)
             */
            fun simpleAccessor(
                mapping: Set<Triple<CoordinateComponent, ItemStack, Boolean>>
            ): CraftingGUIAccessor = CraftingGUIAccessor { id, _, placed, _, _ ->
                val gui: Inventory = Bukkit.getPlayer(id)
                    ?.openInventory
                    ?.topInventory
                    ?.takeIf { i -> CustomCrafterAPI.isCustomCrafterGUI(i) }
                    ?: return@CraftingGUIAccessor

                for ((c, item, forcePlace) in mapping) {
                    if (c !in Converter.getAvailableCraftingSlotComponents()) continue
                    val placedItem: ItemStack? = placed[c]
                    if (placedItem == null || placedItem.isSimilar(ItemStack.empty()) || forcePlace) {
                        gui.setItem(c.toIndex(), item)
                    }
                }
            }
        }
    }

    /**
     * A processor interface of [CRecipeContainer].
     *
     * [NormalConsumer] and [CraftingGUIAccessor] are implementation class of this interface.
     *
     * consumer prams
     * - crafterID([UUID]): crafter's UUID.
     * - relate([MappedRelation]): coordinate relation with recipes and inputs
     * - mapped([Map]<[CoordinateComponent], [ItemStack]>): a mapping of input inventory
     * - list([MutableList]<[ItemStack]>): provided result items contained list. you can modify its components.
     * - isMultipleDisplayCall([Boolean]): called from multiple craft result candidate collector or not (since 5.0.10)
     * consumer return
     * - [Unit]: Unit likes void.
     * ```
     *
     * // call example from Kotlin
     * val consumer = CRecipeContainer.NormalConsumer { crafterID, relate, mapped, list, isMultipleDisplayCall ->
     *   println("foo~~~!!!")
     * }
     * ```
     *
     * @param[func] a function what consume input data
     * @see [NormalConsumer]
     * @see [CraftingGUIAccessor]
     */
    sealed interface Consumer {
        val func: Function5<UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, Boolean, Unit>

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
     *         containers
     *             .filter { c -> c.predicate.func(player, relate, mapped, list) }
     *             .forEach { c -> c.consumer.func(player, relate, mapped, list) }
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
        if (predicate(crafterID, relate, mapped, list, isMultipleDisplayCall)) {
            consumer(crafterID, relate, mapped, list, isMultipleDisplayCall)
        }
    }
}
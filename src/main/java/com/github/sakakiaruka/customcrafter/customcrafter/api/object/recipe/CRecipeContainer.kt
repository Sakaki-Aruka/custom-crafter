package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.MappedRelation
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
     * ```
     * // Java
     * Predicate predicate = new Predicate((Function4<UUID, MappedRelation, Map<Coordinate
     * ```
     *
     * @param[func] a function what checks elements.
     */
    data class Predicate(
        val func: (UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>) -> Boolean
    ) {
        operator fun invoke(
            crafterID: UUID,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
        ): Boolean = func(crafterID, relate, mapped, list)
    }
//    class Predicate private constructor(
//        val func: PredicateFunc
//    ) {
//
//        fun interface PredicateFunc {
//            /**
//             * @param[crafterID] a crafter's uuid.
//             * @param[relate] a coordinate mapping between a [CRecipe] and an input Inventory.
//             * @param[mapped] a coordinate and input items mapping
//             * @param[list] result items that are made by a [CRecipe]
//             */
//            fun invoke(
//                crafterID: UUID,
//                relate: MappedRelation,
//                mapped: Map<CoordinateComponent, ItemStack>,
//                list: MutableList<ItemStack>
//            ): Boolean
//        }
//
//        companion object {
//            /**
//             * A predicate of [CRecipeContainer].
//             * Function parameters
//             * - [UUID]: a crafter's uuid.
//             * - [MappedRelation]: a coordinate mapping between a [CRecipe] and an input Inventory
//             * - [Map]<[CoordinateComponent], [ItemStack]>: a coordinate and input items mapping
//             * - [MutableList]<[ItemStack]>: result items that are made by a [CRecipe]
//             * ```
//             * // call example from Java
//             * CRecipeContainer.Predicate predicate = CRecipeContainer.Predicate.of((crafterID, relate, mapped, list) -> true);
//             *
//             * // call example from Kotlin
//             * val consumer = CRecipeContainer.Consumer.of { crafterID, relate, mapped, list -> true }
//             * ```
//             *
//             * @param[func] function.
//             * @return[CRecipeContainer.Predicate] predicate
//             */
//            fun of(func: (UUID,
//                          MappedRelation,
//                          Map<CoordinateComponent, ItemStack>,
//                          MutableList<ItemStack>
//                    ) -> Boolean): Predicate {
//                return Predicate(PredicateFunc(func))
//            }
//        }
//    }

    /**
     * @param[func] a function what consume input data
     */
    data class Consumer(
        val func: (UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>) -> Unit
    ) {
        operator fun invoke(
            crafterID: UUID,
            relate: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
        ): Unit = func(crafterID, relate, mapped, list)
    }
//    class Consumer private constructor(
//        val func: ConsumerFunc
//    ) {
//        fun interface ConsumerFunc {
//            fun invoke(
//                crafterID: UUID,
//                relate: MappedRelation,
//                mapped: Map<CoordinateComponent, ItemStack>,
//                list: MutableList<ItemStack>
//            )
//        }
//        companion object {
//            /**
//             * A consumer of [CRecipeContainer].
//             * Function parameters
//             * - [UUID]: a crafter's uuid.
//             * - [MappedRelation]: a coordinate mapping between a [CRecipe] and an input Inventory
//             * - [Map<CoordinateComponent, ItemStack>]: a coordinate and input items mapping
//             * - [MutableList<ItemStack>]: result items that are made by a [CRecipe]
//             * ```
//             * // call example from Java
//             * Consumer consumer = CRecipeContainer.Consumer.of((crafterID, relate, mapped, list) -> {
//             *   System.out.println("foo~~!!!");
//             *
//             *   // kotlin's "Unit" likes Java's "void".
//             *   return kotlin.Unit.INSTANCE;
//             * });
//             *
//             * // call example from Kotlin
//             * val consumer = CRecipeContainer.Consumer.of { crafterID, relate, mapped, list ->
//             *   println("foo~~~!!!")
//             * }
//             * ```
//             *
//             * @param[func] function.
//             * @return[CRecipeContainer.Consumer] consumer
//             */
//            fun of(func: (UUID,
//                          MappedRelation,
//                          Map<CoordinateComponent, ItemStack>,
//                          MutableList<ItemStack>
//            ) -> Unit): Consumer {
//                return Consumer(ConsumerFunc(func))
//            }
//        }
//    }

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
     * @return[Unit] no return elements
     */
    fun run(
        crafterID: UUID,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        list: MutableList<ItemStack>
    ) {
        consumers
            .filter { (p, _) -> p.func.invoke(crafterID, relate, mapped, list) }
            .forEach { (_, c) -> c.func.invoke(crafterID, relate, mapped, list) }
    }
}
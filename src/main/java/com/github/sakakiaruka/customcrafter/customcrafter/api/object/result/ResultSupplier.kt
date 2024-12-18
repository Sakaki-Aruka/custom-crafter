package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.result

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.MappedRelation
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeContainer
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

data class ResultSupplier (
    val func: Function4<UUID, MappedRelation, Map<CoordinateComponent, ItemStack>, MutableList<ItemStack>, List<ItemStack>>
) {
        operator fun invoke(
            crafterID: UUID,
            relation: MappedRelation,
            mapped: Map<CoordinateComponent, ItemStack>,
            list: MutableList<ItemStack>
        ): List<ItemStack> = func(crafterID, relation, mapped, list)
}
//data class ResultSupplier(
//    val func: ResultFunc
//) {
//    fun interface ResultFunc {
//        fun invoke(
//            crafterID: UUID,
//            relation: MappedRelation,
//            mapped: Map<CoordinateComponent, ItemStack>,
//            list: MutableList<ItemStack>
//        ): List<ItemStack>
//    }
//
//    companion object {
//        /**
//         * A result supplier of [CRecipeContainer].
//         * Function parameters
//         * - [UUID]: a crafter's uuid.
//         * - [MappedRelation]: a coordinate mapping between a [CRecipe] and an input Inventory
//         * - [Map]<[CoordinateComponent], [ItemStack]>: a coordinate and input items mapping
//         * - [MutableList]<[ItemStack]>: result items that are made by a [CRecipe]
//         * ```
//         * // call example from Java
//         * ResultSupplier supplier = ResultSupplier.of((crafterID, relate, mapped, list) -> List.of(ItemStack.empty()));
//         *
//         * // call example from Kotlin
//         * val consumer = CRecipeContainer.Consumer.of { crafterID, relate, mapped, list -> listOf(ItemStack.empty()) }
//         * ```
//         *
//         * @param[func] function.
//         * @return[CRecipeContainer.Predicate] predicate
//         */
//        fun of(func: (UUID,
//                MappedRelation,
//                Map<CoordinateComponent, ItemStack>,
//                MutableList<ItemStack>
//                ) -> List<ItemStack>): ResultSupplier {
//            return ResultSupplier(ResultFunc(func))
//        }
//    }
//}

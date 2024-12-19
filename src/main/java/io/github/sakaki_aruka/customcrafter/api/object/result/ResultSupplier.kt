package io.github.sakaki_aruka.customcrafter.api.`object`.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A result supplier of [CRecipe].
 *
 * Function parameters
 * - [UUID]: a crafter's uuid.
 * - [MappedRelation]: a coordinate mapping between a [CRecipe] and an input Inventory
 * - [Map]<[CoordinateComponent], [ItemStack]>: a coordinate and input items mapping
 * - [MutableList]<[ItemStack]>: result items that are made by a [CRecipe]
 * ```
 * // call example from Java
 * ResultSupplier supplier = new ResultSupplier ((crafterID, relate, mapped, list) -> List.of(ItemStack.empty()));
 *
 * // call example from Kotlin
 * val supplier = ResultSupplier { crafterID, relate, mapped, list -> listOf(ItemStack.empty()) }
 * ```
 *
 * @param[func] function.
 * @return[ResultSupplier]
 */
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

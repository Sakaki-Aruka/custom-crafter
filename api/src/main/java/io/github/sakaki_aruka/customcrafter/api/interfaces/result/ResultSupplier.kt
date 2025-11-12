package io.github.sakaki_aruka.customcrafter.api.interfaces.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Result items supplier of [CRecipe].
 *
 * @param[f] Lambda expression what receives a context, returns items list
 * @see[io.github.sakaki_aruka.customcrafter.impl.result.ResultSupplierImpl]
 */
interface ResultSupplier {
    val f: (Context) -> List<ItemStack>

    operator fun invoke(ctx: Context): List<ItemStack> = f(ctx)

    /**
     * This class contains ResultSupplier parameters.
     *
     * @param[crafterID] Crafter UUID
     * @param[relation] Coordinate mapping between a [CRecipe] and an input Inventory
     * @param[mapped] Coordinates and input items mapping
     * @param[list] Result items that are made by a [CRecipe]
     * @param[shiftClicked] Shift-clicked or not
     * @param[calledTimes] Calculated minimum amount with [CMatter.amount]
     * @param[isMultipleDisplayCall] `invoke` called from multiple result display item collector or not
     */
    class Context internal constructor(
        val relation: MappedRelation,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val shiftClicked: Boolean,
        val calledTimes: Int,
        val list: MutableList<ItemStack>,
        val crafterID: UUID,
        val isMultipleDisplayCall: Boolean
    )
}
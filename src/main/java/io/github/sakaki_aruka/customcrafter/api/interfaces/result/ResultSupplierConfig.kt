package io.github.sakaki_aruka.customcrafter.api.interfaces.result

import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import org.bukkit.inventory.ItemStack

/**
 * An interface of [ResultSupplier]'s config.
 * @since 5.0.10
 */
interface ResultSupplierConfig {
    val relation: MappedRelation
    val mapped: Map<CoordinateComponent, ItemStack>
    val shiftClicked: Boolean
    val calledTimes: Int
    val list: MutableList<ItemStack>
}
package io.github.sakaki_aruka.customcrafter.api.`object`.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A [CMatter]'s predicate functions.
 */
data class CMatterPredicate(
    val predicate: Function4<ItemStack, Map<CoordinateComponent, ItemStack>, CRecipe, UUID, Boolean>
) {

    operator fun invoke(
        self: ItemStack,
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): Boolean = predicate(self, mapped, recipe, crafterID)
}

//data class CMatterPredicate private constructor(
//    val predicate: MatterPredicateFunc
//){
//    fun interface MatterPredicateFunc {
//        fun invoke(
//            self: ItemStack,
//            mapped: Map<CoordinateComponent, ItemStack>,
//            recipe: CRecipe,
//            crafterID: UUID
//        ): Boolean
//    }
//
//    companion object {
//        fun of(func: (self: ItemStack,
//                      mapped: Map<CoordinateComponent, ItemStack>,
//                      recipe: CRecipe,
//                      crafterID: UUID) -> Boolean): CMatterPredicate {
//            return CMatterPredicate(MatterPredicateFunc(func))
//        }
//    }
//}
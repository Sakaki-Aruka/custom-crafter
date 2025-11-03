package io.github.sakaki_aruka.customcrafter.api.objects.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A [CMatter]'s predicate functions.
 *
 * If [predicate] returns null on search process, the recipe marked not matches.
 */
data class CMatterPredicate(
    val predicate: (Context) -> Boolean
) {

    /**
     * Context of CMatterPredicate
     * @since 5.0.15
     */
    data class Context internal constructor(
        val input: ItemStack,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val recipe: CRecipe,
        val crafterID: UUID
    )

    operator fun invoke(
        self: ItemStack,
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): Boolean = predicate(Context(self, mapped, recipe, crafterID))
}
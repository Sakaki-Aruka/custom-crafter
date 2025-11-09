package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A [CMatter] predicate function.
 *
 * If [predicate] returns null on the search process, a recipe marked not matches.
 *
 * @param[predicate] A lambda expression what receives [Context] and returns inspection result
 * @see[Context]
 * @see[CMatter.predicates]
 */
interface CMatterPredicate {
    val predicate: (Context) -> Boolean

    /**
     * CMatterPredicate context
     *
     * @param[input] Inspection target
     * @param[mapped] User input items mapping
     * @param[recipe] A CRecipe what contains a CMatterPredicate who receives this
     * @param[crafterID] Crafter UUID
     * @see[CMatterPredicate]
     */
    data class Context internal constructor(
        val input: ItemStack,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val recipe: CRecipe,
        val crafterID: UUID
    )
}
package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search.SearchKVClient
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search.SearchSession
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
interface CMatterPredicate: SearchKVClient {
    val predicate: (Context) -> Boolean

    /**
     * CMatterPredicate context
     *
     * @param[coordinate] Inspection point on a recipe mapping
     * @param[matter] Inspector
     * @param[input] Inspection target
     * @param[mapped] User input items mapping
     * @param[recipe] A CRecipe what contains a CMatterPredicate who receives this
     * @param[crafterID] Crafter UUID
     * @see[CMatterPredicate]
     */
    class Context (
        val coordinate: CoordinateComponent,
        val matter: CMatter,
        val input: ItemStack,
        val mapped: Map<CoordinateComponent, ItemStack>,
        val recipe: CRecipe,
        val crafterID: UUID,
        override val session: SearchSession
    ): SearchKVClient.Context {
        fun copyWith(
            matter: CMatter = this.matter
        ): Context {
            return Context(
                coordinate = this.coordinate,
                matter = matter,
                input = this.input,
                mapped = this.mapped,
                recipe = this.recipe,
                crafterID = this.crafterID,
                session = this.session
            )
        }
    }
}
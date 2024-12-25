package io.github.sakaki_aruka.customcrafter.api.processor

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * @suppress
 */
object Container {
    internal fun amorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        if (recipe.containers.isNullOrEmpty()) {
            return Pair(AmorphousFilterCandidate.Type.NOT_REQUIRED, emptyList())
        }

        val recipes: List<CoordinateComponent> = recipe.items
            .filter { !it.value.predicates.isNullOrEmpty() }
            .map { it.key }

        val inputCoordinates: List<CoordinateComponent> = mapped.keys.toList()
        // val input = mapped

        if (recipes.size > inputCoordinates.size) {
            return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
        } else if (recipes.isEmpty()) {
            return Pair(AmorphousFilterCandidate.Type.NOT_REQUIRED, emptyList())
        }

        val map: MutableMap<Int, List<Int>> = mutableMapOf()
        for (index: Int in recipes.indices) {

            map[index] = matchList(mapped, recipe, crafterID)
                .withIndex()
                .filter { it.value }
                .map { it.index }
        }

        val result: MutableList<AmorphousFilterCandidate> = mutableListOf()
        for (slice in map.entries) {
            val R: CoordinateComponent = recipes[slice.key]
            val list: List<CoordinateComponent> = inputCoordinates
                .withIndex()
                .filter { slice.value.contains(it.index) }
                .map { it.value }
            result.add(AmorphousFilterCandidate(R, list))
        }

        val type: AmorphousFilterCandidate.Type =
            if (result.isEmpty()) AmorphousFilterCandidate.Type.NOT_ENOUGH
            else AmorphousFilterCandidate.Type.SUCCESSFUL

        return Pair(type, result)
    }

    private fun matchList(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): List<Boolean> {
        val result: MutableList<Boolean> = mutableListOf()
        mapped.forEach { (_, item) ->
            recipe.items.forEach { (_, m) ->
                result.add(m.predicates?.all { p ->
                    p.predicate.invoke(item, mapped, recipe, crafterID)
                } ?: true)
            }
        }
        return result
    }
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.processor

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterPredicate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack

object Container {
    internal fun amorphous(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
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
            val predicates: List<CMatterPredicate> = recipe.items.values.toList()[index].predicates!!.toList()
            map[index] = matchList(mapped.values.toList(), predicates)
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

    private fun matchList(ins: List<ItemStack>, predicates: List<CMatterPredicate>): List<Boolean> {
        return ins.map { item ->
            val container = item.itemMeta.persistentDataContainer
            predicates.all { p ->
                p.predicates.all { it(item, container) }
            }
        }.toList()
    }
}
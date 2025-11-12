package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack

interface ExtraSearchChecker: CRecipe {
    // CoordinateComponent -> Recipe coordinates
    val check: (Context) -> Map<CoordinateComponent, Boolean>

    sealed interface Context {
        val mapped: Map<CoordinateComponent, ItemStack>
    }

    class ShapedContext(
        override val mapped: Map<CoordinateComponent, ItemStack>
    ): Context

    class ShapelessContext(
        override val mapped: Map<CoordinateComponent, ItemStack>,
        val candidateResults: Map<Int, Set<Triple<Int, Boolean, Boolean>>>,
        val filterResults: Map<Int, MutableList<Triple<Int, Boolean, Boolean>>>,
        val matterPredicateResult: Map<Int, Set<Triple<Int, Boolean, Boolean>>>,
        val amountResults: Map<Int, Set<Triple<Int, Boolean, Boolean>>>,
    ): Context
}
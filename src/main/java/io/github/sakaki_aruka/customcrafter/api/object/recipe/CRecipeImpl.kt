package io.github.sakaki_aruka.customcrafter.api.`object`.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.result.ResultSupplier

data class CRecipeImpl(
    override val name: String,
    override val items: Map<io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent, CMatter>,
    override val containers: List<io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CRecipeContainer>? = null,
    override val results: List<ResultSupplier>? = null,
    override val type: io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CRecipeType,
): CRecipe {
    /**
     * @see[CRecipe.replaceItems]
     */
    override fun replaceItems(newItems: Map<io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent, CMatter>): io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CRecipeImpl {
        return io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CRecipeImpl(
            this.name,
            newItems,
            this.containers,
            this.results,
            this.type
        )
    }
}
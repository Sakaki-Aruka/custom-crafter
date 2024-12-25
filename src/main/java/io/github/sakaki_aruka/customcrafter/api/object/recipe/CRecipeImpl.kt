package io.github.sakaki_aruka.customcrafter.api.`object`.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter.EnchantFilter
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter.EnchantStorageFilter
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter.PotionFilter
import io.github.sakaki_aruka.customcrafter.api.`object`.result.ResultSupplier

data class CRecipeImpl(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val containers: List<CRecipeContainer>? = null,
    override val results: List<ResultSupplier>? = null,
    override val filters: Set<CRecipeFilter<CMatter>>? = setOf(
        EnchantFilter,
        EnchantStorageFilter,
        PotionFilter
    ),
    override val type: CRecipeType,
): CRecipe {
    /**
     * @see[CRecipe.replaceItems]
     */
    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipeImpl {
        return CRecipeImpl(
            this.name,
            newItems,
            this.containers,
            this.results,
            this.filters,
            this.type
        )
    }
}
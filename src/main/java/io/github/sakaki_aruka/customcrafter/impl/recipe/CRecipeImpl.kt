package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.EnchantFilter
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.EnchantStorageFilter
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.PotionFilter
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier

/**
 * A default [CRecipe] implementation class.
 *
 * @param[name] A name of this recipe.
 * @param[items] Elements of this recipe.
 * @param[type] A type of this recipe. (Normal or Amorphous (=Shapeless))
 * @param[containers] Containers of this recipe. (default = null)
 * @param[results] A [ResultSupplier] list. (default = null)
 * @param[filters] A recipe filters. (default = [getDefaultFilters])
 */
data class CRecipeImpl(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipeType,
    override val containers: List<CRecipeContainer>? = null,
    override val results: List<ResultSupplier>? = null,
    override val filters: Set<CRecipeFilter<CMatter>>? = getDefaultFilters()
): CRecipe {
    /**
     * @see[CRecipe.replaceItems]
     */
    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipeImpl {
        return CRecipeImpl(
            this.name,
            newItems,
            this.type,
            this.containers,
            this.results,
            this.filters
        )
    }

    companion object {
        /**
         * returns default candidate filter what are used in search.
         *
         * @return[Set] a set of default filters
         */
        fun getDefaultFilters(): Set<CRecipeFilter<CMatter>> {
            return setOf(
                EnchantFilter,
                EnchantStorageFilter,
                PotionFilter
            )
        }

        /**
         * Amorphous recipe build wrapper.
         *
         * This calls the constructor with arguments and [CRecipeType.AMORPHOUS].
         * @return[CRecipeImpl] an amorphous recipe
         * @throws[IllegalArgumentException] Throws if [items] size is out of the range (1 ~ 36).
         * @throws[IllegalStateException] Throws if built recipe is invalid.
         * @since v5.0.14
         */
        fun amorphous(
            name: String,
            items: List<CMatter>,
            containers: List<CRecipeContainer>? = null,
            results: List<ResultSupplier>? = null,
            filters: Set<CRecipeFilter<CMatter>>? = getDefaultFilters()
        ): CRecipeImpl {
            if (items.isEmpty() || items.size > 36) {
                throw IllegalArgumentException("'items' size must be in range of 1 to 36.")
            }
            val map: Map<CoordinateComponent, CMatter> =
                CustomCrafterAPI.getRandomNCoordinates(items.size)
                    .zip(items)
                    .associate { (c, m) -> c to m }
            val recipe = CRecipeImpl(
                name = name,
                items = map,
                type = CRecipeType.AMORPHOUS,
                containers = containers,
                results = results,
                filters = filters
            )
            CRecipe.isValidCRecipe(recipe).exceptionOrNull()?.let { t -> throw t }
            return recipe
        }
    }
}
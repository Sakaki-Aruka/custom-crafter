package io.github.sakaki_aruka.customcrafter.api.`object`.recipe

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter.EnchantFilter
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter.EnchantStorageFilter
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter.PotionFilter
import io.github.sakaki_aruka.customcrafter.api.`object`.result.ResultSupplier

/**
 * A default [CRecipe] implementation class.
 *
 * @param[name] A name of this recipe.
 * @param[items] Elements of this recipe.
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
     * This constructor provides only a recipe that is CRecipeType#ARMOPHOUS. (= shapeless recipe.)
     *
     * So, [type] is automatically specified.
     *
     * Other parameters are same with default the default constructor,
     * but [items] type is changed to `List<CMatter>` from `Map<CoordinateComponent, CMatter>`
     *
     * @param[name] A name of this recipe.
     * @param[items] Elements of this recipe.
     * @param[containers] Containers of this recipe. (default = null)
     * @param[results] A [ResultSupplier] list. (default = null)
     * @param[filters] A recipe filters. (default = [getDefaultFilters])
     * @since 5.0.9
     */
    constructor(
        name: String,
        items: List<CMatter>,
        containers: List<CRecipeContainer>? = null,
        results: List<ResultSupplier>? = null,
        filters: Set<CRecipeFilter<CMatter>>? = getDefaultFilters()
    ): this(
        name = name,
        items = CustomCrafterAPI.getRandomNCoordinates(items.size)
            .zip(items)
            .toMap(),
        containers = containers,
        results = results,
        filters = filters,
        type = CRecipeType.AMORPHOUS
    )

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
    }
}
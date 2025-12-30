package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search.SearchPreprocessor
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier

/**
 * A default [CRecipe] implementation class.
 *
 * @param[name] A name of this recipe.
 * @param[items] Elements of this recipe.
 * @param[type] A type of this recipe. (Shaped or Shapeless)
 * @param[containers] Containers of this recipe. (default = null)
 * @param[results] A [ResultSupplier] list. (default = null)
 */
open class CRecipeImpl @JvmOverloads constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipe.Type,
    override val preprocessors: List<SearchPreprocessor>? = null,
    override val predicates: List<CRecipePredicate>? = null,
    override val containers: List<CRecipeContainer>? = null,
    override val results: List<ResultSupplier>? = null,
): CRecipe {
    companion object {
        /**
         * Shapeless recipe build wrapper.
         *
         * This calls the constructor with arguments and [CRecipe.Type.SHAPELESS].
         * @return[CRecipeImpl] an shapeless recipe
         * @throws[IllegalArgumentException] Throws if [items] size is out of the range (1 ~ 36).
         * @throws[IllegalStateException] Throws if built recipe is invalid.
         * @since 5.0.14
         */
        @JvmStatic
        @JvmOverloads
        fun shapeless(
            name: String,
            items: List<CMatter>,
            preprocessors: List<SearchPreprocessor>? = null,
            predicates: List<CRecipePredicate>? = null,
            containers: List<CRecipeContainerImpl>? = null,
            results: List<ResultSupplier>? = null,
        ): CRecipeImpl {
            if (items.isEmpty() || items.size > 36) {
                throw IllegalArgumentException("'items' size must be in range of 1 to 36.")
            }
            val map: Map<CoordinateComponent, CMatter> =
                CoordinateComponent.getN(items.size)
                    .zip(items)
                    .associate { (c, m) -> c to m }
            val recipe = CRecipeImpl(
                name = name,
                items = map,
                type = CRecipe.Type.SHAPELESS,
                preprocessors = preprocessors,
                predicates = predicates,
                containers = containers,
                results = results,
            )
            recipe.isValidRecipe().exceptionOrNull()?.let { t -> throw t }
            return recipe
        }
    }
}
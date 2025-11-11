package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeContainerImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.result.ResultSupplierImpl
import org.bukkit.Material
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

data class CVanillaRecipe internal constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipeType,
    override val containers: List<CRecipeContainerImpl>? = null,
    override val results: List<ResultSupplier>? = null,
    override val filters: Set<CRecipeFilter<CMatter>>? = CRecipeImpl.getDefaultFilters(),
    val original: Recipe
): CRecipe {
    companion object {
        fun fromVanilla(recipe: CraftingRecipe): CVanillaRecipe? {
            return when (recipe) {
                is ShapedRecipe -> fromShaped(recipe)
                is ShapelessRecipe -> fromShapeless(recipe)
                else -> null
            }
        }

        /**
         * Converts a vanilla shaped recipe to a CRecipe.
         *
         * @param[recipe] A target shaped recipe
         * @return[CRecipe] A result CRecipe
         * @since 5.0.11
         */
        fun fromShaped(recipe: ShapedRecipe): CVanillaRecipe {
            return CVanillaRecipe(
                recipe.key.namespace + recipe.key.key,
                shapeToItems(recipe.shape, recipe.choiceMap),
                CRecipeType.NORMAL,
                results = listOf(ResultSupplierImpl.timesSingle(recipe.result)),
                original = recipe
            )
        }

        /**
         * Converts a vanilla shapeless recipe to a CRecipe.
         *
         * @param[recipe] A target shapeless recipe
         * @return[CRecipe] A result CRecipe
         * @since[5.0.11]
         */
        fun fromShapeless(recipe: ShapelessRecipe): CVanillaRecipe {
            return CVanillaRecipe(
                recipe.key.namespace + recipe.key.key,
                shapelessToItems(recipe.choiceList),
                CRecipeType.AMORPHOUS,
                results = listOf(ResultSupplierImpl.timesSingle(recipe.result)),
                original = recipe
            )
        }

        private fun shapelessToItems(choices: List<RecipeChoice>): Map<CoordinateComponent, CMatter> {
            val result: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
            choices.withIndex().forEach { (index, choice) ->
                val candidates: Set<Material> = choiceToCandidates(choice)
                val matter: CMatter = CMatterImpl(
                    candidates.firstOrNull()?.name ?: "vanilla matter default name",
                    candidates
                )
                result[CoordinateComponent.Companion.fromIndex(index)] = matter
            }
            return result
        }

        private fun shapeToItems(
            shape: Array<out String>,
            map: Map<Char, RecipeChoice>
        ): Map<CoordinateComponent, CMatter> {
            val candidateMap: Map<Char, Set<Material>> = getCandidateMap(map)
            val result: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
            shape.withIndex().forEach { (index, c) ->
                val candidates: Set<Material> = candidateMap[c.first()] ?: emptySet()
                val matter: CMatter = CMatterImpl(
                    candidates.firstOrNull()?.name ?: "vanilla matter default name",
                    candidates
                )
                result[CoordinateComponent.Companion.fromIndex(index)] = matter
            }
            return result
        }

        private fun getCandidateMap(map: Map<Char, RecipeChoice>): Map<Char, Set<Material>> {
            val result: MutableMap<Char, Set<Material>> = mutableMapOf()
            for ((c, choice) in map.entries) {
                result[c] = choiceToCandidates(choice)
            }
            return result
        }

        private fun choiceToCandidates(choice: RecipeChoice): Set<Material> {
            return when (choice) {
                is RecipeChoice.ExactChoice -> {
                    exactChoiceToCandidate(choice)
                }
                is RecipeChoice.MaterialChoice -> {
                    materialChoiceToCandidate(choice)
                }
                else -> emptySet()
            }
        }

        private fun exactChoiceToCandidate(choice: RecipeChoice.ExactChoice): Set<Material> {
            return choice.choices.map { item -> item.type }.toSet()
        }

        private fun materialChoiceToCandidate(choice: RecipeChoice.MaterialChoice): Set<Material> {
            return choice.choices.toSet()
        }
    }

    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipe {
        return CVanillaRecipe(
            this.name,
            newItems,
            this.type,
            this.containers,
            this.results,
            this.filters,
            this.original
        )
    }
}
package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import org.bukkit.Material
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

/**
 * Deprecated to use. Internal only.
 */
class CVanillaRecipe internal constructor(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipe.Type,
    override val predicates: List<CRecipePredicate>? = null,
    override val results: List<ResultSupplier>? = null,
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
                CRecipe.Type.SHAPED,
                results = listOf(ResultSupplier.timesSingle(recipe.result)),
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
                CRecipe.Type.SHAPELESS,
                results = listOf(ResultSupplier.timesSingle(recipe.result)),
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
                result[CoordinateComponent.fromIndex(index)] = matter
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
                result[CoordinateComponent.fromIndex(index)] = matter
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

    fun relateWith(view: CraftView): MappedRelation {
        val viewSize: Int = view.materials.filterNot { it.value.type.isAir }.count()
        if (viewSize != this.items.size) {
            throw IllegalArgumentException("'view#materials' size is not equals with this recipes size. (view size: ${viewSize}, recipe size: ${items.size})")
        }

        val dx: Int = view.materials.keys.maxOf { it.x } - view.materials.keys.minOf { it.x }
        val dy: Int = view.materials.keys.maxOf { it.y } - view.materials.keys.minOf { it.y }
        if (dx > 3 || dy > 3) {
            throw IllegalArgumentException("")
        }

        return MappedRelation(
            this.items.keys.sortedBy { it.toIndex() }.zip(view.materials.keys.sortedBy { it.toIndex() }).map { (recipe, input) ->
                MappedRelationComponent(recipe, input)
            }.toSet()
        )
    }
}
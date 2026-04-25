package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
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
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.TransmuteRecipe

/**
 * A [CRecipe] wrapper for vanilla [Recipe] instances. For internal use only; instantiation is restricted to internal code.
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
        /**
         * Converts a vanilla [CraftingRecipe] to a [CVanillaRecipe].
         *
         * @param[recipe] A vanilla crafting recipe ([ShapedRecipe], [ShapelessRecipe] or [TransmuteRecipe])
         * @return[CVanillaRecipe] The converted recipe, or `null` if [recipe] is neither shaped nor shapeless
         */
        @JvmStatic
        fun fromVanilla(recipe: CraftingRecipe): CVanillaRecipe? {
            return when (recipe) {
                is ShapedRecipe -> fromShaped(recipe)
                is ShapelessRecipe -> fromShapeless(recipe)
                is TransmuteRecipe -> fromTransmute(recipe)
                else -> null
            }
        }

        /**
         * Converts a vanilla shaped recipe to a CRecipe.
         *
         * @param[recipe] A target shaped recipe
         * @return[CVanillaRecipe] A result CRecipe
         * @since 5.0.11
         */
        @JvmStatic
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
         * @return[CVanillaRecipe] A result CRecipe
         * @since 5.0.11
         */
        @JvmStatic
        fun fromShapeless(recipe: ShapelessRecipe): CVanillaRecipe {
            return CVanillaRecipe(
                recipe.key.namespace + recipe.key.key,
                shapelessToItems(recipe.choiceList),
                CRecipe.Type.SHAPELESS,
                results = listOf(ResultSupplier.timesSingle(recipe.result)),
                original = recipe
            )
        }

        /**
         * Converts a vanilla transmute recipe to a CRecipe.
         *
         * @param[recipe] A target transmute recipe
         * @return[CVanillaRecipe] A result CRecipe
         * @since 5.0.21
         */
        @JvmStatic
        fun fromTransmute(recipe: TransmuteRecipe): CVanillaRecipe {
            val source: CMatter = CMatterImpl(
                name = "${recipe.key.namespace}, ${recipe.key.key} input (source)",
                candidate = choiceToCandidates(recipe.input),
                predicates = setOf(choiceToPredicate(recipe.input))
            )
            val catalyst: CMatter = CMatterImpl(
                name = "${recipe.key.namespace}, ${recipe.key.key} input (catalyst)",
                candidate = choiceToCandidates(recipe.material),
                predicates = setOf(choiceToPredicate(recipe.material))
            )

            val resultSupplier = ResultSupplier { context ->
                val source: ItemStack = context.mapped
                    .filter { (_, item) -> recipe.input.test(item) }
                    .map { (_, item) -> item }
                    .firstOrNull()
                    ?: return@ResultSupplier emptyList()

                listOf(source.withType(recipe.result.type))
            }

            return CVanillaRecipe(
                name = recipe.key.namespace + recipe.key.key,
                items = CoordinateComponent.getN(2).zip(listOf(source, catalyst)).toMap(),
                type = CRecipe.Type.SHAPELESS,
                results = listOf(resultSupplier),
                original = recipe
            )
        }

        private fun shapelessToItems(choices: List<RecipeChoice>): Map<CoordinateComponent, CMatter> {
            val result: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
            choices.withIndex().forEach { (index, choice) ->
                val candidates: Set<Material> = choiceToCandidates(choice)
                val matter: CMatter = CMatterImpl(
                    candidates.firstOrNull()?.name ?: "vanilla matter default name",
                    candidate = candidates,
                    predicates = setOf(choiceToPredicate(choice))
                )
                result[CoordinateComponent.fromIndex(index)] = matter
            }
            return result
        }

        private fun shapeToItems(
            shape: Array<out String>,
            map: Map<Char, RecipeChoice>
        ): Map<CoordinateComponent, CMatter> {
            val result: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
            for ((y: Int, line: String) in shape.withIndex()) {
                for ((x: Int, c: Char) in line.toList().withIndex()) {
                    val choice: RecipeChoice = map[c] ?: continue
                    val candidates: Set<Material> = choiceToCandidates(choice)
                    val matter: CMatter = CMatterImpl(
                        name = candidates.firstOrNull()?.name ?: "vanilla matter default name",
                        candidate = candidates,
                        predicates = setOf(choiceToPredicate(choice))
                    )
                    result[CoordinateComponent(x, y)] = matter
                }
            }
            return result
        }

        private fun choiceToPredicate(choice: RecipeChoice): CMatterPredicate {
            return CMatterPredicate { context ->
                return@CMatterPredicate choice.test(context.input)
            }
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

    /**
     * Builds a [MappedRelation] by matching this recipe's slots against the given [view] in index order.
     *
     * @param[view] The crafting grid snapshot to relate against
     * @return[MappedRelation] Slot mapping between this recipe and the view
     * @throws[IllegalArgumentException] If the non-air item count in [view] does not match this recipe's slot count,
     *   or if the view spans more than 4 columns or 4 rows
     */
    fun relateWith(view: CraftView): MappedRelation {
        val viewSize: Int = view.materials.filterNot { it.value.type.isAir }.count()
        if (viewSize != this.items.size) {
            throw IllegalArgumentException("'view#materials' size is not equals with this recipes size. (view size: ${viewSize}, recipe size: ${items.size})")
        }

        val dx: Int = view.materials.keys.maxOf { it.x } - view.materials.keys.minOf { it.x }
        val dy: Int = view.materials.keys.maxOf { it.y } - view.materials.keys.minOf { it.y }
        if (dx > 3 || dy > 3) {
            throw IllegalArgumentException("view spans more than 4 columns or 4 rows. (dx: $dx, dy: $dy)")
        }

        return MappedRelation(
            this.items.keys.sortedBy { it.toIndex() }.zip(view.materials.keys.sortedBy { it.toIndex() }).map { (recipe, input) ->
                MappedRelationComponent(recipe, input)
            }.toSet()
        )
    }
}
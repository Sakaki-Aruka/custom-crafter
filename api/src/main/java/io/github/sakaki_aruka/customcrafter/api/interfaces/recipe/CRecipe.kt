package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack

/**
 * This interface's implementing types can be used as recipes for CustomCrafter.
 *
 * @param[name] Name of this recipe
 * @param[items] Mapping of CMatter and those coordinates on crafting slots
 * @param[containers] Containers what run on success to search and crafting
 * @param[results] List of [ResultSupplier] what provide items to players
 * @param[filters] List of [CRecipeFilter] what run on search process
 * @param[type] Type of this recipe. See [CRecipeType]
 *
 * @see[CMatter]
 * @see[CRecipeContainer]
 * @see[ResultSupplier]
 * @see[CRecipeFilter]
 * @see[CRecipeType]
 */
interface CRecipe {
    val name: String
    val items: Map<CoordinateComponent, CMatter>
    val containers: List<CRecipeContainer>?
    val results: List<ResultSupplier>?
    val filters: List<CRecipeFilter<CMatter>>?
    val type: CRecipeType

    /**
     * Returns this [CRecipe] is a valid or not.
     *
     * CRecipe's default implementation checks below conditions.
     * - [CRecipe.items] size (in range 1 to 36 ?)
     * - contained [CMatter] are all valid (Do all [CRecipe.items] elements pass [CMatter.isValidMatter]?)
     *
     * ```kotlin
     * // (Usage)
     * val recipe: CRecipe = ~~~
     * recipe.isValidRecipe().exceptionOrNull()?.let{ throw it }
     * ```
     * @return[Result] Result of check
     * @since 5.0.15
     */
    fun isValidRecipe(): Result<Unit> {
        return if (this.items.isEmpty() || this.items.size > 36) {
            Result.failure(IllegalStateException("'items' must contain 1 to 36 valid CMatters."))
        } else if (this.items.values.any { matter -> matter.isValidMatter().isFailure }) {
            val builder = StringBuilder()
            for ((c, matter) in this.items.entries) {
                val t: Throwable = matter.isValidMatter().exceptionOrNull()
                    ?: continue
                builder.append("[items] x: ${c.x}, y: ${c.y}, ${t.message} ${System.lineSeparator()}")
            }
            Result.failure(IllegalStateException(builder.toString()))
        } else {
            Result.success(Unit)
        }
    }

    /**
     * replace [items]
     *
     * @param[newItems] new items what are replace old.
     * @return[CRecipe] created new recipe.
     */
    fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipe

    /**
     * Minimal requires input items amount
     *
     * Default implementation exists
     *
     * @return[Int] Minimal requires input items amount
     * @since 5.0.15
     */
    fun requiresInputItemAmountMin(): Int = this.items.size

    /**
     * Maximum requires input items amount. Inclusive
     *
     * Default implementation exists
     *
     * @return[Int] Maximum requires input items amount
     * @since 5.0.15
     */
    fun requiresInputItemAmountMax(): Int = this.items.size

    /**
     * Runs all [containers] if it is not null.
     *
     * @param[context] Context of CRecipeContainer
     * @see[CRecipeContainer]
     * @see[CRecipeContainer.Context]
     */
    fun runNormalContainers(context: CRecipeContainer.Context) {
        containers?.let { containers ->
            containers.filter { container -> container.predicate(context) }
                .forEach { container -> container.consumer(context) }
        }
    }

    /**
     * This setting is only used for multiple candidate display when it enabled.
     *
     * This func returns false on default.
     *
     * @return[Boolean] default shift clicked
     * @since 5.0.8
     */
    fun multipleCandidateDisplaySettingDefaultShiftClicked(): Boolean = false

    /**
     * This setting is only used for multiple candidate display when it enabled.
     *
     * This func returns 1 on default.
     *
     * @return[Int] default called time
     * @since 5.0.8
     */
    fun multipleCandidateDisplaySettingDefaultCalledTimes(): Int = 1

    /**
     * Returns results of suppliers made
     *
     * @param[context] Context of ResultSupplier
     * @return[MutableList] Generated items list (`MutableList<ItemStack>`). If no item supplier applied, returns an empty list.
     * @see[ResultSupplier]
     * @see[ResultSupplier.Context]
     */
    fun getResults(context: ResultSupplier.Context): MutableList<ItemStack> {
        return results?.let { suppliers ->
            suppliers.map { s -> s.f(context) }.flatten().toMutableList()
        } ?: mutableListOf()
    }

    /**
     * Returns min amount
     *
     * @param[map] Input items mapping
     * @param[relation] Coordinate relations in input items and recipe matters
     * @param[isCraftGUI] Called from Crafting GUI or not
     * @param[shift] Use Shift-Key (Batch Crafting) or not
     * @param[withoutMass] Use mass-marked CMatters to min amount calculation or not
     * @param[includeAir] Use Material#AIR to min amount calculation or not
     * @since 5.0.10
     */
    fun getMinAmount(
        map: Map<CoordinateComponent, ItemStack>,
        relation: MappedRelation,
        isCraftGUI: Boolean,
        shift: Boolean,
        withoutMass: Boolean = true,
        includeAir: Boolean = false
    ): Int? {
        if (!shift) return 1
        var amount = Int.MAX_VALUE
        for ((c, matter) in this.items) {
            val inputCoordinate: CoordinateComponent = relation.components.firstOrNull { it.recipe == c }
                ?.input
                ?: CoordinateComponent(Int.MIN_VALUE, Int.MIN_VALUE)
            val item: ItemStack = // map[c] ?: ItemStack.empty()
                if (inputCoordinate == CoordinateComponent(Int.MIN_VALUE, Int.MIN_VALUE)) ItemStack.empty()
                else map[inputCoordinate] ?: ItemStack.empty()
            if (!includeAir && item.type.isEmpty) {
                continue
            } else if (withoutMass && matter.mass) {
                continue
            }

            val q: Int =
                if (matter.mass) 1
                else item.amount
            if (q < amount) {
                amount = q
            }
        }
        return amount.takeIf { it != Int.MAX_VALUE } ?: 1
    }

    fun getSlots(): List<Int> {
        return if (this.type == CRecipeType.NORMAL) {
            items.keys.map { c -> c.toIndex() }.sorted()
        } else {
            // Amorphous
            (0..<this.items.size).sorted()
        }
    }
}
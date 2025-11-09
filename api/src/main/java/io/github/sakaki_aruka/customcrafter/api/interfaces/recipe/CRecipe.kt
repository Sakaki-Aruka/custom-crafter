package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeContainerImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * This interface's implementing types can be used as recipes for CustomCrafter.
 */
interface CRecipe {
    val name: String
    val items: Map<CoordinateComponent, CMatter>
    val containers: List<CRecipeContainerImpl>?
    val results: List<ResultSupplier>?
    val filters: Set<CRecipeFilter<CMatter>>?
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
     * runs all [containers] if it is not null.
     *
     * @param[crafterID] a crafter's uuid
     * @param[relate] an input inventory and [CRecipe] coordinates relation.
     * @param[mapped] coordinates and input items relation.
     * @param[results] generated results by this recipe.
     */
    fun runNormalContainers(
        crafterID: UUID,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        results: MutableList<ItemStack>,
        isMultipleDisplayCall: Boolean
    ) {
        containers?.let { containers ->
            containers.filter { container ->
                container.predicate(
                    CRecipeContainer.Context(crafterID, relate, mapped, results, isMultipleDisplayCall)
            ) }.forEach { container ->
                container.consumer(
                    CRecipeContainer.Context(crafterID, relate, mapped, results, isMultipleDisplayCall)
                )
            }
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
     * returns results of suppliers made
     *
     * @param[crafterID] a crafter's uuid
     * @param[relate] an input inventory and [CRecipe] coordinates relation.
     * @param[mapped] coordinates and input items relation.
     * @param[shiftClicked] shift clicked or not
     * @param[calledTimes] how many times called this
     * @param[isMultipleDisplayCall] called from multiple craft result candidate collector or not (since 5.0.8)
     * @return[MutableList<ItemStack>] generated items list. if no item supplier applied, returns an empty list.
     */
    fun getResults(
        crafterID: UUID,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        shiftClicked: Boolean,
        calledTimes: Int,
        isMultipleDisplayCall: Boolean
    ): MutableList<ItemStack> {
        return results?.let { suppliers ->
            val list: MutableList<ItemStack> = mutableListOf()
            suppliers.map { s ->
                list.addAll(s.f(
                    ResultSupplier.Config(
                        relate,
                        mapped,
                        shiftClicked,
                        calledTimes,
                        list,
                        crafterID,
                        isMultipleDisplayCall
                    )
                ))
            }
            list
        } ?: mutableListOf()
    }

    /**
     * Get min amount
     * @since 5.0.10
     */
    fun getMinAmount(
        map: Map<CoordinateComponent, ItemStack>,
        isCraftGUI: Boolean,
        shift: Boolean
    ): Int? {
        if (!shift) return 1
        return items.entries
            .filter { (c, _) -> c in Converter.getAvailableCraftingSlotComponents() }
            .filter { (c, _) -> map[c] != null }
            .filter { (_, matter) -> !matter.mass }
            .minOfOrNull { (c, matter) -> map[c]!!.amount / matter.amount }
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
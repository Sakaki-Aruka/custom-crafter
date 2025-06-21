package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeContainer
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
    val containers: List<CRecipeContainer>?
    val results: List<ResultSupplier>?
    val filters: Set<CRecipeFilter<CMatter>>?
    val type: CRecipeType
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
            containers.filter { c ->
                c.predicate is CRecipeContainer.NormalPredicate
                        && c.consumer is CRecipeContainer.NormalConsumer
            }.filter { c ->
                (c.predicate as CRecipeContainer.NormalPredicate)(crafterID, relate, mapped, results, isMultipleDisplayCall)
            }.forEach { c ->
                (c.consumer as CRecipeContainer.NormalConsumer)(crafterID, relate, mapped, results, isMultipleDisplayCall)
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
                list.addAll(s.func.invoke(
                    ResultSupplier.NormalConfig(
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
}
package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.`object`.result.ResultSupplier
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
    val filters: Set<CRecipeFilter<*>>?
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
    fun runContainers(
        crafterID: UUID,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        results: MutableList<ItemStack>
    ) {
        containers?.forEach { container ->
            container.run(crafterID, relate, mapped, results)
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
                    ResultSupplier.Config(
                        crafterID,
                        relate,
                        mapped,
                        list,
                        shiftClicked,
                        calledTimes,
                        isMultipleDisplayCall
                    )
                ))
            }
            list
        } ?: mutableListOf()
    }
}
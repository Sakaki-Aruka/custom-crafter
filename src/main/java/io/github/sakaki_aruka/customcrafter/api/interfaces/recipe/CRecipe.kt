package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

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
     * returns results of suppliers made
     *
     * @param[crafterID] a crafter's uuid
     * @param[relate] an input inventory and [CRecipe] coordinates relation.
     * @param[mapped] coordinates and input items relation.
     * @return[MutableList<ItemStack>] generated items list. if no item supplier applied, returns an empty list.
     */
    fun getResults(
        crafterID: UUID,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        shiftClicked: Boolean,
        calledTimes: Int
    ): MutableList<ItemStack> {
        return results?.let { suppliers ->
            val list: MutableList<ItemStack> = mutableListOf()
            suppliers.map { s ->
                list.addAll(s.func.invoke(crafterID, relate, mapped, list, shiftClicked, calledTimes))
            }
            list
        } ?: mutableListOf()
    }
}
package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.AutoCraftUI
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * A subinterface of [CRecipe] for Auto Crafting feature.
 *
 * @since 5.0.10
 */
interface AutoCraftRecipe: CRecipe {

    /**
     * Plugin name
     *
     * Your plugin has to provide own name.
     * @since 5.0.11
     */
    val publisherPluginName: String

    /**
     * This lambda provides a displayed item to players when they set auto crafting recipes.
     */
    val autoCraftDisplayItemProvider: (Player) -> ItemStack

    /**
     * [ResultSupplier] to provide result items on auto crafting.
     */
    val autoCraftResults: List<ResultSupplier>?

    /**
     * [CRecipeContainer] what are run on auto crafting.
     */
    val autoCraftContainers: List<CRecipeContainer>?

    fun getSignedDisplayItem(player: Player): ItemStack {
        val item: ItemStack = this.autoCraftDisplayItemProvider(player).takeIf { i -> i.isEmpty } ?: AutoCraftUI.UNDEFINED
        //
    }

    /**
     * Returns items what are made by [autoCraftResults].
     * @param[block] AutoCrafter block
     * @param[relate] An input inventory and [CRecipe] coordinates relation.
     * @param[mapped] Input coordinates and input items relation.
     * @param[calledTimes] Called times ( = minimal input item's amount).
     * @return[MutableList] (MutableList<ItemStack>) Generated items list. If no item supplier applied, returns an empty list.
     * @since 5.0.10-1
     */
    fun getAutoCraftResults(
        block: Block,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        calledTimes: Int
    ): MutableList<ItemStack> {
        val list: MutableList<ItemStack> = mutableListOf()
        autoCraftResults?.map { supplier ->
            supplier.func(ResultSupplier.AutoCraftConfig(
                relation = relate,
                mapped = mapped,
                calledTimes = calledTimes,
                list = list,
                autoCrafterBlock = block
            ))
        }?.forEach { itemList ->
            list.addAll(itemList)
        }
        return list
    }

    fun runAutoCraftContainers(
        block: Block,
        relate: MappedRelation,
        mapped: Map<CoordinateComponent, ItemStack>,
        results: MutableList<ItemStack>
    ) {
        autoCraftContainers?.let { containers ->
            containers.filter { c ->
                c.predicate is CRecipeContainer.AutoCraftPredicate
                        && c.consumer is CRecipeContainer.AutoCraftConsumer
            }.filter { c->
                (c.predicate as CRecipeContainer.AutoCraftPredicate)(block, relate, mapped, results)
            }.forEach { c ->
                (c.consumer as CRecipeContainer.AutoCraftConsumer)(block, relate, mapped, results)
            }
        }
    }
}
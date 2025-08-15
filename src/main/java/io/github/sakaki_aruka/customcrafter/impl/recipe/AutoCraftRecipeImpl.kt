package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl.Companion.getDefaultFilters
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * A default [CRecipe] and [AutoCraftRecipe] implementation class.
 *
 * Suggested to get plugin name with the following code.
 * ```Kotlin
 * val publisherPluginName: String = <YOUR PLUGIN INSTANCE>.getPluginMeta.name
 * ```
 *
 * @param[name] A name of this recipe.
 * @param[items] Elements of this recipe.
 * @param[type] A type of this recipe. (Normal or Amorphous (=Shapeless))
 * @parma[publisherPluginName] Name of a publisher plugin
 * @param[autoCraftDisplayItemProvider] Item provider for auto-craft display
 * @param[containers] Containers of this recipe. (default = null)
 * @param[results]
 * @param[filters]
 * @param[autoCraftResults]
 * @param[autoCraftContainers]
 */
data class AutoCraftRecipeImpl(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipeType,
    override val publisherPluginName: String,
    override val autoCraftDisplayItemProvider: (Player, Block) -> ItemStack = AutoCraftRecipe.getDefaultDisplayItemProvider(name),
    override val containers: List<CRecipeContainer>? = null,
    override val results: List<ResultSupplier>? = null,
    override val filters: Set<CRecipeFilter<CMatter>>? = getDefaultFilters(),
    override val autoCraftResults: List<ResultSupplier>? = null,
    override val autoCraftContainers: List<CRecipeContainer>? = null
): AutoCraftRecipe {
    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipe {
        return AutoCraftRecipeImpl(
            this.name,
            newItems,
            this.type,
            this.publisherPluginName,
            this.autoCraftDisplayItemProvider,
            this.containers,
            this.results,
            this.filters,
            this.autoCraftResults,
            this.autoCraftContainers
        )
    }
}

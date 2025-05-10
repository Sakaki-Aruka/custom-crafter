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
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A default [CRecipe] and [AutoCraftRecipe] implementation class.
 *
 * @param[name] A name of this recipe.
 * @param[items] Elements of this recipe.
 * @param[type] A type of this recipe. (Normal or Amorphous (=Shapeless))
 * @param[autoCraftID] An id of this recipe.
 * @param[containers] Containers of this recipe. (default = null)
 * @param[results]
 */
data class AutoCraftRecipeImpl(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val type: CRecipeType,
    override val autoCraftID: UUID,
    override val autoCraftDisplayItemProvider: (Player) -> ItemStack,
    override val containers: List<CRecipeContainer>? = null,
    override val results: List<ResultSupplier>? = null,
    override val filters: Set<CRecipeFilter<CMatter>>? = getDefaultFilters(),
    override val autoCraftResults: List<ResultSupplier>? = null,
): AutoCraftRecipe {
    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipe {
        return AutoCraftRecipeImpl(
            this.name,
            newItems,
            this.type,
            this.autoCraftID,
            this.autoCraftDisplayItemProvider,
            this.containers,
            this.results,
            this.filters
        )
    }
}

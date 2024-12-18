package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.MappedRelation
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.result.ResultSupplier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

data class CRecipeImpl(
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val containers: List<CRecipeContainer>? = null,
    //override val results: List<(crafterID: UUID, relate: MappedRelation, mapped: Map<CoordinateComponent, ItemStack>) -> List<ItemStack>>? = null,
    override val results: List<ResultSupplier>? = null,
    override val type: CRecipeType,
): CRecipe {
    //
    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): CRecipeImpl {
        return CRecipeImpl(this.name, newItems, this.containers, this.results, this.type)
    }
}
package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import java.util.UUID

/**
 * A subinterface of [CRecipe] for Auto Crafting feature.
 *
 * @since 5.0.10
 */
interface AutoCraftingIdentifier: CRecipe {
    val autoCraftID: UUID
}
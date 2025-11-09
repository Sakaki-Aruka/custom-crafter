package io.github.sakaki_aruka.customcrafter.api.objects.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer

/**
 * Default implementation of [CRecipeContainer]
 * @see[CRecipeContainer]
 */
data class CRecipeContainerImpl(
    override val predicate: (CRecipeContainer.Context) -> Boolean,
    override val consumer: (CRecipeContainer.Context) -> Unit
): CRecipeContainer
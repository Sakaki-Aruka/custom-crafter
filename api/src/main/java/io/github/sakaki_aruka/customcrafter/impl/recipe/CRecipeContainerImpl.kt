package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer

/**
 * Default implementation of [io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer]
 * @see[io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer]
 */
open class CRecipeContainerImpl(
    override val predicate: (CRecipeContainer.Context) -> Boolean,
    override val consumer: (CRecipeContainer.Context) -> Unit
): CRecipeContainer
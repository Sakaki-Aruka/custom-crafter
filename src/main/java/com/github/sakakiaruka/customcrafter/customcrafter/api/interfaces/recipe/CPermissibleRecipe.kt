package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe

interface CPermissibleRecipe: CRecipe {
    /**
     * Represents an interface that is a permissible-recipe.
     */
    val permission: CRecipePermission
}
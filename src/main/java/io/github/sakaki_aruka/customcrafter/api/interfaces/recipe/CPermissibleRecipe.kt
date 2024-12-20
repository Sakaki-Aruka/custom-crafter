package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

interface CPermissibleRecipe: CRecipe {
    /**
     * Represents an interface that is a permissible-recipe.
     */
    val permission: CRecipePermission
}
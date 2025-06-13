package io.github.sakaki_aruka.customcrafter.api.objects.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe

/**
 * a type of [CRecipe].
 * NORMAL: means shaped recipe
 * AMORPHOUS: means shapeless recipe
 */
enum class CRecipeType {
    NORMAL,
    AMORPHOUS
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe

/**
 * a type of [CRecipe].
 * NORMAL: means shaped recipe
 * AMORPHOUS: means shapeless recipe
 */
enum class CRecipeType {
    NORMAL,
    AMORPHOUS
}
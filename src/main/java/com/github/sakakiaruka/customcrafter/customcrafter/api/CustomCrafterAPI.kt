package com.github.sakakiaruka.customcrafter.customcrafter.api

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipe

object CustomCrafterAPI {
    val REGISTERED_C_RECIPES: MutableSet<CRecipe> = mutableSetOf()

    const val VERSION: String = "0.1"
    const val IS_STABLE: Boolean = true
    const val IS_BETA: Boolean = false
    val AUTHORS: Set<String> = setOf("Sakaki-Aruka")
}
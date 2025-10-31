package io.github.sakaki_aruka.customcrafter.api.objects.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe

/**
 * a type of [CRecipe].
 * NORMAL: means shaped recipe
 * AMORPHOUS: means shapeless recipe
 */
enum class CRecipeType(
    val type: String
) {
    NORMAL("NORMAL"),
    AMORPHOUS("AMORPHOUS");

    companion object {
        fun of(type: String, ignoreCase: Boolean = true): CRecipeType? {
            return if (ignoreCase) {
                entries.firstOrNull { t -> t.type == type }
            } else {
                entries.firstOrNull { t -> t.type.equals(type, ignoreCase = true) }
            }
        }
    }
}
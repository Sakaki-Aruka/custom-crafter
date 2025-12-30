package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import java.util.UUID

interface SearchPreprocessor {
    class Context(
        val input: CraftView,
        val crafterID: UUID,
        val candidates: List<CRecipe>,
        override val session: SearchSession
    ): SearchKVClient.Context

    fun run(ctx: Context)
}
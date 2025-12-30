package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import java.util.UUID

interface CRecipePredicate: SearchKVClient {
    class Context(
        val input: CraftView,
        val crafterID: UUID,
        val recipe: CRecipe,
        val relation: MappedRelation,
        override val session: SearchSession
    ): SearchKVClient.Context

    fun test(ctx: Context): Boolean
}
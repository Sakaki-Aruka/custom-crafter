package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import java.util.UUID

interface CRecipePredicate {
    class Context(
        val input: CraftView,
        val crafterID: UUID,
        val recipe: CRecipe,
        val relation: MappedRelation
    )

    fun test(ctx: Context): Boolean
}
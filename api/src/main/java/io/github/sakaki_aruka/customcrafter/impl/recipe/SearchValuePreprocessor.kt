package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search.SearchPreprocessor
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search.SearchKVClient

interface SearchValuePreprocessor: SearchPreprocessor, SearchKVClient {
    override fun run(ctx: SearchPreprocessor.Context)
}
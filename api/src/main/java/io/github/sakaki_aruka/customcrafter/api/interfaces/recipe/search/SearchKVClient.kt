package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search

import io.github.sakaki_aruka.customcrafter.internal.search.SearchKV
import java.util.UUID

interface SearchKVClient {
    interface Context {
        val session: SearchSession
    }
    fun put(session: SearchSession, key: String, value: Any) {
        put(session.id, key, value)
    }

    private fun put(id: UUID, key: String, value: Any) {
        SearchKV.put(id, key, value)
    }

    fun <T> value(session: SearchSession, key: String): T? {
        return value(session.id, key)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> value(id: UUID, key: String): T? {
        return SearchKV.value(id, key) as? T
    }
}
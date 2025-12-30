package io.github.sakaki_aruka.customcrafter.internal.search

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.UUID

internal object SearchKV {
    private val cache: Cache<String, Any> = CacheBuilder<String, Any>.newBuilder()
        .maximumSize(10000)
        .build()

    private fun key(id: UUID, key: String): String = "${id.toString()}-$key"

    fun put(id: UUID, key: String, value: Any) {
        cache.put(key(id, key), value)
    }

    fun value(id: UUID, key: String): Any? {
        return cache.getIfPresent(key(id, key))
    }

    fun clear(id: UUID, key: String) {
        cache.invalidate(key(id, key))
    }

    fun clear(id: UUID) {
        cache.invalidateAll(cache.asMap().keys.filter { it.startsWith(id.toString()) })
    }
}
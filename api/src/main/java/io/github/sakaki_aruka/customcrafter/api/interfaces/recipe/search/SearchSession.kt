package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.search

import io.github.sakaki_aruka.customcrafter.internal.search.SearchKV
import java.util.UUID

/**
 * When called "Recipe Clicked (Crafted)"
 * @since 5.0.17
 */
class SearchSession(
    internal val id: UUID
) {
    companion object {
        /**
         * SYNCHRONOUS search-session
         * @since 5.0.17
         */
        val SYNC_SESSION = SearchSession(id = UUID.fromString("00000000-0000-0000-0000-000000000000"))
    }

    /**
     * Returns is this session running on sync or not.
     * @return[Boolean] This session is sync run or not
     * @since 5.0.17
     */
    fun isSyncSession(): Boolean {
        return this.id == SYNC_SESSION
    }

    /**
     * This will delete all values associated with this session that exist in the temporary Key-Value store, SearchKV. Please call this when you register values using the [SearchKVClient.put] function.
     * @since 5.0.17
     */
    fun finish() {
        SearchKV.clear(this.id)
    }
}
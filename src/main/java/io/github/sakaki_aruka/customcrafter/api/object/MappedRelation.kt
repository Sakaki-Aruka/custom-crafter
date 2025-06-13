package io.github.sakaki_aruka.customcrafter.api.`object`

import kotlinx.serialization.Serializable

/**
 * A relation holder.
 *
 * @param[components] Relation components. ->[MappedRelationComponent]
 */

@Serializable
data class MappedRelation internal constructor(
    val components: Set<MappedRelationComponent>
)

package io.github.sakaki_aruka.customcrafter.api.`object`

/**
 * A relation holder.
 *
 * @param[components] Relation components. ->[MappedRelationComponent]
 */

data class MappedRelation internal constructor(
    val components: Set<MappedRelationComponent>
)

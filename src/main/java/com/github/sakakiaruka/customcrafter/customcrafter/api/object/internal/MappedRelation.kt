package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal

/**
 * A relation holder.
 *
 * @param[components] Relation components. ->[MappedRelationComponent]
 */

data class MappedRelation internal constructor(
    val components: Set<MappedRelationComponent>
)

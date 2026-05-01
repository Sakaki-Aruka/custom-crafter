package io.github.sakaki_aruka.customcrafter.objects


/**
 * A relation holder.
 *
 * @param[components] Relation components. ->[MappedRelationComponent]
 */

data class MappedRelation (
    val components: Set<MappedRelationComponent>
)

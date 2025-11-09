package io.github.sakaki_aruka.customcrafter.api.objects.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate

/**
 * Default implementation of [CMatterPredicate]
 *
 * A part of [CMatter]
 *
 * @param[predicate] A lambda expression what receives [Context] and returns inspection result
 * @see[CMatterPredicate]
 * @see[CMatter.predicates]
 */
class CMatterPredicateImpl(
    override val predicate: (CMatterPredicate.Context) -> Boolean
): CMatterPredicate
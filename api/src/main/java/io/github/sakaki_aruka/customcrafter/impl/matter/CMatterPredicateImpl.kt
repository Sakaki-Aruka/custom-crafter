package io.github.sakaki_aruka.customcrafter.impl.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate

/**
 * Default implementation of [io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate]
 *
 * A part of [io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter]
 *
 * @param[predicate] A lambda expression what receives [Context] and returns inspection result
 * @see[io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate]
 * @see[io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter.predicates]
 */
class CMatterPredicateImpl(
    override val predicate: (CMatterPredicate.Context) -> Boolean
): CMatterPredicate
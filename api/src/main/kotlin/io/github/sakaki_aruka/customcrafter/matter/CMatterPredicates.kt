package io.github.sakaki_aruka.customcrafter.matter

/**
 * Combinator functions for composing [CMatterPredicate] instances.
 *
 * @see[CMatterPredicate]
 */
object CMatterPredicates {

    /**
     * Combines this predicate with [other] using logical AND.
     *
     * @param[other] The other predicate to combine with
     * @return[CMatterPredicate] A predicate that passes when both this predicate and [other] pass
     * @since 5.3.0
     */
    @JvmStatic
    fun CMatterPredicate.and(other: CMatterPredicate): CMatterPredicate {
        return CMatterPredicate { ctx -> this.test(ctx) && other.test(ctx) }
    }

    /**
     * Combines this predicate with [other] using logical OR.
     *
     * @param[other] The other predicate to combine with
     * @return[CMatterPredicate] A predicate that passes when either this predicate or [other] passes
     * @since 5.3.0
     */
    @JvmStatic
    fun CMatterPredicate.or(other: CMatterPredicate): CMatterPredicate {
        return CMatterPredicate { ctx -> this.test(ctx) || other.test(ctx) }
    }

    /**
     * Combines this predicate with [predicates] using logical AND.
     *
     * @param[predicates] Additional predicates to combine with
     * @return[CMatterPredicate] A predicate that passes when this predicate and every predicate in [predicates] pass
     * @since 5.3.0
     */
    @JvmStatic
    fun CMatterPredicate.allOf(vararg predicates: CMatterPredicate): CMatterPredicate {
        return CMatterPredicate { ctx -> (predicates.toList() + this).all { it.test(ctx) } }
    }

    /**
     * Combines this predicate with [predicates] and passes when at least [n] of them pass.
     *
     * @param[n] Minimum number of predicates (out of this predicate and [predicates]) that must pass
     * @param[predicates] Additional predicates to combine with
     * @return[CMatterPredicate] A predicate that passes when at least [n] of (this predicate and [predicates]) pass
     * @since 5.3.0
     */
    @JvmStatic
    fun CMatterPredicate.nOf(n: Int, vararg predicates: CMatterPredicate): CMatterPredicate {
        return CMatterPredicate { ctx -> (predicates.toList() + this).count { it.test(ctx) } >= n }
    }
}

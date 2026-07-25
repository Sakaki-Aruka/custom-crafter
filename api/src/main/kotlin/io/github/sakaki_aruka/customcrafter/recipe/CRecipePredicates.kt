package io.github.sakaki_aruka.customcrafter.recipe

/**
 * Combinator functions for composing [CRecipePredicate] instances.
 *
 * @see[CRecipePredicate]
 */
object CRecipePredicates {

    /**
     * Combines this predicate with [other] using logical AND.
     *
     * @param[other] The other predicate to combine with
     * @return[CRecipePredicate] A predicate that passes when both this predicate and [other] pass
     * @since 5.3.0
     */
    @JvmStatic
    fun CRecipePredicate.and(other: CRecipePredicate): CRecipePredicate {
        return CRecipePredicate { ctx -> this.test(ctx) && other.test(ctx) }
    }

    /**
     * Combines this predicate with [other] using logical OR.
     *
     * @param[other] The other predicate to combine with
     * @return[CRecipePredicate] A predicate that passes when either this predicate or [other] passes
     * @since 5.3.0
     */
    @JvmStatic
    fun CRecipePredicate.or(other: CRecipePredicate): CRecipePredicate {
        return CRecipePredicate { ctx -> this.test(ctx) || other.test(ctx) }
    }

    /**
     * Combines this predicate with [predicates] using logical AND.
     *
     * @param[predicates] Additional predicates to combine with
     * @return[CRecipePredicate] A predicate that passes when this predicate and every predicate in [predicates] pass
     * @since 5.3.0
     */
    @JvmStatic
    fun CRecipePredicate.allOf(vararg predicates: CRecipePredicate): CRecipePredicate {
        return CRecipePredicate { ctx -> (predicates.toList() + this).all { it.test(ctx) } }
    }

    /**
     * Combines this predicate with [predicates] and passes when at least [n] of them pass.
     *
     * @param[n] Minimum number of predicates (out of this predicate and [predicates]) that must pass
     * @param[predicates] Additional predicates to combine with
     * @return[CRecipePredicate] A predicate that passes when at least [n] of (this predicate and [predicates]) pass
     * @since 5.3.0
     */
    @JvmStatic
    fun CRecipePredicate.nOf(n: Int, vararg predicates: CRecipePredicate): CRecipePredicate {
        return CRecipePredicate { ctx -> (predicates.toList() + this).count { it.test(ctx) } >= n }
    }
}

package io.github.sakaki_aruka.customcrafter.recipe

/**
 * Combinator functions for composing [CRecipePredicate] instances.
 *
 * Every combinator derives the composed predicate's [CRecipePredicate.name] from the names of its
 * operands, so a composed predicate reports its own structure in diagnostic logs.
 *
 * @see[CRecipePredicate]
 */
object CRecipePredicates {

    /**
     * Returns [predicate] carrying [name] as its [CRecipePredicate.name].
     *
     * A lambda has no identity of its own, so wrapping it here is what makes an
     * [io.github.sakaki_aruka.customcrafter.debug.Explainer] able to report this predicate by name
     * instead of by index.
     *
     * @param[name] Label to report this predicate under
     * @param[predicate] The predicate to label
     * @return[CRecipePredicate] A predicate behaving as [predicate] and named [name]
     * @since 5.3.0
     */
    @JvmStatic
    fun named(name: String, predicate: CRecipePredicate): CRecipePredicate {
        return of(name) { ctx -> predicate.test(ctx) }
    }

    /**
     * Combines this predicate with [other] using logical AND.
     *
     * @param[other] The other predicate to combine with
     * @return[CRecipePredicate] A predicate that passes when both this predicate and [other] pass
     * @since 5.3.0
     */
    @JvmStatic
    fun CRecipePredicate.and(other: CRecipePredicate): CRecipePredicate {
        return of("(${this.name()} and ${other.name()})") { ctx -> this.test(ctx) && other.test(ctx) }
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
        return of("(${this.name()} or ${other.name()})") { ctx -> this.test(ctx) || other.test(ctx) }
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
        val merged: List<CRecipePredicate> = predicates.toList() + this
        return of("allOf(${merged.joinToString(", ") { it.name() }})") { ctx -> merged.all { it.test(ctx) } }
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
        val merged: List<CRecipePredicate> = predicates.toList() + this
        return of("nOf($n, ${merged.joinToString(", ") { it.name() }})") { ctx -> merged.count { it.test(ctx) } >= n }
    }

    private fun of(name: String, body: (CRecipePredicate.Context) -> Boolean): CRecipePredicate {
        return object : CRecipePredicate {
            override fun test(ctx: CRecipePredicate.Context): Boolean = body(ctx)
            override fun name(): String = name
        }
    }
}

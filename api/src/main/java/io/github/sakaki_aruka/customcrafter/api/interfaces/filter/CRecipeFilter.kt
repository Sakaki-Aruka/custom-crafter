package io.github.sakaki_aruka.customcrafter.api.interfaces.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import org.bukkit.inventory.ItemStack

import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.EnchantFilter
import org.bukkit.inventory.meta.ItemMeta

/**
 * candidate filters for [CRecipe].
 *
 * If you want to implement this, recommended to show implementation of [EnchantFilter] more some default filters.
 *
 * [T] must be a subtype of [CMatter].
 *
 * @param[T] a type of target class or interface.
 * @since 5.0.6
 */
interface CRecipeFilter<out T: CMatter> {

    companion object {
        /**
         * This means 'check did not require' .
         *
         * Equals `Pair(ResultType.NOT_REQUIRED, true)` .
         * @since 5.0.15
         */
        val CHECK_NOT_REQUIRED = ResultType.NOT_REQUIRED to true

        /**
         * This means 'failed to predicate checks' .
         *
         * Equals `Pair(ResultType.FAILED, false)` .
         * @since 5.0.15
         */
        val CHECK_FAILED = ResultType.FAILED to false

        /**
         * This means `all checks successful` .
         *
         * Equals `Pair(ResultType.SUCCESS, true)`
         * @since 5.0.15
         */
        val CHECKED_ALL_PASS = ResultType.SUCCESS to true

        /**
         * This means 'did all checks, but some checks failed' .
         *
         * Equals `Pair(ResultType.SUCCESS, false)`
         * @since 5.0.15
         */
        val CHECKED_NOT_PASS = ResultType.SUCCESS to false
    }

    /**
     * SUCCESS not means success to input matter check passed.
     * that only means success to pass any checks before final one.
     *
     * FAILED means failed to pass checks before final.
     *
     * NOT_REQUIRED means a provided matter does not require some checks.
     *
     * @since 5.0.7
     */
    enum class ResultType {
        SUCCESS,
        FAILED,
        NOT_REQUIRED
    }

//    /**
//     * returns a result of checks to [ItemMeta] type.
//     *
//     * e.g.
//     * ```
//     * // in EnchantStorageFilter
//     * override fun metaTypeCheck(meta: ItemMeta): Boolean {
//     *     return meta is EnchantmentStorageMeta
//     * }
//     * ```
//     *
//     * in the implementation of this is permitted to return always true in the case of needless to type check.
//     *
//     * e.g.
//     * ```
//     * // in EnchantFilter
//     * // cause you can get enchantments from ItemStack, so items has not to provide ItemMeta.
//     * override fun metaTypeCheck(meta: ItemMeta): Boolean {
//     *     return true
//     * }
//     * ```
//     *
//     * if this returns false, fail to normal candidate checks.
//     *
//     * @param[meta] checked target ItemMeta
//     * @return[Boolean] the provided meta matches the target or not
//     */
//    fun metaTypeCheck(meta: ItemMeta): Boolean


    /**
     * a candidate checker for normal recipes.
     *
     * @see[ResultType]
     *
     * @param[item] an input item
     * @param[matter] one of a recipe
     * @return[Pair] type of checks and [item] conforms [matter] or not
     */
    fun itemMatterCheck(item: ItemStack, matter: @UnsafeVariance T): Pair<ResultType, Boolean>
}
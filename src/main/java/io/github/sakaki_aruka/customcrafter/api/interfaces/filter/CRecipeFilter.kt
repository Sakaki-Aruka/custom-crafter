package io.github.sakaki_aruka.customcrafter.api.interfaces.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack

import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter.EnchantFilter
import org.bukkit.inventory.meta.ItemMeta

/**
 * candidate filters for [CRecipe].
 *
 * if you want to implement this, recommended to show implementation of [EnchantFilter] more some default filters.
 *
 * you have to set a class what is a subtype of [CMatter] to generic type [T].
 *
 * you can set custom [CMatter] implemented class to [T].
 *
 * you do not use 'internal' or 'private' visibility modifier to classes what implements this interface.
 *
 *
 * @param[T] a type of target class or interface. subtype of [CMatter].
 * @since 5.0.6
 */
interface CRecipeFilter<out T: CMatter> {

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

    /**
     * returns a result of checks to [ItemMeta] type.
     *
     * e.g.
     * ```
     * // in EnchantStorageFilter
     * override fun metaTypeCheck(meta: ItemMeta): Boolean {
     *   return meta is EnchantmentStorageMeta
     * }
     * ```
     *
     * in the implementation of this is permitted to return always true in the case of needless to type check.
     *
     * e.g.
     * ```
     * // in EnchantFilter
     * // cause you can get enchantments from ItemStack, so items has not to provide ItemMeta.
     * override fun metaTypeCheck(meta: ItemMeta): Boolean {
     *   return true
     * }
     * ```
     *
     * if this returns false, fail to normal candidate checks.
     *
     * @param[meta] checked target ItemMeta
     * @return[Boolean] the provided meta matches the target or not
     */
    fun metaTypeCheck(meta: ItemMeta): Boolean

    /**
     * a candidate filter for amorphous recipes.
     *
     * a flow of implementation
     * - collects recipe components what type is [T]
     *   - if collected is empty, returns Type.NOT_REQUIRED and an empty list
     * - collects input component what has attributes of you want to check
     *   - if collected is empty, returns Type.NOT_ENOUGH and an empty list
     * - checks collected components amount
     * - checks
     * - returns a result
     *
     * @param[mapped] an inventory input mapping
     * @param[recipe] a target recipe
     * @return[Pair] first = type of result, second = result
     */
    fun amorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe
    ): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>>

    /**
     * a candidate checker for normal recipes.
     *
     * this method has a difference with [amorphous], because this checks item one by one.
     *
     * @see[ResultType]
     *
     * @param[item] an input item
     * @param[matter] one of a recipe
     * @return[Pair] type of checks and [item] conforms [matter] or not
     */
    fun normal(item: ItemStack, matter: @UnsafeVariance T): Pair<ResultType, Boolean>
}
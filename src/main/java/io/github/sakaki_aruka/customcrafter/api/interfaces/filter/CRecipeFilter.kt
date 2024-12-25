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
 * @param[T] a type of target class or interface. this must be a subtype of [CMatter].
 */
interface CRecipeFilter<out T: CMatter> {

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
     * @param[item] an input item
     * @param[matter] one of a recipe
     * @return[Boolean] [item] conforms [matter] or not
     */
    fun normal(item: ItemStack, matter: @UnsafeVariance T): Boolean
}
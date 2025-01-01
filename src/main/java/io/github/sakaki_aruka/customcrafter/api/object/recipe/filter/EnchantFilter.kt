package io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.`object`.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.`object`.matter.enchant.EnchantStrict
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * @suppress
 * @since 5.0.6
 */
internal object EnchantFilter: CRecipeFilter<CEnchantMatter> {
    override fun metaTypeCheck(meta: ItemMeta): Boolean = true

    override fun normal(
        item: ItemStack,
        matter: CEnchantMatter
    ): Pair<CRecipeFilter.ResultType, Boolean> {
        val itemEnchants: Map<Enchantment, Int> = item.enchantments
        if (matter.enchantComponents.isNotEmpty() && itemEnchants.isEmpty()) return CRecipeFilter.ResultType.FAILED to false
        else if (matter.enchantComponents.isNotEmpty()) return CRecipeFilter.ResultType.NOT_REQUIRED to true

        return CRecipeFilter.ResultType.SUCCESS to matter.enchantComponents.all { base(itemEnchants, it) }
    }

    override fun amorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe
    ): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items
            .filter { it.value is CEnchantMatter }
            .filter { (it.value as CEnchantMatter).enchantComponents.isNotEmpty() }
            .map { it.key }

        val inputCoordinates: List<CoordinateComponent> = mapped.entries
            .filter { it.value.enchantments.isNotEmpty() }
            .map { it.key }
        val inputEnchants: MutableMap<CoordinateComponent, List<EnchantComponent>> = mutableMapOf()
        inputCoordinates.forEach { coordinate ->
            inputEnchants[coordinate] = mapped[coordinate]!!.enchantments.entries.map {
                EnchantComponent(it.key, it.value)
            }
        }

        if (recipes.size > inputCoordinates.size) {
            return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
        } else if (recipes.isEmpty()) {
            return Pair(AmorphousFilterCandidate.Type.NOT_REQUIRED, emptyList())
        }

        val map: MutableMap<Int, List<Int>> = mutableMapOf()
        for (index: Int in recipes.indices) {
            val enchants: Set<CEnchantComponent> = (recipe.items[recipes[index]]!! as CEnchantMatter).enchantComponents
            val matched: List<Int> = matchList(inputEnchants.values.toList(), enchants)
                .withIndex()
                .filter { it.value }
                .map { it.index }
            map[index] = matched
        }

        val result: MutableList<AmorphousFilterCandidate> = mutableListOf()
        for (slice in map.entries) {
            val R: CoordinateComponent = recipes[slice.key]
            val list: List<CoordinateComponent> = inputCoordinates
                .withIndex()
                .filter { slice.value.contains(it.index) }
                .map { it.value }
            if (list.isEmpty()) {
                return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
            }
            result.add(AmorphousFilterCandidate(R, list))
        }
        val type =
            if (result.isEmpty()) AmorphousFilterCandidate.Type.NOT_ENOUGH
            else AmorphousFilterCandidate.Type.SUCCESSFUL

        return Pair(type, result)
    }

    internal fun matchList(
        ins: List<List<EnchantComponent>>,
        recipes: Collection<CEnchantComponent>
    ): List<Boolean> {
        val result: MutableList<Boolean> = mutableListOf()
        for (itemContains: List<EnchantComponent> in ins) {
            var outerJudge = 0
            for (matter: CEnchantComponent in recipes) {
                var innerJudge = 0
                for (inEnchant: EnchantComponent in itemContains) {
                    when (matter.strict) {
                        EnchantStrict.INPUT -> {} // not implemented
                        EnchantStrict.NOT_STRICT -> {} // no implementation
                        EnchantStrict.ONLY_ENCHANT -> {
                            if (matter.enchantment == inEnchant.type) innerJudge++
                        }
                        EnchantStrict.STRICT -> {
                            if (matter.enchantment == inEnchant.type
                                && matter.level == inEnchant.level) innerJudge++
                        }
                    }
                }
                outerJudge += innerJudge
            }
            result.add(outerJudge == recipes.size)
        }
        return result
    }

    internal data class EnchantComponent(
        val type: Enchantment,
        val level: Int
    )

    internal fun base(
        enchants: Map<Enchantment, Int>,
        required: CEnchantComponent
    ): Boolean {
        return when (required.strict) {
            EnchantStrict.INPUT -> true
            EnchantStrict.NOT_STRICT -> true
            EnchantStrict.ONLY_ENCHANT -> {
                enchants.keys.contains(required.enchantment)
            }
            EnchantStrict.STRICT -> {
                enchants.getOrDefault(required.enchantment, -1) == required.level
            }
        }
    }

}
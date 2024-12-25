package io.github.sakaki_aruka.customcrafter.api.`object`.recipe.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.`object`.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta

/**
 * @suppress
 */
internal object EnchantStorageFilter: CRecipeFilter<CEnchantmentStoreMatter> {
    override fun metaTypeCheck(meta: ItemMeta): Boolean {
        return meta is EnchantmentStorageMeta
    }

    override fun amorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe
    ): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items
            .filter { it.value is CEnchantmentStoreMatter }
            .map { it.key }

        val inputCoordinates: List<CoordinateComponent> = mapped.entries
            .filter { it.value.itemMeta is EnchantmentStorageMeta }
            .filter { (it.value.itemMeta as EnchantmentStorageMeta).storedEnchants.isNotEmpty() }
            .map { it.key }
        val inputEnchants: MutableMap<CoordinateComponent, List<EnchantFilter.EnchantComponent>> = mutableMapOf()
        inputCoordinates.forEach { coordinate ->
            inputEnchants[coordinate] = mapped[coordinate]!!.let { item ->
                (item.itemMeta as EnchantmentStorageMeta).storedEnchants.entries.map { stored ->
                    EnchantFilter.EnchantComponent(stored.key, stored.value)
                }
            }
        }

        if (recipes.size > inputCoordinates.size) {
            return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
        } else if (recipes.isEmpty()) {
            return Pair(AmorphousFilterCandidate.Type.NOT_REQUIRED, emptyList())
        }

        val map: MutableMap<Int, List<Int>> = mutableMapOf()
        for (index: Int in recipes.indices) {
            val enchants: Set<CEnchantComponent> = (recipe.items[recipes[index]]!! as CEnchantmentStoreMatter).storedEnchantComponents
            val matched: List<Int> = EnchantFilter.matchList(inputEnchants.values.toList(), enchants)
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
            result.add(AmorphousFilterCandidate(R, list))
        }

        val type =
            if (result.isEmpty()) AmorphousFilterCandidate.Type.NOT_ENOUGH
            else AmorphousFilterCandidate.Type.SUCCESSFUL

        return Pair(type, result)
    }

    override fun normal(
        item: ItemStack,
        matter: CEnchantmentStoreMatter
    ): Boolean {
        val stored: Map<Enchantment, Int> = (item.itemMeta as EnchantmentStorageMeta).storedEnchants
        if (matter.storedEnchantComponents.isNotEmpty()
            && stored.isEmpty()) return false
        return matter.storedEnchantComponents.all { EnchantFilter.base(stored, it) }
    }
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.processor

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CEnchantMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.EnchantStrict
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

object Enchant {
    internal fun enchant(item: ItemStack, e: CEnchantMatter): Boolean {
        val itemEnchants: Map<Enchantment, Int> = item.enchantments
        if (e.enchantComponents.isNotEmpty() && itemEnchants.isEmpty()) return false
        return e.enchantComponents.all { base(itemEnchants, it) }
    }

    internal fun enchantStored(item: ItemStack, e: CEnchantmentStoreMatter): Boolean {
        val stored: Map<Enchantment, Int> = (item.itemMeta as? EnchantmentStorageMeta ?: return false).storedEnchants
        if (e.storedEnchantComponents.isNotEmpty() && stored.isEmpty()) return false
        return e.storedEnchantComponents.all { base(stored, it) }
    }

    private fun base(enchants: Map<Enchantment, Int>, required: CEnchantComponent): Boolean {
        // when (component.strict) {
        //                EnchantStrict.INPUT -> continue // not implemented
        //                EnchantStrict.NOT_STRICT -> continue
        //                EnchantStrict.ONLY_ENCHANT -> if (!itemEnchants.keys.contains(component.enchantment)) return false
        //                EnchantStrict.STRICT -> {
        //                    if (itemEnchants.getOrDefault(component.enchantment, -1) != component.level) return false
        //                }
        //            }
        return when (required.strict) {
            EnchantStrict.INPUT -> true // not implemented
            EnchantStrict.NOT_STRICT -> true
            EnchantStrict.ONLY_ENCHANT -> enchants.keys.contains(required.enchantment)
            EnchantStrict.STRICT -> enchants.getOrDefault(required.enchantment, -1) == required.level
        }
    }

    private data class EnchantComponent(
        val type: Enchantment,
        val level: Int
    )

    internal fun amorphous(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items
            .filter { it.value is CEnchantMatter }
            .map { it.key }

        val inputCoordinates: List<CoordinateComponent> = mapped.entries
            .filter { it.value.enchantments.isNotEmpty() }
            .map { it.key }
        val inputEnchants: MutableMap<CoordinateComponent, List<EnchantComponent>> = mutableMapOf()
        inputCoordinates.forEach { coordinate ->
            inputEnchants[coordinate] = mapped[coordinate]!!.enchantments.entries.map { EnchantComponent(it.key, it.value) }
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
            result.add(AmorphousFilterCandidate(R, list))
        }
        val type =
            if (result.isEmpty()) AmorphousFilterCandidate.Type.NOT_ENOUGH
            else AmorphousFilterCandidate.Type.SUCCESSFUL

        return Pair(type, result)
    }

    private fun matchList(ins: List<List<EnchantComponent>>, recipes: Collection<CEnchantComponent>): List<Boolean> {
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

    internal fun storesAmorphous(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items
            .filter { it.value is CEnchantmentStoreMatter }
            .map { it.key }

        val inputCoordinates: List<CoordinateComponent> = mapped.entries
            .filter { it.value.itemMeta is EnchantmentStorageMeta }
            .filter { (it.value.itemMeta as EnchantmentStorageMeta).storedEnchants.isNotEmpty() }
            .map { it.key }
        val inputEnchants: MutableMap<CoordinateComponent, List<EnchantComponent>> = mutableMapOf()
        inputCoordinates.forEach { coordinate ->
            inputEnchants[coordinate] = mapped[coordinate]!!.let { item ->
                (item.itemMeta as EnchantmentStorageMeta).storedEnchants.entries.map { stored ->
                    EnchantComponent(stored.key, stored.value)
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
            result.add(AmorphousFilterCandidate(R, list))
        }

        val type =
            if (result.isEmpty()) AmorphousFilterCandidate.Type.NOT_ENOUGH
            else AmorphousFilterCandidate.Type.SUCCESSFUL

        return Pair(type, result)
    }
}
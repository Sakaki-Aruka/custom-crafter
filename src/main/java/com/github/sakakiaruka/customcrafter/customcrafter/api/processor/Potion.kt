package com.github.sakakiaruka.customcrafter.customcrafter.api.processor

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect

object Potion {
    internal fun potion(item: ItemStack, p: CPotionMatter): Boolean {
        val meta: PotionMeta = item.itemMeta as? PotionMeta ?: return false
        val effects: MutableList<PotionEffect> = meta.customEffects
        meta.basePotionType?.let {
            effects.addAll(it.potionEffects)
        }
        if (!p.potionComponents.all { it.enabledBottleTypes.contains(relate(item.type)) }) return false

        return p.potionComponents.all { c ->
            when (c.strict) {
                CPotionComponent.PotionStrict.INPUT -> true
                CPotionComponent.PotionStrict.NOT_STRICT -> true
                CPotionComponent.PotionStrict.ONLY_EFFECT -> {
                    effects.any { e -> c.effect.type == e.type }
                }
                CPotionComponent.PotionStrict.STRICT -> {
                    effects.any { e -> c.effect.type == e.type
                            && c.effect.amplifier == e.amplifier }
                }
            }
        }
    }

    private fun relate(type: Material): CPotionComponent.PotionBottleType? {
        return when (type) {
            Material.POTION -> CPotionComponent.PotionBottleType.NORMAL
            Material.LINGERING_POTION -> CPotionComponent.PotionBottleType.LINGERING
            Material.SPLASH_POTION -> CPotionComponent.PotionBottleType.SPLASH
            else -> null
        }
    }

    internal fun amorphous(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items
            .filter { it.value is CPotionMatter }
            .map { it.key }

        val inputCoordinates: List<CoordinateComponent> = mapped.entries
            .filter { it.value.itemMeta is PotionMeta }
            .map { it.key }
        val inputPotions: MutableMap<CoordinateComponent, List<PotionEffect>> = mutableMapOf()
        inputCoordinates.forEach { coordinate ->
            val meta: PotionMeta = (mapped[coordinate]!!.itemMeta as PotionMeta)
            val effects: MutableSet<PotionEffect> = mutableSetOf()
            meta.customEffects.forEach { e -> effects.add(e) }
            meta.basePotionType?.let { e -> effects.addAll(e.potionEffects) }
            inputPotions[coordinate] = effects.toList()
        }

        if (recipes.size > inputCoordinates.size) {
            return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
        } else if (recipes.isEmpty()) {
            return Pair(AmorphousFilterCandidate.Type.NOT_REQUIRED, emptyList())
        }

        val map: MutableMap<Int, List<Int>> = mutableMapOf() // recipeIndex, inputIndices
        for (index: Int in recipes.indices) {
            val potions: Set<CPotionComponent> = (recipe.items[recipes[index]]!! as CPotionMatter).potionComponents
            val matched: List<Int> = matchList(inputPotions.values.toList(), potions)
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

    private fun matchList(ins: List<List<PotionEffect>>, recipes: Collection<CPotionComponent>): List<Boolean> {
        val result: MutableList<Boolean> = mutableListOf() // relate in-recipe
        ins.forEach { inEffects ->
            result.add(
                inEffects.all { inEffectSingle ->
                    recipes.all { recipeComponentSingle ->
                        when (recipeComponentSingle.strict) {
                            CPotionComponent.PotionStrict.INPUT -> true // not implemented
                            CPotionComponent.PotionStrict.NOT_STRICT -> true
                            CPotionComponent.PotionStrict.ONLY_EFFECT -> {
                                inEffectSingle.type == recipeComponentSingle.effect.type
                            }
                            CPotionComponent.PotionStrict.STRICT -> {
                                inEffectSingle.type == recipeComponentSingle.effect.type
                                        && inEffectSingle.amplifier == recipeComponentSingle.effect.amplifier
                            }
                        }
                    }
                }
            )
        }
        return result
    }
}
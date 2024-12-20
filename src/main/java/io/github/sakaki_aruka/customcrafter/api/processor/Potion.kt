package io.github.sakaki_aruka.customcrafter.api.processor

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CPotionMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CPermissibleRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.`object`.matter.potion.CPotionComponent
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect

/**
 * @suppress
 */
object Potion {
    internal fun potion(item: ItemStack, p: CPotionMatter): Boolean {
        val meta: PotionMeta = item.itemMeta as? PotionMeta ?: return false
        val effects: MutableList<PotionEffect> = meta.customEffects
        meta.basePotionType?.let {
            effects.addAll(it.potionEffects)
        }

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

    internal fun amorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe
    ): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items
            .filter { it.value is CPotionMatter }
            .filter { (it.value as CPotionMatter).potionComponents.isNotEmpty() }
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

    private fun effectsValidate(
        inList: List<PotionEffect>,
        components: Collection<CPotionComponent>
    ): Boolean {
        return components.all { c ->
            inList.any { inEffect ->
                when (c.strict) {
                    CPotionComponent.PotionStrict.INPUT -> true
                    CPotionComponent.PotionStrict.NOT_STRICT -> true
                    CPotionComponent.PotionStrict.ONLY_EFFECT -> {
                        c.effect.type == inEffect.type
                    }
                    CPotionComponent.PotionStrict.STRICT -> {
                        c.effect.type == inEffect.type
                                && c.effect.amplifier == inEffect.amplifier
                    }
                }
            }
        }
    }

    // a list of List<PotionEffect> (= effects are contained one potion.)
    // validate effects List<PotionEffect> and Collection<CPotionComponent>
    private fun matchList(
        ins: List<List<PotionEffect>>,
        recipes: Collection<CPotionComponent>
    ): List<Boolean> {
        val result: MutableList<Boolean> = mutableListOf() // relate in-recipe
        ins.forEach { inEffects ->
            result.add(effectsValidate(inEffects, recipes))
        }
        return result
    }
}
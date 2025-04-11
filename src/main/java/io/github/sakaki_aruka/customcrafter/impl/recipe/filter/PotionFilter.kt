package io.github.sakaki_aruka.customcrafter.impl.recipe.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CPotionMatter
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect

/**
 * A default [CPotionMatter] filter implementation.
 * @since 5.0.6
 */
object PotionFilter: CRecipeFilter<CPotionMatter> {
    override fun metaTypeCheck(meta: ItemMeta): Boolean {
        return meta is PotionMeta
    }

    override fun normal(
        item: ItemStack,
        matter: CPotionMatter
    ): Pair<CRecipeFilter.ResultType ,Boolean> {
        val meta: PotionMeta = item.itemMeta as PotionMeta
        val effects: MutableList<PotionEffect> = meta.customEffects
        meta.basePotionType?.let {
            effects.addAll(it.potionEffects)
        }

        return CRecipeFilter.ResultType.SUCCESS to matter.potionComponents.all { c ->
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

}
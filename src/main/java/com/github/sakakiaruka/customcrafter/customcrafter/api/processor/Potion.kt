package com.github.sakakiaruka.customcrafter.customcrafter.api.processor

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionComponent
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

        for (c: CPotionComponent in p.potionComponents) {
            for (datum in c.data) {
                when (datum.value) {
                    CPotionComponent.PotionStrict.INPUT -> continue
                    CPotionComponent.PotionStrict.NOT_STRICT -> continue
                    CPotionComponent.PotionStrict.ONLY_EFFECT -> {
                        if (!effects.any { it.type == datum.key }) return false
                    }
                    CPotionComponent.PotionStrict.STRICT -> {
                        if (!effects.any { it.type == datum.key && it.duration == datum.key.duration}) return false
                    }
                }
            }
        }
        return true
    }

    private fun relate(type: Material): CPotionComponent.PotionBottleType? {
        return when (type) {
            Material.POTION -> CPotionComponent.PotionBottleType.NORMAL
            Material.LINGERING_POTION -> CPotionComponent.PotionBottleType.LINGERING
            Material.SPLASH_POTION -> CPotionComponent.PotionBottleType.SPLASH
            else -> null
        }
    }
}
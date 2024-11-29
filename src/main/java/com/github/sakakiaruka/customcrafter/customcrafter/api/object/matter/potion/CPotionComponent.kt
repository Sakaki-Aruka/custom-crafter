package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion

import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

data class CPotionComponent(
    val data: Map<PotionEffect, PotionStrict>,
    val bottleType: PotionBottleType,
    val enabledBottleTypes: Set<PotionBottleType>
) {
    enum class PotionStrict {
        INPUT,
        NOT_STRICT,
        ONLY_EFFECT,
        STRICT
    }

    enum class PotionBottleType(val material: Material) {
        NORMAL(Material.POTION),
        LINGERING(Material.LINGERING_POTION),
        SPLASH(Material.SPLASH_POTION)
    }
}
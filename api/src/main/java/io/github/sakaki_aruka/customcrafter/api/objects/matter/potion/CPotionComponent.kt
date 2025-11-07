package io.github.sakaki_aruka.customcrafter.api.objects.matter.potion

import org.bukkit.potion.PotionEffect

/**
 * A component of CMatter's potion.
 *
 * @constructor
 * A default constructor of CPotionComponent.
 *
 * @param[effect] A type of potion
 * @param[strict] required level
 */
data class CPotionComponent(
    val effect: PotionEffect,
    val strict: PotionStrict
) {

    /**
     * An enum what means potion required level.
     *
     * ONLY_EFFECT: The system checks only type.
     * STRICT: The system checks amplifier and type.
     */
    enum class PotionStrict {
        ONLY_EFFECT,
        STRICT
    }
}
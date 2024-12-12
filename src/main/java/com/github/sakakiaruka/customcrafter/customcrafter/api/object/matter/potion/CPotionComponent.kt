package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion

import org.bukkit.Material
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
     * (INPUT: only for internal uses)
     * NOT_STRICT: The system does not check amplifier and type.
     * ONLY_EFFECT: The system checks only type.
     * STRICT: The system checks amplifier and type.
     */
    enum class PotionStrict {
        INPUT,
        NOT_STRICT,
        ONLY_EFFECT,
        STRICT
    }
}
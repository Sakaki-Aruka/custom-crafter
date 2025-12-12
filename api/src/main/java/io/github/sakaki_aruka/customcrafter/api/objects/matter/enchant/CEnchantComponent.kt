package io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant

import org.bukkit.enchantments.Enchantment

/**
 * A component of CMatter's enchant.
 *
 * @param[level] enchantment level
 * @param[enchantment] enchantment type
 * @param[strict] required level. Look [Strict]
 */
data class CEnchantComponent(
    val level: Int,
    val enchantment: Enchantment,
    val strict: Strict,
) {
    /**
     * An enum what means enchant required level.
     *
     * - ONLY_ENCHANT: The system checks only level.
     * - STRICT: The system checks level and type.
     */
    enum class Strict {
        ONLY_ENCHANT,
        STRICT
    }
}
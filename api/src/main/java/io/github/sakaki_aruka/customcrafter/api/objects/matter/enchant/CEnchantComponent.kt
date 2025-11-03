package io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant

import org.bukkit.enchantments.Enchantment

/**
 * An enum what means enchant required level.
 *
 * - ONLY_ENCHANT: The system checks only level.
 * - STRICT: The system checks level and type.
 */
enum class EnchantStrict {
    ONLY_ENCHANT,
    STRICT
}

/**
 * A component of CMatter's enchant.
 *
 * @param[level] enchantment level
 * @param[enchantment] enchantment type
 * @param[strict] required level. Look [EnchantStrict]
 */
data class CEnchantComponent(
    val level: Int,
    val enchantment: Enchantment,
    val strict: EnchantStrict,
)
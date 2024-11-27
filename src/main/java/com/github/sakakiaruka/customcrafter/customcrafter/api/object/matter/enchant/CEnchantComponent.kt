package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant

import org.bukkit.enchantments.Enchantment

enum class EnchantStrict {
    INPUT,
    NOT_STRICT,
    ONLY_ENCHANT,
    STRICT
}

data class CEnchantComponent(
    val level: Int,
    val enchantment: Enchantment,
    val strict: EnchantStrict,
)
package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent

/**
 * This interface's implementing types will be subject to checks for available enchantments when used as materials.
 */
interface CEnchantmentStoreMatter: CMatter {
    val storedEnchantComponents: Set<CEnchantComponent>
}
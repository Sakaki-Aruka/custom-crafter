package io.github.sakaki_aruka.customcrafter.matter.enchant

import io.github.sakaki_aruka.customcrafter.matter.CMatter

/**
 * This interface's implementing types will be subject to checks for available enchantments when used as materials.
 */
interface CEnchantmentStoreMatter: CMatter {
    val storedEnchantComponents: Set<CEnchantComponent>
}
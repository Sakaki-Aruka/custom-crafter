package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent

/**
 * This interface's implementing types will be subject to checks for available enchantments when used as materials.
 */
interface CEnchantmentStoreMatter: CMatter {
    val storedEnchantComponents: Set<CEnchantComponent>
}
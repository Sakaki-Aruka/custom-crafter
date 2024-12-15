package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent

/**
 * This interface's implementing types will be subject to checks regarding enchantments when used as materials.
 * This type includes enchantComponents what is a Set of [CEnchantComponent].
 */
interface CEnchantMatter: CMatter {
    val enchantComponents: Set<CEnchantComponent>
}
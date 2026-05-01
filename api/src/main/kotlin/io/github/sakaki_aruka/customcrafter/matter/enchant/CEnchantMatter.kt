package io.github.sakaki_aruka.customcrafter.matter.enchant

import io.github.sakaki_aruka.customcrafter.matter.CMatter

/**
 * This interface's implementing types will be subject to checks regarding enchantments when used as materials.
 *
 * This type includes enchantComponents what is a Set of [CEnchantComponent].
 */
interface CEnchantMatter: CMatter {
    val enchantComponents: Set<CEnchantComponent>
}
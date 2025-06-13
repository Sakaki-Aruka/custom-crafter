package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent

/**
 * This interface's implementing types will be subject to checks regarding enchantments when used as materials.
 *
 * This type includes enchantComponents what is a Set of [CEnchantComponent].
 */
interface CEnchantMatter: CMatter {
    val enchantComponents: Set<CEnchantComponent>
}
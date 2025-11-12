package io.github.sakaki_aruka.customcrafter.impl.matter.enchant

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterPredicateImpl
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import org.bukkit.Material

/**
 * A default [CMatter], [CEnchantMatter], [CEnchantmentStoreMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[enchantComponents] enchant predicates for applied enchantments. set of [CEnchantComponent].
 * @param[storedEnchantComponents] enchant predicates for stored enchantments. set of [CEnchantComponent].
 */
data class CEnchantBothMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val storedEnchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicateImpl>? = null
): CEnchantMatter, CEnchantmentStoreMatter

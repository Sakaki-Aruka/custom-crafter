package io.github.sakaki_aruka.customcrafter.matter.enchant

import io.github.sakaki_aruka.customcrafter.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.matter.CEnchantmentStoreMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatter
import io.github.sakaki_aruka.customcrafter.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.matter.CMatterImpl
import org.bukkit.Material

/**
 * A default [CMatter], [CEnchantMatter], [CEnchantmentStoreMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[anyAmount] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[enchantComponents] enchant predicates for applied enchantments. set of [CEnchantComponent].
 * @param[storedEnchantComponents] enchant predicates for stored enchantments. set of [CEnchantComponent].
 */
open class CEnchantBothMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val storedEnchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val anyAmount: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
): CEnchantMatter, CEnchantmentStoreMatter

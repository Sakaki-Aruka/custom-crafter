package io.github.sakaki_aruka.customcrafter.api.`object`.matter.enchant

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.`object`.matter.CMatterPredicate
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer

/**
 * A default [CMatter], [CEnchantMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[enchantComponents] enchant predicates for applied enchantments. set of [CEnchantComponent].
 */
data class CEnchantMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = null

): CMatter, CEnchantMatter {
    override fun asOne(): CEnchantMatterImpl {
        return CEnchantMatterImpl(
            name = name,
            candidate = candidate,
            amount = 1,
            mass = mass,
            predicates = predicates,
            enchantComponents = enchantComponents
        )
    }
}

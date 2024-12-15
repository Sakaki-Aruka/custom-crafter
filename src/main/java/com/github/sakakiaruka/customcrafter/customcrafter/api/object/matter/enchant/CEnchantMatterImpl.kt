package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CEnchantMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterPredicate
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
 * @param[persistentDataContainer] [PersistentDataContainer]
 * @param[enchantComponents] enchant predicates for applied enchantments. set of [CEnchantComponent].
 */
data class CEnchantMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int,
    override val mass: Boolean,
    override val predicates: Set<CMatterPredicate>?,
    override val persistentDataContainer: PersistentDataContainer?,
    override val enchantComponents: Set<CEnchantComponent>
): CMatter, CEnchantMatter {
    override fun asOne(): CEnchantMatterImpl {
        return CEnchantMatterImpl(
            name,
            candidate,
            amount = 1,
            mass,
            predicates,
            persistentDataContainer,
            enchantComponents
        )
    }
}

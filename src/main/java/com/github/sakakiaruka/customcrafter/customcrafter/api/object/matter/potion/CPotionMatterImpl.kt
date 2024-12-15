package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterPredicate
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer

/**
 * A default [CMatter], [CPotionMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[persistentDataContainer] [PersistentDataContainer]
 * @param[potionComponents] potion predicates for contained. set of [CPotionComponent].
 */
data class CPotionMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int,
    override val mass: Boolean,
    override val predicates: Set<CMatterPredicate>?,
    override val persistentDataContainer: PersistentDataContainer?,
    override val potionComponents: Set<CPotionComponent>
): CMatter, CPotionMatter {
    override fun asOne(): CPotionMatterImpl {
        return CPotionMatterImpl(
            name,
            candidate,
            amount = 1,
            mass,
            predicates,
            persistentDataContainer,
            potionComponents
        )
    }
}

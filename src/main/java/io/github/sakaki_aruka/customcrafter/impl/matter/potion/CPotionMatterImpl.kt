package io.github.sakaki_aruka.customcrafter.impl.matter.potion

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CPotionMatter
import io.github.sakaki_aruka.customcrafter.api.objects.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import org.bukkit.Material

/**
 * A default [CMatter], [CPotionMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[potionComponents] potion predicates for contained. set of [CPotionComponent].
 */
data class CPotionMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val potionComponents: Set<CPotionComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = null,
): CMatter, CPotionMatter {
    override fun asOne(): CPotionMatterImpl {
        return CPotionMatterImpl(
            name = name,
            candidate = candidate,
            amount = 1,
            mass = mass,
            predicates = predicates,
            potionComponents = potionComponents
        )
    }
}

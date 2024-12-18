package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer

/**
 * A default [CMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount (default = 1)
 * @param[mass] this matter is mass or not (default = false)
 * @param[predicates] if in checks, this matter requires to pass these all. (default = null)
 */
data class CMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = null,
): CMatter {
    override fun asOne(): CMatterImpl {
        return CMatterImpl(
            this.name,
            this.candidate,
            amount = 1,
            this.mass,
            this.predicates,
        )
    }
}
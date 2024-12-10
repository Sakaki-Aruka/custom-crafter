package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer

/**
 * A default [CMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[persistentDataContainer] [PersistentDataContainer]
 */
data class CMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int,
    override val mass: Boolean,
    override val predicates: Set<CMatterPredicate>?,
    override val persistentDataContainer: PersistentDataContainer?,
): CMatter {
    override fun asOne(): CMatterImpl {
        return CMatterImpl(
            this.name,
            this.candidate,
            amount = 1,
            this.mass,
            this.predicates,
            this.persistentDataContainer
        )
    }
}
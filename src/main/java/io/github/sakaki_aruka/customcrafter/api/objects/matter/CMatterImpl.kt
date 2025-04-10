package io.github.sakaki_aruka.customcrafter.api.objects.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import org.bukkit.Material

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
    /**
     * @see[CMatter.asOne]
     */
    override fun asOne(): CMatterImpl {
        return CMatterImpl(
            this.name,
            this.candidate,
            amount = 1,
            this.mass,
            this.predicates,
        )
    }

    companion object {
        /**
         * returns single candidate [CMatterImpl].
         *
         * its name is [material]'s name. `material.name`.
         *
         * @param[material] a candidate of this matter.
         */
        fun single(material: Material): CMatter {
            return CMatterImpl(
                name = material.name,
                candidate = setOf(material)
            )
        }
    }
}
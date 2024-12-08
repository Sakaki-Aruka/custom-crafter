package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer

data class CMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int,
    override val mass: Boolean,
    override val predicates: Set<CMatterPredicate>?,
    override val persistentDataContainer: PersistentDataContainer?,
): CMatter {
    override fun asOne(): CMatter {
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
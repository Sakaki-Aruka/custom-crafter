package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CMatter
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer

data class CMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int,
    override val mass: Boolean,
    override val containers: Set<CMatterContainer>?,
    override val persistentDataContainer: PersistentDataContainer?,
): CMatter {
    //
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterContainer
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer

class CPotionMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int,
    override val mass: Boolean,
    override val containers: Set<CMatterContainer>?,
    override val persistentDataContainer: PersistentDataContainer?,
    override val potionComponents: Set<CPotionComponent>
) : CMatter, CPotionMatter {
}
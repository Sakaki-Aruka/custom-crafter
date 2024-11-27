package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterContainer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

interface CMatter {
    val name: String
    val candidate: Set<Material>
    val amount: Int
    val mass: Boolean
    val containers: Set<CMatterContainer>?
    val persistentDataContainer: PersistentDataContainer?

    fun hasContainers(): Boolean = containers != null
    fun hasPDC(): Boolean = persistentDataContainer != null

    fun predicatesResult(player: Player, items: List<ItemStack>): Boolean {
        return containers?.all { c -> c.predicates.all { p -> p(player, items) } } ?: true
    }

}
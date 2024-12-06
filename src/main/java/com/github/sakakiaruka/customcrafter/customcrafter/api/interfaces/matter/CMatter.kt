package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterPredicate
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

interface CMatter {
    val name: String
    val candidate: Set<Material>
    val amount: Int
    val mass: Boolean
    val predicates: Set<CMatterPredicate>?
    val persistentDataContainer: PersistentDataContainer?

    fun hasContainers(): Boolean = predicates != null
    fun hasPDC(): Boolean = persistentDataContainer != null

    fun predicatesResult(item: ItemStack, container: PersistentDataContainer): Boolean {
        return predicates?.all { c -> c.predicates.all { p -> p(item, container) } } ?: true
    }

    fun asOne(): CMatter

}
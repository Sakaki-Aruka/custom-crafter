package io.github.sakaki_aruka.customcrafter.api.interfaces.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import org.bukkit.inventory.ItemStack

interface NormalFilter<T: CMatter> {
    fun normal(item: ItemStack, matter: T): Boolean
}
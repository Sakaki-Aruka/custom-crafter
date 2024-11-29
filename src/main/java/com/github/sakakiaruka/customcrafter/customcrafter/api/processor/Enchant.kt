package com.github.sakakiaruka.customcrafter.customcrafter.api.processor

import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CEnchantMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.CEnchantmentStoreMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.EnchantStrict
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

object Enchant {
    internal fun enchant(item: ItemStack, e: CEnchantMatter): Boolean {
        val itemEnchants: Map<Enchantment, Int> = item.enchantments
        if (e.enchantComponents.isNotEmpty() && itemEnchants.isEmpty()) return false
        return e.enchantComponents.all { base(itemEnchants, it) }
    }

    internal fun enchantStored(item: ItemStack, e: CEnchantmentStoreMatter): Boolean {
        val stored: Map<Enchantment, Int> = (item.itemMeta as? EnchantmentStorageMeta ?: return false).storedEnchants
        if (e.storedEnchantComponents.isNotEmpty() && stored.isEmpty()) return false
        return e.storedEnchantComponents.all { base(stored, it) }
    }

    private fun base(enchants: Map<Enchantment, Int>, required: CEnchantComponent): Boolean {
        // when (component.strict) {
        //                EnchantStrict.INPUT -> continue // not implemented
        //                EnchantStrict.NOT_STRICT -> continue
        //                EnchantStrict.ONLY_ENCHANT -> if (!itemEnchants.keys.contains(component.enchantment)) return false
        //                EnchantStrict.STRICT -> {
        //                    if (itemEnchants.getOrDefault(component.enchantment, -1) != component.level) return false
        //                }
        //            }
        return when (required.strict) {
            EnchantStrict.INPUT -> true // not implemented
            EnchantStrict.NOT_STRICT -> true
            EnchantStrict.ONLY_ENCHANT -> enchants.keys.contains(required.enchantment)
            EnchantStrict.STRICT -> enchants.getOrDefault(required.enchantment, -1) == required.level
        }
    }
}
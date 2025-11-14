package io.github.sakaki_aruka.customcrafter.impl.recipe.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta

/**
 * A default [CEnchantmentStoreMatter] filter implementation.
 * @since 5.0.6
 */
object EnchantStorageFilter: CRecipeFilter<CEnchantmentStoreMatter> {
    override fun itemMatterCheck(
        item: ItemStack,
        matter: CEnchantmentStoreMatter
    ): Pair<CRecipeFilter.ResultType, Boolean> {
        val stored: Map<Enchantment, Int> = (item.itemMeta as EnchantmentStorageMeta).storedEnchants
        if (matter.storedEnchantComponents.isEmpty()) {
            return CRecipeFilter.CHECK_NOT_REQUIRED
        } else if (stored.isEmpty()) {
            return CRecipeFilter.CHECK_FAILED
        }

        return CRecipeFilter.ResultType.SUCCESS to matter.storedEnchantComponents.all { EnchantFilter.base(stored, it) }
    }
}
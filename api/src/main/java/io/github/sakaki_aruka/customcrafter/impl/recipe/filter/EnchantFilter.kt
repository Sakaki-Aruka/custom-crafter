package io.github.sakaki_aruka.customcrafter.impl.recipe.filter

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.EnchantStrict
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * A default [CEnchantMatter] filter implementation.
 * @since 5.0.6
 */
object EnchantFilter: CRecipeFilter<CEnchantMatter> {
    override fun itemMatterCheck(
        item: ItemStack,
        matter: CEnchantMatter
    ): Pair<CRecipeFilter.ResultType, Boolean> {
        val itemEnchants: Map<Enchantment, Int> = item.enchantments
        if (matter.enchantComponents.isEmpty()) {
            return CRecipeFilter.CHECK_NOT_REQUIRED
        } else if (itemEnchants.isEmpty()) {
            return CRecipeFilter.CHECK_FAILED
        }
        return CRecipeFilter.ResultType.SUCCESS to matter.enchantComponents.all { base(itemEnchants, it) }
    }

    internal fun base(
        enchants: Map<Enchantment, Int>,
        required: CEnchantComponent
    ): Boolean {
        return when (required.strict) {
            EnchantStrict.ONLY_ENCHANT -> {
                enchants.keys.contains(required.enchantment)
            }
            EnchantStrict.STRICT -> {
                enchants.getOrDefault(required.enchantment, -1) == required.level
            }
        }
    }

}
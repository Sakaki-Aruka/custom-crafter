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
    override fun metaTypeCheck(meta: ItemMeta): Boolean = true

    override fun normal(
        item: ItemStack,
        matter: CEnchantMatter
    ): Pair<CRecipeFilter.ResultType, Boolean> {
        val itemEnchants: Map<Enchantment, Int> = item.enchantments
        if (matter.enchantComponents.isNotEmpty() && itemEnchants.isEmpty()) return CRecipeFilter.ResultType.FAILED to false
        else if (matter.enchantComponents.isNotEmpty()) return CRecipeFilter.ResultType.NOT_REQUIRED to true

        return CRecipeFilter.ResultType.SUCCESS to matter.enchantComponents.all { base(itemEnchants, it) }
    }

    internal fun base(
        enchants: Map<Enchantment, Int>,
        required: CEnchantComponent
    ): Boolean {
        return when (required.strict) {
            EnchantStrict.INPUT -> true
            EnchantStrict.NOT_STRICT -> true
            EnchantStrict.ONLY_ENCHANT -> {
                enchants.keys.contains(required.enchantment)
            }
            EnchantStrict.STRICT -> {
                enchants.getOrDefault(required.enchantment, -1) == required.level
            }
        }
    }

}
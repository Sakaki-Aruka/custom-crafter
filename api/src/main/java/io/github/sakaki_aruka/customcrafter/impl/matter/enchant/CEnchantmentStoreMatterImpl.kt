package io.github.sakaki_aruka.customcrafter.impl.matter.enchant

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterPredicateImpl
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.EnchantmentStorageMeta

/**
 * A default [CMatter], [CEnchantmentStoreMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[storedEnchantComponents] enchant predicates for stored enchantments. set of [CEnchantComponent].
 */
data class CEnchantmentStoreMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val storedEnchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
): CEnchantmentStoreMatter {
    companion object {
        /**
         * Default CEnchantStoreMatter components checker implementation on CMatterPredicate
         *
         * @since 5.0.15
         */
        val DEFAULT_ENCHANT_STORE_CHECKER = CMatterPredicateImpl { ctx ->
            if (ctx.input.type.isEmpty) {
                return@CMatterPredicateImpl true
            }

            val enchantStoreMatter = ctx.matter as? CEnchantmentStoreMatter
                ?: return@CMatterPredicateImpl true

            if (enchantStoreMatter.storedEnchantComponents.isEmpty()) {
                return@CMatterPredicateImpl true
            } else if (ctx.input.itemMeta !is EnchantmentStorageMeta) {
                return@CMatterPredicateImpl false
            }

            val sources: Map<Enchantment, Int> = (ctx.input.itemMeta as EnchantmentStorageMeta).storedEnchants.entries
                .associate { it.key to it.value }

            return@CMatterPredicateImpl enchantStoreMatter.storedEnchantComponents.all { component ->
                CEnchantMatterImpl.enchantBaseCheck(sources, component)
            }
        }
    }
}

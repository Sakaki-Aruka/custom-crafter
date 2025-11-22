package io.github.sakaki_aruka.customcrafter.impl.matter.enchant

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterPredicateImpl
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.EnchantStrict
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

/**
 * A default [CMatter], [CEnchantMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[enchantComponents] enchant predicates for applied enchantments. set of [CEnchantComponent].
 */
open class CEnchantMatterImpl(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
): CEnchantMatter {
    companion object {
        /**
         * @since 5.0.15
         */
        val DEFAULT_ENCHANT_CHECKER = CMatterPredicateImpl { ctx ->
            if (ctx.input.type.isAir) {
                return@CMatterPredicateImpl true
            }
            val enchantMatter = ctx.matter as? CEnchantMatter
                ?: return@CMatterPredicateImpl true

            if (enchantMatter.enchantComponents.isEmpty()) {
                return@CMatterPredicateImpl true
            } else if (ctx.input.itemMeta.enchants.isEmpty()) {
                return@CMatterPredicateImpl false
            }

            val sources: MutableMap<Enchantment, Int> = mutableMapOf()
            ctx.input.enchantments.entries.forEach { (type, level) -> sources[type] = level }
            ctx.input.itemMeta.enchants.entries.forEach { (type, level) -> sources[type] = level }
            return@CMatterPredicateImpl enchantMatter.enchantComponents.all { component ->
                enchantBaseCheck(sources, component)
            }
        }

        internal fun enchantBaseCheck(
            enchants: Map<Enchantment, Int>,
            required: CEnchantComponent
        ): Boolean {
            return when (required.strict) {
                EnchantStrict.ONLY_ENCHANT -> enchants.containsKey(required.enchantment)
                EnchantStrict.STRICT -> {
                    enchants.getOrDefault(required.enchantment, -1) == required.level
                }
            }
        }
    }
}

package io.github.sakaki_aruka.customcrafter.impl.matter.enchant

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
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
open class CEnchantMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val enchantComponents: Set<CEnchantComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates()
): CEnchantMatter {
    companion object {
        /**
         * Default CEnchantMatter components checker implementation on CMatterPredicate
         *
         * @since 5.0.15
         */
        @JvmField
        val DEFAULT_ENCHANT_CHECKER = CMatterPredicate { ctx ->
            if (ctx.input.type.isAir) {
                return@CMatterPredicate true
            }
            val enchantMatter = ctx.matter as? CEnchantMatter
                ?: return@CMatterPredicate true

            if (enchantMatter.enchantComponents.isEmpty()) {
                return@CMatterPredicate true
            } else if (ctx.input.itemMeta.enchants.isEmpty()) {
                return@CMatterPredicate false
            }

            val sources: MutableMap<Enchantment, Int> = mutableMapOf()
            ctx.input.enchantments.entries.forEach { (type, level) -> sources[type] = level }
            ctx.input.itemMeta.enchants.entries.forEach { (type, level) -> sources[type] = level }
            return@CMatterPredicate enchantMatter.enchantComponents.all { component ->
                enchantBaseCheck(sources, component)
            }
        }

        internal fun enchantBaseCheck(
            enchants: Map<Enchantment, Int>,
            required: CEnchantComponent
        ): Boolean {
            return when (required.strict) {
                CEnchantComponent.Strict.ONLY_ENCHANT -> enchants.containsKey(required.enchantment)
                CEnchantComponent.Strict.STRICT -> {
                    enchants.getOrDefault(required.enchantment, -1) == required.level
                }
            }
        }
    }
}

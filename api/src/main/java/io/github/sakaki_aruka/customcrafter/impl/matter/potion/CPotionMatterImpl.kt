package io.github.sakaki_aruka.customcrafter.impl.matter.potion

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CPotionMatter
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterPredicateImpl
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import org.bukkit.Material
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType

/**
 * A default [CMatter], [CPotionMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount
 * @param[mass] this matter is mass or not
 * @param[predicates] if in checks, this matter requires to pass these all.
 * @param[potionComponents] potion predicates for contained. set of [CPotionComponent].
 */
open class CPotionMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val potionComponents: Set<CPotionComponent>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = CMatterImpl.defaultMatterPredicates(),
): CPotionMatter {
    companion object {
        /**
         * Default CPotionMatter components checker implementation on CMatterPredicate
         *
         * @since 5.0.15
         */
        @JvmField
        val DEFAULT_POTION_CHECKER: CMatterPredicate = CMatterPredicateImpl { ctx ->
            if (ctx.input.type.isAir) {
                return@CMatterPredicateImpl true
            }
            val potionMatter = ctx.matter as? CPotionMatter
                ?: return@CMatterPredicateImpl true

            if (potionMatter.potionComponents.isEmpty()) {
                return@CMatterPredicateImpl true
            } else if (ctx.input.itemMeta !is PotionMeta) {
                // CPotionComponent required, but the input does not have potion effects
                return@CMatterPredicateImpl false
            }

            // Key: Type of potion, Value: Effect level
            val sources: MutableMap<PotionEffectType, Int> = mutableMapOf()
            val potionMeta: PotionMeta = ctx.input.itemMeta as PotionMeta
            potionMeta.basePotionType?.let { p ->
                p.potionEffects.forEach { effect -> sources[effect.type] = effect.amplifier }
            }
            potionMeta.customEffects.takeIf { it.isNotEmpty() }?.let { list ->
                list.forEach { effect -> sources[effect.type] = effect.amplifier }
            }

            return@CMatterPredicateImpl potionMatter.potionComponents.all { component ->
                when (component.strict) {
                    CPotionComponent.Strict.ONLY_EFFECT -> sources.containsKey(component.effect.type)
                    CPotionComponent.Strict.STRICT -> {
                        sources.containsKey(component.effect.type)
                                && sources.getValue(component.effect.type) == component.effect.amplifier
                    }
                }
            }
        }
    }
}

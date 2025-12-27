package io.github.sakaki_aruka.customcrafter.impl.matter

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantmentStoreMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.potion.CPotionMatterImpl
import org.bukkit.Material

/**
 * A default [CMatter] implemented class.
 *
 * @param[name] matter name
 * @param[candidate] matter candidate materials
 * @param[amount] matter amount (default = 1)
 * @param[mass] this matter is mass or not (default = false)
 * @param[predicates] if in checks, this matter requires to pass these all. (default = Enchant, EnchantStorage, Potion checker contains)
 */
open class CMatterImpl @JvmOverloads constructor(
    override val name: String,
    override val candidate: Set<Material>,
    override val amount: Int = 1,
    override val mass: Boolean = false,
    override val predicates: Set<CMatterPredicate>? = defaultMatterPredicates(),
): CMatter {
    companion object {
        /**
         * Returns default elements (enchant, enchant storage, potion) predicate set
         * @return[Set] Returns a default elements checker set
         * @see[CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER]
         * @see[CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER]
         * @see[CPotionMatterImpl.DEFAULT_POTION_CHECKER]
         * @since 5.0.15
         */
        @JvmStatic
        fun defaultMatterPredicates(): Set<CMatterPredicate> {
            return setOf(
                CEnchantMatterImpl.DEFAULT_ENCHANT_CHECKER,
                CEnchantmentStoreMatterImpl.DEFAULT_ENCHANT_STORE_CHECKER,
                CPotionMatterImpl.DEFAULT_POTION_CHECKER
            )
        }

        /**
         * Returns [CMatterImpl] build from specified materials.
         *
         * ```kotlin
         * // below 2 matters are same
         * val matter = CMatterImpl.of(Material.STONE, Material.COBBLESTONE)
         *
         * val matter = CMatterImpl(
         *     name = "STONE-COBBLESTONE",
         *     candidate = setOf(Material.STONE, Material.COBBLESTONE),
         *     amount = 1,
         *     mass = false,
         *     predicates = null
         * )
         * ```
         *
         * @param[materials] Candidate materials
         * @return[CMatterImpl] Built matter
         * @throws[IllegalStateException] Throws when [materials] is empty and contains invalid material
         */
        @JvmStatic
        fun of(vararg materials: Material): CMatterImpl {
            val matter = CMatterImpl(
                name = materials.joinToString("-") { m -> m.name },
                candidate = materials.toSet()
            )

            val checkResult: Result<Unit> = matter.isValidMatter()
            checkResult.exceptionOrNull()?.let { t -> throw t }
            return matter
        }

        /**
         * returns single candidate [CMatterImpl].
         *
         * its name is [material]'s name. `material.name`.
         *
         * ```kotlin
         * // below 2 matters are same
         * val matter = CMatterImpl.single(Material.STONE)
         *
         * val matter = CMatterImpl(
         *     name = "STONE",
         *     candidate = setOf(Material.STONE),
         *     amount = 1,
         *     mass = false,
         *     predicates = null
         * )
         * ```
         *
         * @param[material] a candidate of this matter.
         */
        @JvmStatic
        fun single(material: Material): CMatterImpl = of(material)

        /**
         * Returns multi candidate [CMatterImpl].
         *
         * ```kotlin
         * // below 2 matters are same
         * val matter = CMatterImpl.multi(Material.STONE, Material.COBBLESTONE)
         *
         * val matter = CMatterImpl(
         *     name = "STONE-COBBLESTONE",
         *     candidate = setOf(Material.STONE, Material.COBBLESTONE),
         *     amount = 1,
         *     mass = false,
         *     predicates = null
         * )
         * ```
         *
         * @param[materials] Candidate materials
         * @return[CMatterImpl] Built matter
         * @throws[IllegalStateException] Throws when [materials] is empty and contains invalid material
         */
        @JvmStatic
        fun multi(vararg materials: Material): CMatterImpl = of(*materials)
    }
}
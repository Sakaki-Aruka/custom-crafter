package online.aruka.demo.register

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.impl.util.AsyncUtil.fromBukkitMainThread
import online.aruka.demo.Demo
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import java.util.concurrent.Callable

object ShapelessRecipeProvider {
    fun glowBerry(): CRecipe {
        val glowstone: CMatter = CMatterImpl.single(Material.GLOWSTONE_DUST)
        val berry: CMatter = CMatterImpl.single(Material.SWEET_BERRIES)
        /*
         * # -> glowstone
         * + -> sweet berries
         *
         * #+
         *
         * returns 1x glow berries
         */
        return CRecipeImpl.shapeless(
            name = "glow berry recipe",
            items = listOf(glowstone, berry),
            results = listOf(
                ResultSupplier.timesSingle(ItemStack.of(Material.GLOW_BERRIES))
            )
        )
    }

    fun infinityIronBlockExtract(): CRecipe {
        val infinityIronBlock: CMatter = CMatterImpl(
            name = "infinity iron block",
            candidate = setOf(Material.IRON_BLOCK),
            predicates = setOf(CMatterPredicate { ctx ->
                ctx.input.itemMeta.persistentDataContainer.has(
                    NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                    PersistentDataType.INTEGER
                )
            })
        )
        val supplier = ResultSupplier { ctx ->
            val amount: Int = ctx.mapped.values.first().itemMeta.persistentDataContainer.getOrDefault(
                NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                PersistentDataType.INTEGER,
                0
            )
            if (amount == 0) {
                emptyList()
            } else {
                listOf(ItemStack.of(Material.IRON_BLOCK, amount))
            }
        }
        return CRecipeImpl.shapeless(
            name = "infinity iron block extract recipe",
            items = listOf(infinityIronBlock),
            results = listOf(supplier)
        )
    }

    fun extractPotion(): CRecipe {
        val potion: CMatter = CMatterImpl.of(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION)
        val extractor = ResultSupplier { ctx ->
            val potionMeta: PotionMeta = ctx.mapped.values.first().itemMeta as? PotionMeta
                ?: return@ResultSupplier emptyList()
            val effects: MutableList<PotionEffect> = mutableListOf()
            potionMeta.basePotionType?.let { base -> effects.addAll(base.potionEffects) }
            if (potionMeta.hasCustomEffects()) {
                effects.addAll(potionMeta.customEffects)
            }

            Callable {
                Bukkit.getPlayer(ctx.crafterID)?.addPotionEffects(effects)
            }.fromBukkitMainThread()

            emptyList()
        }
        val supplier = ResultSupplier.timesSingle(ItemStack.of(Material.GLASS_BOTTLE))
        return CRecipeImpl.shapeless(
            name = "potion effect extractor recipe",
            items = listOf(potion),
            results = listOf(supplier, extractor)
        )
    }
}
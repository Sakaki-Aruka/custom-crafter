package online.aruka.demo.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterPredicateImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeContainerImpl
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import online.aruka.demo.Demo
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType

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
        return CRecipeImpl.amorphous(
            name = "glow berry recipe",
            items = listOf(glowstone, berry),
            results = listOf(
                ResultSupplier.timesSingle(ItemStack.of(Material.GLOW_BERRIES))
            )
        )
    }

    fun infinityIronBlock(): CRecipe {
        val normalIronBlock: CMatter = CMatterImpl(
            name = "normal iron block",
            candidate = setOf(Material.IRON_BLOCK),
            predicates = setOf(CMatterPredicateImpl { ctx ->
                !ctx.input.itemMeta.persistentDataContainer.has(
                    NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                    PersistentDataType.INTEGER
                )
            })
        )
        val infinityIronBlock: CMatter = CMatterImpl(
            name = "infinity iron block",
            candidate = setOf(Material.IRON_BLOCK),
            predicates = setOf(
                CMatterPredicateImpl { ctx ->
                    // CMatterPredicate, container value check
                    ctx.input.itemMeta.persistentDataContainer.has(
                        NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                        PersistentDataType.INTEGER)
                },

                CMatterPredicateImpl { ctx ->
                    // CMatterPredicate, amount check
                    var count = 0
                    ctx.mapped.values.forEach { v ->
                        count += v.itemMeta.persistentDataContainer.getOrDefault(
                            NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                            PersistentDataType.INTEGER,
                            0
                        )
                    }

                    ctx.input.itemMeta.persistentDataContainer.getOrDefault(
                        NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                        PersistentDataType.INTEGER,
                        0
                    ) + count < Int.MAX_VALUE
                },
            ),
            mass = true
        )
        val supplier = ResultSupplier { ctx ->
            var count = 0
            ctx.mapped.values.forEach { v ->
                v.itemMeta.persistentDataContainer.get(
                    NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                    PersistentDataType.INTEGER
                )?.let { count += it }
                    ?: run { count += v.amount }
            }
            val block: ItemStack = ItemStack.of(Material.IRON_BLOCK)
            block.editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                    PersistentDataType.INTEGER,
                    count
                )
                meta.displayName("Infinity Iron Block".toComponent())
                meta.lore(listOf("<green>Contained Iron Block:</green> $count".toComponent()))
            }
            listOf(block)
        }

        return CRecipeImpl.amorphous(
            name = "infinity iron block recipe",
            items = List(35) { normalIronBlock } + infinityIronBlock,
            results = listOf(supplier)
        )
    }

    fun infinityIronBlockExtract(): CRecipe {
        val infinityIronBlock: CMatter = CMatterImpl(
            name = "infinity iron block",
            candidate = setOf(Material.IRON_BLOCK),
            predicates = setOf(CMatterPredicateImpl { ctx ->
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
        return CRecipeImpl.amorphous(
            name = "infinity iron block extract recipe",
            items = listOf(infinityIronBlock),
            results = listOf(supplier)
        )
    }

    fun extractPotion(): CRecipe {
        val potion: CMatter = CMatterImpl.of(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION)
        val container = CRecipeContainerImpl(
            predicate = { ctx -> ctx.mapped.values.first().itemMeta is PotionMeta },
            consumer = { ctx ->
                val potionMeta: PotionMeta = ctx.mapped.values.first().itemMeta as? PotionMeta ?: return@CRecipeContainerImpl
                Bukkit.getPlayer(ctx.userID)?.let { player ->
                    potionMeta.basePotionType?.let { basePotion ->
                        player.addPotionEffects(basePotion.potionEffects)
                    }
                    if (potionMeta.hasCustomEffects()) {
                        player.addPotionEffects(potionMeta.customEffects)
                    }
                }
            }
        )
        val supplier = ResultSupplier.timesSingle(ItemStack.of(Material.GLASS_BOTTLE))
        return CRecipeImpl.amorphous(
            name = "potion effect extractor recipe",
            items = listOf(potion),
            containers = listOf(container),
            results = listOf(supplier)
        )
    }
}
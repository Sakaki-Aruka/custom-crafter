package online.aruka.demo.register

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterPredicateImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.GroupRecipe
import io.github.sakaki_aruka.customcrafter.impl.result.ResultSupplierImpl
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import online.aruka.demo.Demo
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ShapedRecipeProvider {
    fun enchantedGoldenApple(): CRecipe {
        val goldBlock: CMatter = CMatterImpl.single(Material.GOLD_BLOCK)
        val apple: CMatter = CMatterImpl.single(Material.APPLE)
        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        /*
         * # -> gold block
         * + -> apple
         *
         * ###
         * #+#
         * ###
         *
         * returns 1x enchanted golden apple (Notch Apple)
         */
        CoordinateComponent.square(3).forEach { c ->
            items[c] = goldBlock
        }
        items[CoordinateComponent(1, 1)] = apple

        return CRecipeImpl(
            name = "enchanted golden apple recipe",
            items = items,
            results = listOf(
                ResultSupplierImpl.timesSingle(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE))
            ),
            type = CRecipeType.NORMAL
        )
    }

    fun wateredBottles(): CRecipe {
        val emptyBottle: CMatter = CMatterImpl.single(Material.GLASS_BOTTLE)
        val waterBucket: CMatter = CMatterImpl.single(Material.WATER_BUCKET)

        val supplier = ResultSupplierImpl { ctx ->
            val list: MutableList<ItemStack> = mutableListOf()
            list.add(ItemStack.of(Material.POTION, ctx.calledTimes * 4))
            list.add(ItemStack.of(Material.BUCKET, ctx.calledTimes))
            return@ResultSupplierImpl list
        }

        /*
         * # -> glass bottle
         * + -> water bucket
         *
         *  #  (x:1, y:0)
         * #+# (x:0, y:1), (x:1, y:1), (x:2, y:1)
         *
         * returns 4x water bottle, 1x empty bucket
         */
        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        items[CoordinateComponent(1, 0)] = emptyBottle
        items[CoordinateComponent(0, 1)] = emptyBottle
        items[CoordinateComponent(1, 1)] = waterBucket
        items[CoordinateComponent(2, 1)] = emptyBottle

        return CRecipeImpl(
            name = "bulk water bottles recipe",
            items = items,
            results = listOf(supplier),
            type = CRecipeType.NORMAL
        )
    }

    fun moreWateredBottles(): CRecipe {
        val emptyBottle: CMatter = CMatterImpl.single(Material.GLASS_BOTTLE)
        val waterBucket: CMatter = CMatterImpl(
            name = "water bucket (mass)",
            candidate = setOf(Material.WATER_BUCKET),
            mass = true
        )

        val supplier = ResultSupplierImpl { ctx ->
            listOf(
                ItemStack.of(Material.POTION, ctx.calledTimes * 4),
                ItemStack.of(Material.BUCKET)
            )
        }

        /*
         * # -> glass bottle
         * + -> water bucket
         *
         *  #  (x:1, y:0)
         * #+# (x:0, y:1), (x:1, y:1), (x:2, y:1)
         *  #  (x:1, y:2)
         *
         * returns (min amount * 4)x water bottle, 1x empty bucket
         */
        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        items[CoordinateComponent(1, 0)] = emptyBottle
        items[CoordinateComponent(0, 1)] = emptyBottle
        items[CoordinateComponent(1, 1)] = waterBucket
        items[CoordinateComponent(2, 1)] = emptyBottle
        items[CoordinateComponent(1, 2)] = emptyBottle

        return CRecipeImpl(
            name = "bulk water bottles (more) recipe",
            items = items,
            results = listOf(supplier),
            type = CRecipeType.NORMAL
        )
    }

    fun infinityIronBlockCore(): CRecipe {
        val ironBlock: CMatter = CMatterImpl(
            name = "iron block core",
            candidate = setOf(Material.IRON_BLOCK),
            predicates = setOf(CMatterPredicateImpl { ctx ->
                val key = NamespacedKey(Demo.plugin, "infinity_iron_block_count")
                !ctx.input.itemMeta.persistentDataContainer.has(key, PersistentDataType.LONG)
            })
        )
        val obsidian: CMatter = CMatterImpl(
            name = "obsidian",
            candidate = setOf(Material.OBSIDIAN),
            mass = true
        )

        val supplier = ResultSupplierImpl { ctx ->
            val totalIronBlockAmount: Int = ctx.mapped.values.filter { v -> v.type == Material.IRON_BLOCK }
                .sumOf { i -> i.amount }
            val core: ItemStack = ItemStack.of(Material.IRON_BLOCK)
            core.editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                    PersistentDataType.INTEGER,
                    totalIronBlockAmount
                )
                meta.displayName("Infinity Iron Block".toComponent())
                meta.lore(listOf("<green>Contained Iron Block:</green> $totalIronBlockAmount".toComponent()))
            }
            listOf(core)
        }

        /*
         * # -> iron block
         * + -> obsidian
         *
         * ###
         * #+#
         * ###
         *
         * returns (min amount)x iron block
         */
        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        CoordinateComponent.square(3).forEach { c ->
            items[c] = ironBlock
        }
        items[CoordinateComponent(1, 1)] = obsidian

        return CRecipeImpl(
            name = "infinity iron block core",
            items = items,
            results = listOf(supplier),
            type = CRecipeType.NORMAL
        )
    }

    fun infinityIronBlock(): CRecipe {
        val core: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl(
                name = "infinity iron block core",
                candidate = setOf(Material.IRON_BLOCK),
                predicates = setOf(CMatterPredicateImpl { ctx ->
                    ctx.input.itemMeta.persistentDataContainer.has(
                        NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                        PersistentDataType.INTEGER,
                    )
                }),
                mass = true
            ),
            includeAir = false
        )
        val coreContext = GroupRecipe.Context.default(CoordinateComponent(0, 0))

        val ironBlock: CMatter = GroupRecipe.Matter.of(
            matter = CMatterImpl.of(Material.IRON_BLOCK),
            includeAir = true
        )
        val ironBlockContext = GroupRecipe.Context.of(
            members = Converter.getAvailableCraftingSlotComponents().filter { it.toIndex() != 0 }.toSet(),
            min = 1
        )

        /*
         * # -> Infinity Iron Block (Core)
         * + -> Iron Block
         *
         * 1x #, 1~35x +
         */
        val items: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        items[CoordinateComponent(0, 0)] = core
        ironBlockContext.members.forEach { c -> items[c] = ironBlock }

        val supplier: ResultSupplier = ResultSupplierImpl { ctx ->

            val core: ItemStack = ctx.mapped.getValue(
                ctx.relation.components.first { (recipe, _) -> recipe.toIndex() == 0 }.input
            ).clone()
            val currentCount: Int = core.itemMeta.persistentDataContainer.getOrDefault(
                NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                PersistentDataType.INTEGER,
                0
            )
            val ironBlockAmount: Int = ctx.mapped.entries
                .filter { (c, _) -> c.toIndex() != 0 }
                .sumOf { (_, input) -> input.amount }

            val result: MutableList<ItemStack> = mutableListOf()
            if (core.amount > 1) {
                result.add(core.clone().asQuantity(core.amount - 1))
            }

            val canAddAmount: Int = Int.MAX_VALUE - currentCount
            if (canAddAmount < ironBlockAmount) {
                result.add(ItemStack.of(Material.IRON_BLOCK, ironBlockAmount - canAddAmount))
            }

            val newValue: Int = currentCount + if (canAddAmount < ironBlockAmount) canAddAmount else ironBlockAmount

            core.editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(Demo.plugin, "infinity_iron_block_count"),
                    PersistentDataType.INTEGER,
                    newValue
                )

                meta.lore(listOf("<green>Contained Iron Block: <white>$newValue".toComponent()))
            }

            result.add(core.asOne())
            return@ResultSupplierImpl result
        }

        return GroupRecipe(
            name = "Infinity Iron Block",
            items = items,
            groups = setOf(coreContext, ironBlockContext),
            results = listOf(supplier)
        )
    }
}
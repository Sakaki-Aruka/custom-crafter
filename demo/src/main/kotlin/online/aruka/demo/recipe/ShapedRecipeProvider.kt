package online.aruka.demo.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

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
                ResultSupplier.timesSingle(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE))
            ),
            type = CRecipeType.NORMAL
        )
    }

    fun wateredBottles(): CRecipe {
        val emptyBottle: CMatter = CMatterImpl.single(Material.GLASS_BOTTLE)
        val waterBucket: CMatter = CMatterImpl.single(Material.WATER_BUCKET)

        val supplier = ResultSupplier { ctx ->
            val list: MutableList<ItemStack> = mutableListOf()
            list.add(ItemStack.of(Material.POTION, ctx.calledTimes * 4))
            list.add(ItemStack.of(Material.BUCKET, ctx.calledTimes))
            return@ResultSupplier list
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

        val supplier = ResultSupplier { ctx ->
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
}
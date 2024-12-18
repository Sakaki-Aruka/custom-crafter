package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.CraftView
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.CMatterImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeType
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.search.Search
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * @suppress
 */
internal object SearchTest {
    fun run() {
        vanillaTest()
        customTest()
    }

    private fun customTest() {
        amorphousTest1()
        amorphousTest2()
        normalTest1()
    }

    private fun normalTest1() {
        /*
         * xxx
         * x_x
         * xxx
         * x = STONE
         * _ = AIR
         *
         */

        val matter: CMatter = CMatterImpl(
            "testMatter",
            setOf(Material.COBBLESTONE)
        )

        val mapped: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        listOf(0, 1, 2, 9, 11, 18, 19, 20).map { i ->
            CoordinateComponent(i % 9, i / 9)
        }.forEach { c ->
            mapped[c] = matter
        }

        val recipe: CRecipe = CRecipeImpl(
            "testRecipe",
            mapped,
            type = CRecipeType.NORMAL
        )

        val gui = CustomCrafterAPI.getCraftingGUI()
        setOf(0, 1, 2, 9, 11, 18, 19, 20).forEach { i ->
            gui.setItem(i, ItemStack(Material.COBBLESTONE))
        }
        CustomCrafterAPI.RECIPES.add(recipe)

        val resultOfNatural = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true
        )

        CAssert.assertTrue(resultOfNatural != null)
        CAssert.assertTrue(resultOfNatural!!.vanilla() == null)
        CAssert.assertTrue(resultOfNatural.customs().isNotEmpty())
        CAssert.assertTrue(resultOfNatural.customs().first().first == recipe)
        CAssert.assertTrue(resultOfNatural.customs().first().second.components.toSet().size == 8)

        val resultOfUnnatural = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = false
        )

        CAssert.assertTrue(resultOfUnnatural != null)
        CAssert.assertTrue(resultOfUnnatural!!.vanilla() != null)
        CAssert.assertTrue(resultOfUnnatural.vanilla()!!.result.type == Material.FURNACE)
        CAssert.assertTrue(resultOfUnnatural.customs().isNotEmpty())
        CAssert.assertTrue(resultOfUnnatural.customs().first().first == recipe)
        CAssert.assertTrue(resultOfUnnatural.customs().first().second.components.toSet().size == 8)

        val invalidInput = CustomCrafterAPI.getCraftingGUI()
        setOf(0, 1, 2, 9, 11, 18, 19).forEach { i ->
            invalidInput.setItem(i, ItemStack(Material.COBBLESTONE))
        }
        invalidInput.setItem(20, ItemStack(Material.STONE))
        val invalidResult = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(invalidInput)!!,
            natural = true
        )
        CAssert.assertTrue(invalidResult != null)
        CAssert.assertTrue(invalidResult!!.customs().isEmpty())
        CAssert.assertTrue(invalidResult.vanilla() == null)

        //cleanup
        CustomCrafterAPI.RECIPES.remove(recipe)
    }

    private fun amorphousTest1() {
        /*
         * x * 4
         * x = STONE
         */

        val matter: CMatter = CMatterImpl(
            "testMatter",
            setOf(Material.STONE),
            amount = 1,
            mass = false,
            predicates = null,
            persistentDataContainer = null
        )

        val recipe: CRecipe = CRecipeImpl(
            "testRecipe",
            mapOf(
                Pair(CoordinateComponent(0, 0), matter),
                Pair(CoordinateComponent(0, 1), matter),
                Pair(CoordinateComponent(0, 2), matter),
                Pair(CoordinateComponent(0, 3), matter)
            ),
            containers = null,
            results = null,
            type = CRecipeType.AMORPHOUS
        )

        CustomCrafterAPI.RECIPES.add(recipe)

        val gui = CustomCrafterAPI.getCraftingGUI()
        setOf(0, 1, 45, 46).forEach { i ->
            gui.setItem(i, ItemStack(Material.STONE))
        }
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true
        )
        CAssert.assertTrue(result != null)
        CAssert.assertTrue(result!!.vanilla() == null)
        CAssert.assertTrue(result.customs().isNotEmpty())
        CAssert.assertTrue(result.customs().size == 1)
        val (returnedRecipe, relation) = result.customs().first()
        CAssert.assertTrue(returnedRecipe == recipe)
        val returnedComponents = relation.components
        CAssert.assertTrue(returnedComponents.isNotEmpty())
        CAssert.assertTrue(returnedComponents.size == 4)
        CAssert.assertTrue(returnedComponents.map { it.recipe }.toSet().size == 4)
        CAssert.assertTrue(returnedComponents.map { it.input }.toSet().size == 4)

        // clean up
        CustomCrafterAPI.RECIPES.remove(recipe)
    }

    private fun amorphousTest2() {
        // batch test
        val slimeBall: CMatter = CMatterImpl(
            "slimeBall",
            setOf(Material.SLIME_BALL)
        )

        val lavaBucket: CMatter = CMatterImpl(
            "lavaBucket",
            setOf(Material.LAVA_BUCKET),
            amount = 1,
            mass = true,
            predicates = null,
            persistentDataContainer = null
        )

        val recipe: CRecipe = CRecipeImpl(
            "testRecipe",
            mapOf(
                Pair(CoordinateComponent(0, 0), slimeBall),
                Pair(CoordinateComponent(0, 1), lavaBucket)
            ),
            containers = null,
            results = null,
            CRecipeType.AMORPHOUS
        )

        val gui = CustomCrafterAPI.getCraftingGUI()
        gui.setItem(0, ItemStack(Material.SLIME_BALL, 10))
        gui.setItem(1, ItemStack(Material.LAVA_BUCKET))
        CustomCrafterAPI.RECIPES.add(recipe)
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true
        )

        CAssert.assertTrue(result != null)
        CAssert.assertTrue(result!!.vanilla() == null)
        CAssert.assertTrue(result.customs().size == 1)
        CAssert.assertTrue(result.customs().first().first == recipe)

        // cleanup
        CustomCrafterAPI.RECIPES.remove(recipe)
    }

    private fun vanillaTest() {
        furnaceTest()
        batchFurnaceTest()
        emptyTest()
    }

    private fun furnaceTest() {
        val gui = CustomCrafterAPI.getCraftingGUI()
        setOf(0, 1, 2, 9, 11, 18, 19, 20).forEach { i ->
            gui.setItem(i, ItemStack(Material.COBBLESTONE))
        }
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true
        )
        CAssert.assertTrue(result != null)
        CAssert.assertTrue(result!!.customs().isEmpty())
        CAssert.assertTrue(result.vanilla() != null)
        CAssert.assertTrue(result.vanilla()!!.result.isSimilar(ItemStack(Material.FURNACE)))
    }

    private fun batchFurnaceTest() {
        val gui = CustomCrafterAPI.getCraftingGUI()
        setOf(0, 1, 2, 9, 11, 18, 19, 20).forEach { i ->
            gui.setItem(i, ItemStack(Material.COBBLESTONE, 10))
        }
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true
        )
        CAssert.assertTrue(result != null)
        CAssert.assertTrue(result!!.customs().isEmpty())
        CAssert.assertTrue(result.vanilla() != null)
        CAssert.assertTrue(result.vanilla()!!.result.type == Material.FURNACE)
        CAssert.assertTrue(result.vanilla()!!.result.amount == 1)
    }

    private fun emptyTest() {
        val gui = CustomCrafterAPI.getCraftingGUI()
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true
        )
        CAssert.assertTrue(result == null)
    }
}
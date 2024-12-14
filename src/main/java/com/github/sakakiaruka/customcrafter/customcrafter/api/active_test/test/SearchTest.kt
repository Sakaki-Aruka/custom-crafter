package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
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
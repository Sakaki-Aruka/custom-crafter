package io.github.sakaki_aruka.customcrafter.api.active_test.test

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.active_test.CAssert
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
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

        val testRecipes: MutableList<CRecipe> = mutableListOf(recipe)

        val resultOfNatural = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true,
            sourceRecipes = testRecipes
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
            predicates = null
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

        val testRecipes: MutableList<CRecipe> = mutableListOf(recipe)

        val gui = CustomCrafterAPI.getCraftingGUI()
        setOf(0, 1, 45, 46).forEach { i ->
            gui.setItem(i, ItemStack(Material.STONE))
        }
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true,
            sourceRecipes = testRecipes
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
            predicates = null
        )

        val recipe: CRecipe = CRecipeImpl(
            "testRecipe",
            mapOf(
                Pair(CoordinateComponent(0, 0), slimeBall),
                Pair(CoordinateComponent(0, 1), lavaBucket)
            ),
            containers = null,
            results = null,
            type = CRecipeType.AMORPHOUS
        )

        val gui = CustomCrafterAPI.getCraftingGUI()
        gui.setItem(0, ItemStack(Material.SLIME_BALL, 10))
        gui.setItem(1, ItemStack(Material.LAVA_BUCKET))
        val testRecipes: MutableList<CRecipe> = mutableListOf(recipe)
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true,
            sourceRecipes = testRecipes
        )

        CAssert.assertTrue(result != null)
        CAssert.assertTrue(result!!.vanilla() == null)
        CAssert.assertTrue(result.customs().size == 1)
        CAssert.assertTrue(result.customs().first().first == recipe)
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
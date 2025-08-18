package io.github.sakaki_aruka.customcrafter.impl.test

import io.github.sakaki_aruka.customcrafter.api.active_test.CAssert
import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.EnchantStrict
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantmentStoreMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.potion.CPotionMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.EnchantFilter
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.EnchantStorageFilter
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.PotionFilter
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.ktorm.dsl.CASE
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
        enchantTest1()
        enchantTest2()
        enchantStoreTest1()
        potionTest1()
        potionTest2()
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

        val gui = CraftUI().inventory
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
            natural = false,
            sourceRecipes = testRecipes
        )

        CAssert.assertTrue(resultOfUnnatural != null)
        CAssert.assertTrue(resultOfUnnatural!!.vanilla() != null)
        CAssert.assertTrue(resultOfUnnatural.vanilla()!!.result.type == Material.FURNACE)
        CAssert.assertTrue(resultOfUnnatural.customs().isNotEmpty())
        CAssert.assertTrue(resultOfUnnatural.customs().first().first == recipe)
        CAssert.assertTrue(resultOfUnnatural.customs().first().second.components.toSet().size == 8)

        val invalidInput = CraftUI().inventory
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

        val gui = CraftUI().inventory
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

        val gui = CraftUI().inventory
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
        val gui = CraftUI().inventory
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
        val gui = CraftUI().inventory
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
        val gui = CraftUI().inventory
        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            natural = true
        )
        CAssert.assertTrue(result == null)
    }

    private fun enchantTest1() {
        val onlyEnchantment = CEnchantMatterImpl(
            "oe",
            setOf(Material.STONE),
            setOf(CEnchantComponent(
                level = 1, enchantment = Enchantment.EFFICIENCY, EnchantStrict.ONLY_ENCHANT
            ))
        )

        val strict = CEnchantMatterImpl(
            "s",
            setOf(Material.STONE),
            setOf(CEnchantComponent(
                level = 1, enchantment = Enchantment.EFFICIENCY, EnchantStrict.STRICT
            ))
        )

        val recipe = CRecipeImpl(
            "r",
            mapOf(
                CoordinateComponent(0, 2) to onlyEnchantment,
                CoordinateComponent(0, 3) to strict
            ),
            CRecipeType.AMORPHOUS
        )

        val input1 = ItemStack(Material.STONE)
        input1.editMeta { meta -> meta.addEnchant(Enchantment.EFFICIENCY, 5, false) }

        val input2 = ItemStack(Material.STONE)
        input2.editMeta { meta -> meta.addEnchant(Enchantment.EFFICIENCY, 1, false) }

        val gui = CraftUI().inventory
        gui.setItem(38, input1)
        gui.setItem(46, input2)

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = CraftView.fromInventory(gui)!!,
            sourceRecipes = listOf(recipe)
        )

        val stone = ItemStack(Material.STONE)
        CAssert.assertTrue(
            EnchantFilter.normal(stone, onlyEnchantment).first == CRecipeFilter.ResultType.FAILED)
        CAssert.assertTrue(
            EnchantFilter.normal(stone, strict).first == CRecipeFilter.ResultType.FAILED)

        CAssert.assertTrue(result != null)
        CAssert.assertTrue(result!!.vanilla() == null)
        CAssert.assertTrue(result.customs().size == 1)
        val (_, mapped) = result.customs().first()
        CAssert.assertTrue(mapped.components
            .contains(MappedRelationComponent(CoordinateComponent(0, 2), CoordinateComponent.fromIndex(38))))

        CAssert.assertTrue(mapped.components
            .contains(MappedRelationComponent(CoordinateComponent(0, 3), CoordinateComponent.fromIndex(46))))
    }

    private fun enchantTest2() {
        val emptyEnchant = CEnchantMatterImpl(
            name = "empty",
            candidate = setOf(Material.STONE),
            enchantComponents = emptySet()
        )

        val noEnchantInput1 = ItemStack.of(Material.STONE)
        //noEnchantInput1.editMeta { meta -> meta.addEnchant(Enchantment.EFFICIENCY, 5, false) }
        val (resultType1, _) = EnchantFilter.normal(noEnchantInput1, emptyEnchant)

        CAssert.assertTrue(resultType1 == CRecipeFilter.ResultType.NOT_REQUIRED)

        val hasEnchant1 = CEnchantMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf(CEnchantComponent(1, Enchantment.EFFICIENCY, EnchantStrict.ONLY_ENCHANT))
        )
        val (resultType2, _) = EnchantFilter.normal(noEnchantInput1, hasEnchant1)
        CAssert.assertTrue(resultType2 == CRecipeFilter.ResultType.FAILED)

        val hasEnchantInput1 = ItemStack.of(Material.STONE)
        hasEnchantInput1.editMeta { meta -> meta.addEnchant(Enchantment.EFFICIENCY, 1, true) }

        val (resultType3, _) = EnchantFilter.normal(hasEnchantInput1, hasEnchant1)
        CAssert.assertTrue(resultType3 == CRecipeFilter.ResultType.SUCCESS)
    }

    private fun enchantStoreTest1() {
        val noEnchantInput = ItemStack.of(Material.ENCHANTED_BOOK)
        val onlyEnchantInput = ItemStack.of(Material.ENCHANTED_BOOK).apply {
            itemMeta = itemMeta.apply {
                (this as EnchantmentStorageMeta).addStoredEnchant(Enchantment.EFFICIENCY, 100, true)
            }
        }
        val strictInput = ItemStack.of(Material.ENCHANTED_BOOK).apply {
            itemMeta = itemMeta.apply {
                (this as EnchantmentStorageMeta).addStoredEnchant(Enchantment.EFFICIENCY, 1, true)
            }
        }

        val noEnchantMatter = CEnchantmentStoreMatterImpl(
            name = "",
            candidate = setOf(Material.ENCHANTED_BOOK),
            storedEnchantComponents = setOf()
        )

        val onlyEnchantMatter = CEnchantmentStoreMatterImpl(
            name = "",
            candidate = setOf(Material.ENCHANTED_BOOK),
            setOf(CEnchantComponent(
                level = 1, enchantment = Enchantment.EFFICIENCY, strict = EnchantStrict.ONLY_ENCHANT))
        )

        val strictMatter = CEnchantmentStoreMatterImpl(
            name = "",
            candidate = setOf(Material.ENCHANTED_BOOK),
            storedEnchantComponents = setOf(CEnchantComponent(
                level = 1, enchantment = Enchantment.EFFICIENCY, strict = EnchantStrict.STRICT))
        )

        CAssert.assertTrue(EnchantStorageFilter.normal(noEnchantInput, noEnchantMatter).first == CRecipeFilter.ResultType.NOT_REQUIRED)
        CAssert.assertTrue(EnchantStorageFilter.normal(noEnchantInput, onlyEnchantMatter).first == CRecipeFilter.ResultType.FAILED)
        CAssert.assertTrue(EnchantStorageFilter.normal(noEnchantInput, strictMatter).first == CRecipeFilter.ResultType.FAILED)

        CAssert.assertTrue(EnchantStorageFilter.normal(onlyEnchantInput, noEnchantMatter).first == CRecipeFilter.ResultType.NOT_REQUIRED)
        val (strictType1, strictResult1) = EnchantStorageFilter.normal(onlyEnchantInput, onlyEnchantMatter)
        CAssert.assertTrue(strictType1 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(strictResult1)
        val (strictType2, strictResult2) = EnchantStorageFilter.normal(onlyEnchantInput, strictMatter)
        CAssert.assertTrue(strictType2 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(!strictResult2)

        CAssert.assertTrue(EnchantStorageFilter.normal(strictInput, noEnchantMatter).first == CRecipeFilter.ResultType.NOT_REQUIRED)
        val (strictType3, strictResult3) = EnchantStorageFilter.normal(strictInput, onlyEnchantMatter)
        CAssert.assertTrue(strictType3 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(strictResult3)
        val (strictType4, strictResult4) = EnchantStorageFilter.normal(strictInput, strictMatter)
        CAssert.assertTrue(strictType4 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(strictResult4)
    }

    private fun enchantStoreTest2() {
        //
    }

    private fun potionTest1() {
        val onlyEffect = CPotionMatterImpl(
            "oe",
            setOf(Material.POTION),
            setOf(CPotionComponent(
                PotionEffect(
                    PotionEffectType.POISON, 1, 3),
                CPotionComponent.PotionStrict.ONLY_EFFECT
            )))

        val strict = CPotionMatterImpl(
            "s",
            setOf(Material.POTION),
            setOf(CPotionComponent(
                PotionEffect(
                    PotionEffectType.POISON, 1, 1),
                CPotionComponent.PotionStrict.STRICT
            )))

        val recipe: CRecipe = CRecipeImpl(
            "potionTestRecipe",
            mapOf(
                CoordinateComponent(0, 0) to onlyEffect,
                CoordinateComponent(0, 1) to strict
            ),
            CRecipeType.AMORPHOUS
        )

        val input1 = ItemStack(Material.POTION)
        input1.editMeta { m ->
            (m as PotionMeta).addCustomEffect(PotionEffect(PotionEffectType.POISON, 1, 1), true)
        }

        val gui = CraftUI().inventory
        gui.setItem(0, input1)
        gui.setItem(30, input1)

        val result = Search.search(
            UUID.randomUUID(),
            CraftView.fromInventory(gui)!!,
            sourceRecipes = listOf(recipe)
        )

        CAssert.assertTrue(result != null)
        CAssert.assertTrue(result!!.vanilla() == null)
        CAssert.assertTrue(result.customs().size == 1)
        val (returnedRecipe, mapped) = result.customs().first()
        CAssert.assertTrue(returnedRecipe == recipe)
        CAssert.assertTrue(mapped.components.size == 2)
        CAssert.assertTrue(mapped.components.contains(
            MappedRelationComponent(CoordinateComponent(0, 0), CoordinateComponent(0, 0))))
        CAssert.assertTrue(mapped.components.contains(
            MappedRelationComponent(CoordinateComponent(0, 1), CoordinateComponent.fromIndex(30))))
    }

    private fun potionTest2() {
        val noEffectPotionInput = ItemStack.of(Material.POTION)
        val onlyEffectPotionInput = ItemStack.of(Material.POTION).apply {
            itemMeta = itemMeta.apply {
                (this as PotionMeta).addCustomEffect(
                    PotionEffect(
                        PotionEffectType.POISON, 100, 100,
                        true),
                    true
                )
            }
        }
        val strictPotionInput = ItemStack.of(Material.POTION).apply {
            itemMeta = itemMeta.apply {
                (this as PotionMeta).addCustomEffect(
                    PotionEffect(
                    PotionEffectType.POISON, 1, 1),
                    true)
            }
        }

        val noEffectMatter = CPotionMatterImpl(
            name = "",
            candidate = setOf(Material.POTION),
            potionComponents = emptySet()
        )
        val onlyEffectMatter = CPotionMatterImpl(
            name = "",
            candidate = setOf(Material.POTION),
            potionComponents = setOf(
                CPotionComponent(
                    effect = PotionEffect(
                        PotionEffectType.POISON, 5, 5),
                    strict = CPotionComponent.PotionStrict.ONLY_EFFECT
                )
            )
        )
        val strictEffectMatter = CPotionMatterImpl(
            name = "",
            candidate = setOf(Material.POTION),
            potionComponents = setOf(
                CPotionComponent(
                    effect = PotionEffect(
                        PotionEffectType.POISON, 1, 1),
                    strict = CPotionComponent.PotionStrict.STRICT
                )
            )
        )

        CAssert.assertTrue(PotionFilter.normal(noEffectPotionInput, noEffectMatter).first == CRecipeFilter.ResultType.NOT_REQUIRED)
        CAssert.assertTrue(PotionFilter.normal(noEffectPotionInput, onlyEffectMatter).first == CRecipeFilter.ResultType.FAILED)
        CAssert.assertTrue(PotionFilter.normal(noEffectPotionInput, strictEffectMatter).first == CRecipeFilter.ResultType.FAILED)

        CAssert.assertTrue(PotionFilter.normal(onlyEffectPotionInput, noEffectMatter).first == CRecipeFilter.ResultType.NOT_REQUIRED)
        val (type1, result1) = PotionFilter.normal(onlyEffectPotionInput, onlyEffectMatter)
        CAssert.assertTrue(type1 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(result1)
        val (type2, result2) = PotionFilter.normal(onlyEffectPotionInput, strictEffectMatter)
        CAssert.assertTrue(type2 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(!result2)

        CAssert.assertTrue(PotionFilter.normal(strictPotionInput, noEffectMatter).first == CRecipeFilter.ResultType.NOT_REQUIRED)
        val (type3, result3) = PotionFilter.normal(strictPotionInput, onlyEffectMatter)
        CAssert.assertTrue(type3 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(result3)
        val (type4, result4) = PotionFilter.normal(strictPotionInput, strictEffectMatter)
        CAssert.assertTrue(type4 == CRecipeFilter.ResultType.SUCCESS)
        CAssert.assertTrue(result4)
    }


}
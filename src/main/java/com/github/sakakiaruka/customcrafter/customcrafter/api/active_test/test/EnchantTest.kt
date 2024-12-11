package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CEnchantMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantMatterImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantmentStoreMatterImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.EnchantStrict
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeType
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Enchant
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

/**
 * @suppress
 */
internal object EnchantTest {
    fun run() {
        enchantEqualsTest()
        enchantStoredEqualsTest()
        enchantAmorphousTest()
    }

    private fun enchantEqualsTest() {
        val itemStack = ItemStack(Material.STONE)
        val meta = itemStack.itemMeta
        meta.addEnchant(Enchantment.FLAME, 1, true)
        itemStack.itemMeta = meta

        fun base(set: Set<CEnchantComponent>): CEnchantMatterImpl {
            return CEnchantMatterImpl(
                name = "test",
                setOf(Material.STONE),
                amount = 1,
                mass = false,
                predicates = null,
                persistentDataContainer = null,
                set
            )
        }

        fun getEnchantedMatter(strict: EnchantStrict): CEnchantMatterImpl {
            return base(setOf(CEnchantComponent(1, Enchantment.FLAME, strict)))
        }

        val notStrict = getEnchantedMatter(EnchantStrict.NOT_STRICT)
        val onlyEnchant = getEnchantedMatter(EnchantStrict.ONLY_ENCHANT)
        val strict = getEnchantedMatter(EnchantStrict.STRICT)

        CAssert.assertTrue(Enchant.enchant(itemStack, notStrict))
        CAssert.assertTrue(Enchant.enchant(itemStack, onlyEnchant))
        CAssert.assertTrue(Enchant.enchant(itemStack, strict))

        val emptyEnchantMatter = base(emptySet())
        CAssert.assertTrue(Enchant.enchant(itemStack, emptyEnchantMatter))

        val levelEnchantMatter = base(setOf(CEnchantComponent(100, Enchantment.FLAME, EnchantStrict.ONLY_ENCHANT)))
        CAssert.assertTrue(Enchant.enchant(itemStack, levelEnchantMatter))
    }

    private fun enchantStoredEqualsTest() {
        val book = ItemStack(Material.ENCHANTED_BOOK)
        val meta = (book.itemMeta as EnchantmentStorageMeta)
        meta.addStoredEnchant(Enchantment.FLAME, 1, true)
        book.itemMeta = meta

        fun base(set: Set<CEnchantComponent>): CEnchantmentStoreMatterImpl {
            return CEnchantmentStoreMatterImpl(
                name = "test",
                setOf(Material.ENCHANTED_BOOK),
                amount = 1,
                mass = false,
                predicates = null,
                persistentDataContainer = null,
                set
            )
        }

        val emptyMatter: CEnchantmentStoreMatter = base(emptySet())

        CAssert.assertTrue(Enchant.enchantStored(book, emptyMatter))

        val lv1LureNotStrict = base(setOf(CEnchantComponent(1, Enchantment.LURE, EnchantStrict.NOT_STRICT)))
        val lv1LureOnlyEnchant = base(setOf(CEnchantComponent(1, Enchantment.LURE, EnchantStrict.ONLY_ENCHANT)))
        val lv1LureStrict = base(setOf(CEnchantComponent(1, Enchantment.LURE, EnchantStrict.STRICT)))

        CAssert.assertTrue(Enchant.enchantStored(book, lv1LureNotStrict))
        CAssert.assertTrue(!Enchant.enchantStored(book, lv1LureOnlyEnchant))
        CAssert.assertTrue(!Enchant.enchantStored(book, lv1LureStrict))

        val lv1FlameNotStrict = base(setOf(CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.NOT_STRICT)))
        val lv1FlameOnlyEnchant = base(setOf(CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.ONLY_ENCHANT)))
        val lv1FlameStrict = base(setOf(CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.STRICT)))

        CAssert.assertTrue(Enchant.enchantStored(book, lv1FlameNotStrict))
        CAssert.assertTrue(Enchant.enchantStored(book, lv1FlameOnlyEnchant))
        CAssert.assertTrue(Enchant.enchantStored(book, lv1FlameStrict))

        val lv100FlameNotStrict = base(setOf(CEnchantComponent(100, Enchantment.FLAME, EnchantStrict.NOT_STRICT)))
        val lv100FlameOnlyEnchant = base(setOf(CEnchantComponent(100, Enchantment.FLAME, EnchantStrict.ONLY_ENCHANT)))
        val lv100FlameStrict = base(setOf(CEnchantComponent(100, Enchantment.FLAME, EnchantStrict.STRICT)))

        CAssert.assertTrue(Enchant.enchantStored(book, lv100FlameNotStrict))
        CAssert.assertTrue(Enchant.enchantStored(book, lv100FlameOnlyEnchant))
        CAssert.assertTrue(!Enchant.enchantStored(book, lv100FlameStrict))

        fun multi(strict: EnchantStrict): CEnchantmentStoreMatterImpl {
            return base(setOf(
                CEnchantComponent(1, Enchantment.FLAME, strict),
                CEnchantComponent(1, Enchantment.LURE, strict)
            ))
        }

        val multiNotStrict = multi(EnchantStrict.NOT_STRICT)
        val multiOnlyEnchant = multi(EnchantStrict.ONLY_ENCHANT)
        val multiStrict = multi(EnchantStrict.STRICT)

        CAssert.assertTrue(Enchant.enchantStored(book, multiNotStrict))
        CAssert.assertTrue(!Enchant.enchantStored(book, multiOnlyEnchant))
        CAssert.assertTrue(!Enchant.enchantStored(book, multiStrict))
    }

    private fun enchantAmorphousTest() {
        fun enchantedStone(components: Set<CEnchantComponent>): CEnchantMatter {
            return CEnchantMatterImpl(
                "oneEnchantRecipeMatter",
                setOf(Material.STONE),
                amount = 1,
                mass = false,
                null, null,
                components
            )
        }

        fun enchantedStoneItemStack(components: Set<CEnchantComponent>): ItemStack {
            val item = ItemStack(Material.STONE)
            val meta = item.itemMeta
            components.forEach { c ->
                meta.addEnchant(c.enchantment, c.level, true)
            }
            item.setItemMeta(meta)
            return item
        }

        fun enchantedRecipe(items: Map<CoordinateComponent, CMatter>): CRecipe {
            return CRecipeImpl(
                "testRecipe",
                items,
                containers = null, results = null,
                type = CRecipeType.AMORPHOUS
            )
        }

        /*
         * 1. matterX: CEnchantMatter
         * 2. recipeX: CRecipe
         * 3. itemStackX: ItemStack
         * 4. mappedInputX: Map<CoordinateComponent, ItemStack>
         */

        // CEnchantMatter
        val matter1: CEnchantMatter = enchantedStone(setOf(
            CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.ONLY_ENCHANT)
        ))

        // CRecipe
        val recipe1: CRecipe = enchantedRecipe(mapOf(
            Pair(CoordinateComponent(0, 0), matter1)
        ))

        // ItemStack
        val itemStack1: ItemStack = enchantedStoneItemStack(setOf(
            CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.INPUT)
        ))
        // Map<CoordinateComponent, ItemStack>
        val mappedInput1 = mapOf(
            Pair(CoordinateComponent(0, 0), itemStack1)
        )

        /*
         * 1. type
         * 2. list size
         * 3. list.first.coordinate
         * 4. list.coordinate.coordinate
         */

        val (type, candidateList) = Enchant.amorphous(mappedInput1, recipe1)
        CAssert.assertTrue(type == AmorphousFilterCandidate.Type.SUCCESSFUL)
        CAssert.assertTrue(candidateList.size == 1)
        val first = candidateList.first()
        CAssert.assertTrue(first.coordinate == CoordinateComponent(0, 0))
        CAssert.assertTrue(first.list == listOf(CoordinateComponent(0, 0)))


        /*
         * 1. matterX: CEnchantMatter
         * 2. recipeX: CRecipe
         * 3. itemStackX: ItemStack
         * 4. mappedInputX: Map<CoordinateComponent, ItemStack>
         */
        val matter2: CEnchantMatter = enchantedStone(setOf(
            CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.STRICT),
            CEnchantComponent(1, Enchantment.LURE, EnchantStrict.ONLY_ENCHANT)
        ))
        val recipe2: CRecipe = enchantedRecipe(mapOf(
            Pair(CoordinateComponent(0, 0), matter2)
        ))
        val mappedInput2 = mappedInput1

        /*
         * 1. type
         * 2. list size
         * 3. list.first.coordinate
         * 4. list.coordinate.coordinate
         */

        val (type2, candidateList2) = Enchant.amorphous(mappedInput2, recipe2)
        CAssert.assertTrue(type2 == AmorphousFilterCandidate.Type.NOT_ENOUGH)
        CAssert.assertTrue(candidateList2.isEmpty())

        /*
         * 1. matterX: CEnchantMatter
         * 2. recipeX: CRecipe
         * 3. itemStackX: ItemStack
         * 4. mappedInputX: Map<CoordinateComponent, ItemStack>
         */
        val matter3: CEnchantMatter = enchantedStone(emptySet())
        val recipe3: CRecipe = enchantedRecipe(mapOf(
            Pair(CoordinateComponent(0, 0), matter3)
        ))
        val mappedInput3 = mappedInput1
        val (type3, candidateList3) = Enchant.amorphous(mappedInput3, recipe3)
        CAssert.assertTrue(type3 == AmorphousFilterCandidate.Type.NOT_REQUIRED)
        CAssert.assertTrue(candidateList3.isEmpty())

        // TODO amorphous test (multi CMatter recipes and multi inputs)
    }
}
package io.github.sakaki_aruka.customcrafter.api.active_test.test

import io.github.sakaki_aruka.customcrafter.api.active_test.CAssert
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantmentStoreMatterImpl
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.EnchantStrict
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.internal.processor.Enchant
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
                candidate = setOf(Material.STONE),
                amount = 1,
                mass = false,
                predicates = null,
                enchantComponents = set
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
                storedEnchantComponents =  set
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
                name = "oneEnchantRecipeMatter",
                candidate = setOf(Material.STONE),
                amount = 1,
                mass = false,
                predicates = null,
                enchantComponents =  components
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

        /*
         * 1. matterX: CEnchantMatter
         * 2. recipeX: CRecipe
         * 3. itemStackX: ItemStack
         * 4. mappedInputX: Map<CoordinateComponent, ItemStack>
         */
        val for4Components: Set<CEnchantComponent> = setOf(
            CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.STRICT)
        )
        val matter4: CEnchantMatter = enchantedStone(for4Components)

        val for4Components1: Set<CEnchantComponent> = setOf(
            CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.STRICT),
            CEnchantComponent(1, Enchantment.LURE, EnchantStrict.STRICT)
        )
        val matter4_1: CEnchantMatter = enchantedStone(for4Components1)

        val recipe4: CRecipe = enchantedRecipe(mapOf(
            Pair(CoordinateComponent(0, 0), matter4),
            Pair(CoordinateComponent(0, 1), matter4),
            Pair(CoordinateComponent(0, 2), matter4_1),
            Pair(CoordinateComponent(0, 3), matter4)
        ))

        val for4ComponentsItem: Set<CEnchantComponent> = setOf(
            CEnchantComponent(1, Enchantment.AQUA_AFFINITY, EnchantStrict.INPUT),
            CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.INPUT)
        )
        val for4ComponentsItem1: Set<CEnchantComponent> = setOf(
            CEnchantComponent(1, Enchantment.LURE, EnchantStrict.INPUT),
            CEnchantComponent(1, Enchantment.FLAME, EnchantStrict.INPUT)
        )
        val itemStack4 = enchantedStoneItemStack(for4ComponentsItem)
        val itemStack4_1 = enchantedStoneItemStack(for4ComponentsItem1)
        val mappedInput4: Map<CoordinateComponent, ItemStack> = mapOf(
            Pair(CoordinateComponent(0, 0), itemStack4_1),
            Pair(CoordinateComponent(0, 1), itemStack4_1),
            Pair(CoordinateComponent(0, 2), itemStack4),
            Pair(CoordinateComponent(0, 3), itemStack4_1)
        )

        val (type4, list4) = Enchant.amorphous(mappedInput4, recipe4)
        CAssert.assertTrue(type4 == AmorphousFilterCandidate.Type.SUCCESSFUL)
        CAssert.assertTrue(list4.size == 4)
        val c0Matched = (0..<4).map { i -> CoordinateComponent(0, i) }.toSet()
        val c1Matched = (0..<4).filter { it != 2 }.map { i -> CoordinateComponent(0, i) }.toSet()
        CAssert.assertTrue(list4[0].list.toSet() == c0Matched)
        CAssert.assertTrue(list4[1].list.toSet() == c0Matched)
        CAssert.assertTrue(list4[2].list.toSet() == c1Matched)
        CAssert.assertTrue(list4[3].list.toSet() == c0Matched)
    }
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantMatterImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantmentStoreMatterImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.EnchantStrict
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
//            .apply {
//            itemMeta = (itemMeta as EnchantmentStorageMeta).apply {
//                addStoredEnchant(Enchantment.FLAME, 1, true)
//            }
//        }
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

        val emptyMatter: CEnchantmentStoreMatterImpl = base(emptySet())

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
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionMatterImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Potion
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @suppress
 */
internal object PotionTest {
    fun run() {
        potionTest()
        amorphousTest()
    }

    private fun potionTest() {
        val nonPotionItem = ItemStack(Material.STONE)
        val basicPotionMatter: CPotionMatter = CPotionMatterImpl(
            "potionTestBasic",
            setOf(Material.POTION),
            amount = 1,
            mass = false,
            null, null,
            setOf(
                CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT)
            )
        )

        CAssert.assertTrue(!Potion.potion(nonPotionItem, basicPotionMatter))

        val splashPotionItem = ItemStack(Material.SPLASH_POTION).apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                addCustomEffect(PotionEffect(PotionEffectType.LUCK, 100, 1), true)
            }
        }
        // if skip to check candidates, this check returns true. it is expected.
        CAssert.assertTrue(Potion.potion(splashPotionItem, basicPotionMatter))

        val basicEnableSplash: CPotionMatter = CPotionMatterImpl(
            basicPotionMatter.name,
            setOf(Material.SPLASH_POTION),
            amount = 1, mass = false, null, null,
            setOf(
                CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT)
            )
        )
        CAssert.assertTrue(Potion.potion(splashPotionItem, basicEnableSplash))

        val containedNotStrict: CPotionMatter = CPotionMatterImpl(
            basicPotionMatter.name,
            setOf(Material.SPLASH_POTION),
            amount = 1, mass = false, null, null,
            setOf(
                CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT),
                CPotionComponent(PotionEffect(PotionEffectType.POISON, 100, 1), CPotionComponent.PotionStrict.NOT_STRICT)
            )
        )
        CAssert.assertTrue(Potion.potion(splashPotionItem, containedNotStrict))

        val lv100LuckMatter: CPotionMatter = CPotionMatterImpl(
            basicPotionMatter.name,
            setOf(Material.SPLASH_POTION),
            amount = 1, mass = false, null, null,
            setOf(
                CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 100), CPotionComponent.PotionStrict.STRICT)
            )
        )
        CAssert.assertTrue(!Potion.potion(splashPotionItem, lv100LuckMatter))

    }

    private fun amorphousTest() {
        //
    }
}
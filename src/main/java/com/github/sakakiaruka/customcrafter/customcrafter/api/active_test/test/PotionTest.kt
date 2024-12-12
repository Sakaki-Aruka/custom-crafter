package com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test

import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.CAssert
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter.CPotionMatter
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.internal.AmorphousFilterCandidate
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionMatterImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeImpl
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CRecipeType
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
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
        fun potionItem(components: Set<CPotionComponent>): ItemStack {
            return ItemStack(Material.POTION).apply {
                itemMeta = (itemMeta as PotionMeta).apply {
                    components.forEach { c ->
                        addCustomEffect(c.effect, true)
                    }
                }
            }
        }

        fun potionMatter(components: Set<CPotionComponent>): CPotionMatter {
            return CPotionMatterImpl(
                "potionTestMatter",
                setOf(Material.POTION),
                amount = 1, mass = false, null, null,
                components
            )
        }

        fun potionContainedRecipe(items: Map<CoordinateComponent, CMatter>): CRecipe {
            return CRecipeImpl(
                "potionTestRecipe",
                items,
                null, null, CRecipeType.AMORPHOUS
            )
        }

        fun basicPE(effectType: PotionEffectType, level: Int = 1): PotionEffect {
            return PotionEffect(effectType, 100, level)
        }


        /*
         * 1. matterX: CEnchantMatter
         * 2. recipeX: CRecipe
         * 3. itemStackX: ItemStack
         * 4. mappedInputX: Map<CoordinateComponent, ItemStack>
         */

        val matter1: CPotionMatter = potionMatter(setOf(
            CPotionComponent(basicPE(PotionEffectType.LUCK), CPotionComponent.PotionStrict.STRICT)
        ))

        val recipe1: CRecipe = potionContainedRecipe(mapOf(
            Pair(CoordinateComponent(0, 0), matter1)
        ))

        val itemStack1: ItemStack = potionItem(setOf(
            CPotionComponent(basicPE(PotionEffectType.LUCK), CPotionComponent.PotionStrict.INPUT)
        ))
        val mappedInput1 = mapOf(
            Pair(CoordinateComponent(0, 0), itemStack1)
        )

        /*
         * 1. type
         * 2. list size
         * 3. list.first.coordinate
         * 4. list.coordinate.coordinate
         */

        val (type1, list1) = Potion.amorphous(mappedInput1, recipe1)
        CAssert.assertTrue(type1 == AmorphousFilterCandidate.Type.SUCCESSFUL)
        CAssert.assertTrue(list1.size == 1)
        val first1 = list1.first()
        CAssert.assertTrue(first1.coordinate == CoordinateComponent(0, 0))
        CAssert.assertTrue(first1.list == listOf(CoordinateComponent(0, 0)))
    }
}
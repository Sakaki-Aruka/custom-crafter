package io.github.sakaki_aruka.customcrafter.api.active_test.test

import io.github.sakaki_aruka.customcrafter.api.active_test.CAssert
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CPotionMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.potion.CPotionMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.internal.processor.Potion
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
            name = "potionTestBasic",
            candidate = setOf(Material.POTION),
            amount = 1,
            mass = false,
            predicates =  null,
            potionComponents =  setOf(
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
            name = basicPotionMatter.name,
            candidate = setOf(Material.SPLASH_POTION),
            amount = 1, mass = false,
            predicates = null,
            potionComponents = setOf(
                CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT)
            )
        )
        CAssert.assertTrue(Potion.potion(splashPotionItem, basicEnableSplash))

        val containedNotStrict: CPotionMatter = CPotionMatterImpl(
            basicPotionMatter.name,
            setOf(Material.SPLASH_POTION),
            amount = 1, mass = false,
            predicates = null,
            potionComponents = setOf(
                CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT),
                CPotionComponent(PotionEffect(PotionEffectType.POISON, 100, 1), CPotionComponent.PotionStrict.NOT_STRICT)
            )
        )
        CAssert.assertTrue(Potion.potion(splashPotionItem, containedNotStrict))

        val lv100LuckMatter: CPotionMatter = CPotionMatterImpl(
            basicPotionMatter.name,
            setOf(Material.SPLASH_POTION),
            amount = 1, mass = false,
            predicates = null,
            potionComponents = setOf(
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
                amount = 1, mass = false,
                predicates = null,
                potionComponents = components
            )
        }

        fun potionContainedRecipe(items: Map<CoordinateComponent, CMatter>): CRecipe {
            return CRecipeImpl(
                name = "potionTestRecipe",
                items = items,
                type = CRecipeType.AMORPHOUS
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

        // 2. not strict test
        /*
         * 1. matterX: CEnchantMatter
         * 2. recipeX: CRecipe
         * 3. itemStackX: ItemStack
         * 4. mappedInputX: Map<CoordinateComponent, ItemStack>
         */
        val matter2 = potionMatter(setOf())

        val recipe2 = potionContainedRecipe(mapOf(
            Pair(CoordinateComponent(0, 0), matter2)
        ))

        val item2 = potionItem(setOf(CPotionComponent(
            PotionEffect(PotionEffectType.POISON, 100, 1),
            CPotionComponent.PotionStrict.NOT_STRICT
        )))
        val mappedInput2 = mapOf(
            Pair(CoordinateComponent(1, 1), item2)
        )

        val (type2, list2) = Potion.amorphous(mappedInput2, recipe2)
        CAssert.assertTrue(type2 == AmorphousFilterCandidate.Type.NOT_REQUIRED)
        CAssert.assertTrue(list2.isEmpty())

        /*
         * 1. type
         * 2. list size
         * 3. list.first.coordinate
         * 4. list.coordinate.coordinate
         */

        val for4Components: Set<CPotionComponent> = setOf(
            CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT)
        )
        val matter4: CPotionMatter = potionMatter(for4Components)

        val for4Components1: Set<CPotionComponent> = setOf(
            CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT),
            CPotionComponent(PotionEffect(PotionEffectType.POISON, 100,1), CPotionComponent.PotionStrict.STRICT)
        )
        val matter4_1: CPotionMatter = potionMatter(for4Components1)

        val recipe4: CRecipe = potionContainedRecipe(mapOf(
            Pair(CoordinateComponent(0, 0), matter4),
            Pair(CoordinateComponent(0, 1), matter4),
            Pair(CoordinateComponent(0, 2), matter4_1),
            Pair(CoordinateComponent(0, 3), matter4)
        ))

        val for4ComponentsItem: Set<CPotionComponent> = setOf(
            CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT),
            CPotionComponent(PotionEffect(PotionEffectType.HUNGER, 100, 1), CPotionComponent.PotionStrict.STRICT)
        )

        val for4ComponentsItem1: Set<CPotionComponent> = setOf(
            CPotionComponent(PotionEffect(PotionEffectType.LUCK, 100, 1), CPotionComponent.PotionStrict.STRICT),
            CPotionComponent(PotionEffect(PotionEffectType.POISON, 100, 1), CPotionComponent.PotionStrict.STRICT)
        )
        val itemStack4 = potionItem(for4ComponentsItem)
        val itemStack4_1 = potionItem(for4ComponentsItem1)
        val mappedInput4: Map<CoordinateComponent, ItemStack> = mapOf(
            Pair(CoordinateComponent(1, 1), itemStack4_1),
            Pair(CoordinateComponent(1, 2), itemStack4_1),
            Pair(CoordinateComponent(1, 3), itemStack4),
            Pair(CoordinateComponent(1, 4), itemStack4_1)
        )

        val (type4, list4) = Potion.amorphous(mappedInput4, recipe4)
        CAssert.assertTrue(type4 == AmorphousFilterCandidate.Type.SUCCESSFUL)
        CAssert.assertTrue(list4.size == 4)
        val c0Matched = (1..4).map { i -> CoordinateComponent(1, i) }.toSet()
        val c1Matched = (1..4).filter { it != 3 }.map { i -> CoordinateComponent(1, i) }.toSet()
        CAssert.assertTrue(list4[0].list.toSet() == c0Matched)
        CAssert.assertTrue(list4[1].list.toSet() == c0Matched)
        CAssert.assertTrue(list4[2].list.toSet() == c1Matched)
        CAssert.assertTrue(list4[3].list.toSet() == c0Matched)
    }
}
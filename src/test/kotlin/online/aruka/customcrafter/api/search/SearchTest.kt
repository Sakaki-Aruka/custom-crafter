package online.aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.EnchantStrict
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantmentStoreMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.potion.CPotionMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.inventory.ItemStackMock
import org.mockbukkit.mockbukkit.world.WorldMock
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal object SearchTest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun shapedTest1() {
        /*
         * xxx
         * x_x
         * xxx
         *
         * x = stone
         * _ = air
         */

        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { _ -> matter }
        val recipe = CRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL
        )

        val ui = CraftUI()
        CoordinateComponent.square(3).forEach { c ->
            ui.inventory.setItem(c.toIndex(), ItemStackMock(Material.STONE))
        }

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
        assertEquals(null, result.vanilla())

        ui.inventory.clear()
        CoordinateComponent.square(3).forEach { c ->
            ui.inventory.setItem(c.toIndex(), ItemStackMock(Material.DIRT))
        }

        val result2 = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(0, result2.size())
    }

    @Test
    fun vanillaTest1() {
        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack(Material.STONE))

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView()
        )

        assertEquals(1, result.size())
        assertTrue(result.customs().isEmpty())
        assertTrue(result.vanilla() != null)
        assertEquals(Material.STONE_BUTTON, result.vanilla()!!.result.type)
    }

    @Test
    fun amorphousTest1() {
        val matter = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl(
            name = "",
            items = CustomCrafterAPI.getRandomNCoordinates(4).associateWith { _ -> matter },
            type = CRecipeType.AMORPHOUS
        )
        val ui = CraftUI()
        setOf(0, 1, 45, 46).forEach { i ->
            ui.inventory.setItem(i, ItemStack(Material.STONE))
        }

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
        assertEquals(null, result.vanilla())
        assertEquals(4, result.customs().first().second.components.size)
    }


    @Test
    fun amorphousTest2() {
        // mass test
        val slimeBall = CMatterImpl.single(Material.SLIME_BALL)
        val lavaBucket = CMatterImpl("", setOf(Material.LAVA_BUCKET), mass = true)
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to slimeBall,
                CoordinateComponent(0, 1) to lavaBucket
            ),
            type = CRecipeType.AMORPHOUS
        )

        val ui = CraftUI()
        ui.inventory.setItem(0, ItemStack(Material.SLIME_BALL, 10))
        ui.inventory.setItem(1, ItemStack(Material.LAVA_BUCKET))

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
    }

    @Test
    fun detectUnmatchMaterialCandidateInAmorphousRecipe() {
        // Recipe: requires 9 stones
        // Input : provides 8 stones and 1 cobblestone
        // -> Fail to mapping

        val stone = CMatterImpl.single(Material.STONE)
        // ###
        // # #
        // ###
        // # = STONE
        val recipe = CRecipeImpl.amorphous("", List(9) {stone})

        val ui = CraftUI()
        for (c in CoordinateComponent.square(3)) {
            ui.inventory.setItem(c.toIndex(), ItemStack.of(Material.STONE))
        }
        // *##
        // # #
        // ###
        // * = COBBLESTONE, # = STONE
        ui.inventory.setItem(0, ItemStack.of(Material.COBBLESTONE))

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(0, result.size())
    }

    @Test
    fun detectUnmatchEnchantCandidateInAmorphousRecipe() {
        // Recipe: requires 3 enchanted (efficiency, lv 5, strict) stones
        // Input : provides 3 enchanted (efficiency, lv 5 x2, lv4 x1) stones
        // -> Fail to mapping
        val matter = CEnchantMatterImpl(
            name = "lv5EfficiencyStone",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf(
                CEnchantComponent(5, Enchantment.EFFICIENCY, EnchantStrict.STRICT)
            )
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(0, 2) to matter
            ),
            type = CRecipeType.AMORPHOUS
        )

        val lv5Stone = ItemStack.of(Material.STONE)
        lv5Stone.editMeta { meta ->
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true)
        }
        val lv4Stone = ItemStack.of(Material.STONE)
        lv4Stone.editMeta { meta ->
            meta.addEnchant(Enchantment.EFFICIENCY, 4, true)
        }

        val ui = CraftUI()
        ui.inventory.setItem(0, lv5Stone)
        ui.inventory.setItem(1, lv5Stone)
        ui.inventory.setItem(2, lv4Stone)

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(0, result.size())
    }

    @Test
    fun detectUnmatchEnchantStorageCandidateInAmorphousRecipe() {
        // Recipe: requires 3 enchanted books (efficiency, lv5, strict)
        // Input : provides 3 enchanted books (efficiency, lv5x2, lv4x1)
        // -> Fail to mapping
        val matter = CEnchantmentStoreMatterImpl(
            name = "book",
            candidate = setOf(Material.ENCHANTED_BOOK),
            storedEnchantComponents = setOf(
                CEnchantComponent(5, Enchantment.EFFICIENCY, EnchantStrict.STRICT)
            )
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(0, 2) to matter
            ),
            type = CRecipeType.AMORPHOUS
        )

        val lv5 = ItemStack.of(Material.ENCHANTED_BOOK)
        lv5.editMeta { meta ->
            (meta as EnchantmentStorageMeta).addStoredEnchant(Enchantment.EFFICIENCY, 5, true)
        }
        val lv4 = ItemStack.of(Material.ENCHANTED_BOOK)
        lv4.editMeta { meta ->
            (meta as EnchantmentStorageMeta).addStoredEnchant(Enchantment.EFFICIENCY, 4, true)
        }

        val ui = CraftUI()
        ui.inventory.setItem(0, lv5)
        ui.inventory.setItem(1, lv5)
        ui.inventory.setItem(2, lv4)

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(0, result.size())
    }

    @Test
    fun detectUnmatchPotionCandidateInAmorphousRecipe() {
        // Recipe: requires 3 potions (luck, lv5, strict)
        // Input : provides 3 potions (luck, lv5x2, lv4x1)
        // -> Fail to mapping
        val matter = CPotionMatterImpl(
            name = "potion",
            candidate = setOf(Material.POTION),
            potionComponents = setOf(
                CPotionComponent(
                    PotionEffect(PotionEffectType.LUCK, 1, 5),
                    CPotionComponent.PotionStrict.STRICT
                )
            )
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(0, 2) to matter
            ),
            type = CRecipeType.AMORPHOUS
        )

        val lv5 = ItemStack.of(Material.POTION)
        lv5.editMeta { meta ->
            (meta as PotionMeta).addCustomEffect(PotionEffect(PotionEffectType.LUCK, 1, 5), true)
        }
        val lv4 = ItemStack.of(Material.POTION)
        lv4.editMeta { meta ->
            (meta as PotionMeta).addCustomEffect(PotionEffect(PotionEffectType.LUCK, 1, 4), true)
        }

        val ui = CraftUI()
        ui.inventory.setItem(0, lv5)
        ui.inventory.setItem(1, lv5)
        ui.inventory.setItem(2, lv4)

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(0, result.size())
    }

    @Test
    fun detectUnmatchMatterPredicateInAmorphousRecipe() {
        // Recipe: requires to craft by Notch
        // Action: craft by empty UUID user
        // -> Fail to mapping
        val NotchID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
        val emptyID = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val matter = CMatterImpl(
            name = "",
            candidate = setOf(Material.GRAVEL),
            predicates = setOf(CMatterPredicate {_, _, _, id -> id == NotchID})
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(1, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(1, 1) to matter
            ),
            type = CRecipeType.AMORPHOUS
        )

        val gravel = ItemStack.of(Material.GRAVEL)
        val ui = CraftUI()
        ui.inventory.setItem(0, gravel)
        ui.inventory.setItem(1, gravel)
        ui.inventory.setItem(9, gravel)
        ui.inventory.setItem(10, gravel)

        val result = Search.search(
            crafterID = emptyID,
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(0, result.size())
    }

    @Test
    fun detectUnmatchMatterAmountInAmorphousRecipe() {
        // Recipe: requires 3 stones (amount = 2)
        // Input : provides 3 stones (amount = 1)
        // -> Fail to mapping
        val matter = CMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            amount = 2
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(0, 2) to matter
            ),
            type = CRecipeType.AMORPHOUS
        )

        val stone = ItemStack.of(Material.STONE, 1)
        val ui = CraftUI()
        ui.inventory.setItem(0, stone)
        ui.inventory.setItem(1, stone)
        ui.inventory.setItem(2, stone)

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(0, result.size())
    }

    @Test
    fun enchantTest1() {
        val onlyEnchant = CEnchantMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf(
                CEnchantComponent(
                    level = 1,
                    enchantment = Enchantment.EFFICIENCY,
                    strict = EnchantStrict.ONLY_ENCHANT
                )
            )
        )

        val strict = CEnchantMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf(
                CEnchantComponent(
                    level = 1,
                    enchantment = Enchantment.EFFICIENCY,
                    strict = EnchantStrict.STRICT
                )
            )
        )

        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 2) to onlyEnchant,
                CoordinateComponent(0, 3) to strict
            ),
            type = CRecipeType.AMORPHOUS
        )

        val input1 = ItemStack(Material.STONE)
        input1.editMeta { meta -> meta.addEnchant(Enchantment.EFFICIENCY, 5, false) }

        val input2 = ItemStack(Material.STONE)
        input2.editMeta { meta -> meta.addEnchant(Enchantment.EFFICIENCY, 1, false) }

        val ui = CraftUI()
        ui.inventory.setItem(38, input1)
        ui.inventory.setItem(46, input2)

        val result = Search.search(
            crafterID = UUID.randomUUID(),
            view = ui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(1, result.size())
        assertEquals(null, result.vanilla())

        val (_, mapped) = result.customs().first()
        val recipeSet: MutableSet<CoordinateComponent> = mutableSetOf(
            CoordinateComponent(0, 2),
            CoordinateComponent(0, 3)
        )
        val inputSet: MutableSet<CoordinateComponent> = mutableSetOf(
            CoordinateComponent.fromIndex(38),
            CoordinateComponent.fromIndex(46)
        )
        for (c in mapped.components) {
            recipeSet.remove(c.recipe)
            inputSet.remove(c.input)
        }
        assertTrue(recipeSet.isEmpty())
        assertTrue(inputSet.isEmpty())
    }
    
    @Test
    fun enchantFilterTest1() {
        val noEnchantInput = ItemStack.of(Material.STONE)
        val onlyEnchantInput = ItemStack.of(Material.STONE).apply {
            itemMeta = itemMeta.apply {
                addEnchant(Enchantment.EFFICIENCY, 100, true)
            }
        }
        val strictEnchantInput = ItemStack.of(Material.STONE).apply {
            itemMeta = itemMeta.apply {
                addEnchant(Enchantment.EFFICIENCY, 1, true)
            }
        }

        val noEnchantMatter = CEnchantMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf()
        )
        val onlyEnchantMatter = CEnchantMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf(CEnchantComponent(150, Enchantment.EFFICIENCY, EnchantStrict.ONLY_ENCHANT))
        )
        val strictEnchantMatter = CEnchantMatterImpl(
            name = "",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf(CEnchantComponent(1, Enchantment.EFFICIENCY, EnchantStrict.STRICT))
        )

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            EnchantFilter.normal(noEnchantInput, noEnchantMatter).first
        )
        assertEquals(
            CRecipeFilter.ResultType.FAILED,
            EnchantFilter.normal(noEnchantInput, onlyEnchantMatter).first
        )
        assertEquals(
            CRecipeFilter.ResultType.FAILED,
            EnchantFilter.normal(noEnchantInput, strictEnchantMatter).first
        )

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            EnchantFilter.normal(onlyEnchantInput, noEnchantMatter).first
        )
        val (type1, result1) = EnchantFilter.normal(onlyEnchantInput, onlyEnchantMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type1)
        assertTrue(result1)
        val (type2, result2) = EnchantFilter.normal(onlyEnchantInput, strictEnchantMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type2)
        assertTrue(!result2)

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            EnchantFilter.normal(strictEnchantInput, noEnchantMatter).first
        )
        val (type3, result3) = EnchantFilter.normal(strictEnchantInput, onlyEnchantMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type3)
        assertTrue(result3)
        val (type4, result4) = EnchantFilter.normal(strictEnchantInput, strictEnchantMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type4)
        assertTrue(result4)
    }
    
    @Test
    fun enchantStoreFilterTest1() {
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

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            EnchantStorageFilter.normal(noEnchantInput, noEnchantMatter).first
        )
        assertEquals(
            CRecipeFilter.ResultType.FAILED,
            EnchantStorageFilter.normal(noEnchantInput, onlyEnchantMatter).first
        )
        assertEquals(
            CRecipeFilter.ResultType.FAILED,
            EnchantStorageFilter.normal(noEnchantInput, strictMatter).first
        )

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            EnchantStorageFilter.normal(onlyEnchantInput, noEnchantMatter).first
        )
        val (strictType1, strictResult1) = EnchantStorageFilter.normal(onlyEnchantInput, onlyEnchantMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, strictType1)
        assertTrue(strictResult1)
        val (strictType2, strictResult2) = EnchantStorageFilter.normal(onlyEnchantInput, strictMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, strictType2)
        assertTrue(!strictResult2)

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            EnchantStorageFilter.normal(strictInput, noEnchantMatter).first
        )
        val (strictType3, strictResult3) = EnchantStorageFilter.normal(strictInput, onlyEnchantMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, strictType3)
        assertTrue(strictResult3)
        val (strictType4, strictResult4) = EnchantStorageFilter.normal(strictInput, strictMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, strictType4)
        assertTrue(strictResult4)
    }
    
    @Test
    fun potionTest1() {
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

        val gui = CraftUI()
        gui.inventory.setItem(0, input1)
        gui.inventory.setItem(30, input1)

        val result = Search.search(
            UUID.randomUUID(),
            gui.toView(),
            sourceRecipes = listOf(recipe)
        )

        assertEquals(null, result.vanilla())
        assertEquals(1, result.customs().size)
        val (returnedRecipe, mapped) = result.customs().first()
        assertEquals(recipe, returnedRecipe)
        assertEquals(2, mapped.components.size)
        val recipeSet: MutableSet<CoordinateComponent> = mutableSetOf(
            CoordinateComponent(0, 0),
            CoordinateComponent(0, 1)
        )
        val inputSet: MutableSet<CoordinateComponent> = mutableSetOf(
            CoordinateComponent(0, 0),
            CoordinateComponent.fromIndex(30)
        )
        for (c in mapped.components) {
            recipeSet.remove(c.recipe)
            inputSet.remove(c.input)
        }
        assertTrue(recipeSet.isEmpty())
        assertTrue(inputSet.isEmpty())
    }
    
    @Test
    fun potionFilterTest1() {
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

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            PotionFilter.normal(noEffectPotionInput, noEffectMatter).first
        )
        assertEquals(
            CRecipeFilter.ResultType.FAILED,
            PotionFilter.normal(noEffectPotionInput, onlyEffectMatter).first
        )
        assertEquals(
            CRecipeFilter.ResultType.FAILED,
            PotionFilter.normal(noEffectPotionInput, strictEffectMatter).first
        )

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            PotionFilter.normal(onlyEffectPotionInput, noEffectMatter).first
        )
        val (type1, result1) = PotionFilter.normal(onlyEffectPotionInput, onlyEffectMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type1)
        assertTrue(result1)
        val (type2, result2) = PotionFilter.normal(onlyEffectPotionInput, strictEffectMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type2)
        assertTrue(!result2)

        assertEquals(
            CRecipeFilter.ResultType.NOT_REQUIRED,
            PotionFilter.normal(strictPotionInput, noEffectMatter).first
        )
        val (type3, result3) = PotionFilter.normal(strictPotionInput, onlyEffectMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type3)
        assertTrue(result3)
        val (type4, result4) = PotionFilter.normal(strictPotionInput, strictEffectMatter)
        assertEquals(CRecipeFilter.ResultType.SUCCESS, type4)
        assertTrue(result4)
    }
}
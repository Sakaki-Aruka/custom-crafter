package online.aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipePredicate
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent
import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantmentStoreMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.matter.potion.CPotionMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
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
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis
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
            type = CRecipe.Type.SHAPED
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
    fun shapelessTest1() {
        val matter = CMatterImpl.single(Material.STONE)
        val recipe = CRecipeImpl(
            name = "",
            items = CoordinateComponent.getN(4).associateWith { _ -> matter },
            type = CRecipe.Type.SHAPELESS
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
    fun shapelessTest2() {
        // mass test
        val slimeBall = CMatterImpl.single(Material.SLIME_BALL)
        val lavaBucket = CMatterImpl("", setOf(Material.LAVA_BUCKET), mass = true)
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to slimeBall,
                CoordinateComponent(0, 1) to lavaBucket
            ),
            type = CRecipe.Type.SHAPELESS
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
    fun detectUnmatchMaterialCandidateInShapelessRecipe() {
        // Recipe: requires 9 stones
        // Input : provides 8 stones and 1 cobblestone
        // -> Fail to mapping

        val stone = CMatterImpl.single(Material.STONE)
        // ###
        // # #
        // ###
        // # = STONE
        val recipe = CRecipeImpl.shapeless("", List(9) {stone})

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
    fun detectUnmatchEnchantCandidateInShapelessRecipe() {
        // Recipe: requires 3 enchanted (efficiency, lv 5, strict) stones
        // Input : provides 3 enchanted (efficiency, lv 5 x2, lv4 x1) stones
        // -> Fail to mapping
        val matter = CEnchantMatterImpl(
            name = "lv5EfficiencyStone",
            candidate = setOf(Material.STONE),
            enchantComponents = setOf(
                CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.STRICT)
            )
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(0, 2) to matter
            ),
            type = CRecipe.Type.SHAPELESS
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
    fun detectUnmatchEnchantStorageCandidateInShapelessRecipe() {
        // Recipe: requires 3 enchanted books (efficiency, lv5, strict)
        // Input : provides 3 enchanted books (efficiency, lv5x2, lv4x1)
        // -> Fail to mapping
        val matter = CEnchantmentStoreMatterImpl(
            name = "book",
            candidate = setOf(Material.ENCHANTED_BOOK),
            storedEnchantComponents = setOf(
                CEnchantComponent(5, Enchantment.EFFICIENCY, CEnchantComponent.Strict.STRICT)
            )
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(0, 2) to matter
            ),
            type = CRecipe.Type.SHAPELESS
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
    fun detectUnmatchPotionCandidateInShapelessRecipe() {
        // Recipe: requires 3 potions (luck, lv5, strict)
        // Input : provides 3 potions (luck, lv5x2, lv4x1)
        // -> Fail to mapping
        val matter = CPotionMatterImpl(
            name = "potion",
            candidate = setOf(Material.POTION),
            potionComponents = setOf(
                CPotionComponent(
                    PotionEffect(PotionEffectType.LUCK, 1, 5),
                    CPotionComponent.Strict.STRICT
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
            type = CRecipe.Type.SHAPELESS
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
    fun detectUnmatchMatterPredicateInShapelessRecipe() {
        // Recipe: requires to craft by Notch
        // Action: craft by empty UUID user
        // -> Fail to mapping
        val NotchID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
        val emptyID = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val matter = CMatterImpl(
            name = "",
            candidate = setOf(Material.GRAVEL),
            predicates = setOf(CMatterPredicate { ctx -> ctx.crafterID == NotchID })
        )
        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 0) to matter,
                CoordinateComponent(1, 0) to matter,
                CoordinateComponent(0, 1) to matter,
                CoordinateComponent(1, 1) to matter
            ),
            type = CRecipe.Type.SHAPELESS
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
    fun detectUnmatchMatterAmountInShapelessRecipe() {
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
            type = CRecipe.Type.SHAPELESS
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
                    strict = CEnchantComponent.Strict.ONLY_ENCHANT
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
                    strict = CEnchantComponent.Strict.STRICT
                )
            )
        )

        val recipe = CRecipeImpl(
            name = "",
            items = mapOf(
                CoordinateComponent(0, 2) to onlyEnchant,
                CoordinateComponent(0, 3) to strict
            ),
            type = CRecipe.Type.SHAPELESS
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
    fun potionTest1() {
        val onlyEffect = CPotionMatterImpl(
            "oe",
            setOf(Material.POTION),
            setOf(CPotionComponent(
                PotionEffect(
                    PotionEffectType.POISON, 1, 3),
                CPotionComponent.Strict.ONLY_EFFECT
            )))

        val strict = CPotionMatterImpl(
            "s",
            setOf(Material.POTION),
            setOf(CPotionComponent(
                PotionEffect(
                    PotionEffectType.POISON, 1, 1),
                CPotionComponent.Strict.STRICT
            )))

        val recipe: CRecipe = CRecipeImpl(
            "potionTestRecipe",
            mapOf(
                CoordinateComponent(0, 0) to onlyEffect,
                CoordinateComponent(0, 1) to strict
            ),
            CRecipe.Type.SHAPELESS
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
    fun asyncShapedSearchTest() {
        val matter = CMatterImpl.of(Material.STONE)
        val recipe = CRecipeImpl(
            "",
            CoordinateComponent.squareFill(6).associateWith { matter },
            CRecipe.Type.SHAPED
        )

        val view = CraftView(
            materials = CoordinateComponent.squareFill(6).associateWith { ItemStack.of(Material.STONE) },
            result = ItemStack.empty()
        )

        val future: CompletableFuture<Search.SearchResult> = Search.asyncSearch(crafterID = UUID.randomUUID(), view, listOf(recipe))
        val result = future.get()

        assertEquals(1, result.size())
        assertEquals(1, result.customs().size)
    }

    @Test
    fun asyncShapelessSearchTest() {
        val matter = CMatterImpl.of(Material.STONE)
        val recipePredicate = CRecipePredicate {
            Thread.sleep(3000)
            true
        }
        val recipe = CRecipeImpl(
            "",
            CoordinateComponent.squareFill(6).associateWith { matter },
            predicates = listOf(recipePredicate),
            type = CRecipe.Type.SHAPELESS
        )

        val view = CraftView(
            materials = CoordinateComponent.squareFill(6).associateWith { ItemStack.of(Material.STONE) },
            result = ItemStack.empty()
        )

        val times = 1000
        val list = (0..<times).map { recipe }

        val future: CompletableFuture<Search.SearchResult> = Search.asyncSearch(crafterID = UUID.randomUUID(), view, list)
        var result: Search.SearchResult

        println("time: ${measureTimeMillis { result = future.get() }} ms")

        assertEquals(times, result.size())
        assertEquals(times, result.customs().size)
    }
}
package online.aruka.customcrafter.api.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.AutoCraftRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal object CBlockTest {

    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        server.addWorld(WorldMock())
        server.addPlayer()

        CustomCrafterAPI.setAutoCraftingBaseBlock(Material.GOLD_BLOCK)

        server.worlds.first().let { world ->
            val base = world.getBlockAt(0, 64, 0)
            base.type = Material.CRAFTER

            for (dx in (-1..1)) {
                for (dz in (-1..1)) {
                    if (dx == 0 && dz == 0) {
                        continue
                    }
                    base.getRelative(dx, -1, dz).type = Material.GOLD_BLOCK
                }
            }
        }

        CBlockDB.initTables(useInMemoryDatabase = true)
        try {
            CBlockDB.unlink(server.worlds.first().getBlockAt(0, 64, 0))
        } catch (_: Exception) {
            //
        }
    }

    @AfterEach
    fun tearDown() {
        val recipes = CustomCrafterAPI.getRecipes()
        recipes.forEach { r -> CustomCrafterAPI.unregisterRecipe(r) }
        try {
            CBlockDB.unlink(server.worlds.first().getBlockAt(0, 64, 0))
        } catch (_: Exception) {
            //
        }

        MockBukkit.unmock()
    }

    @Test
    fun linkTest() {
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val block = server.worlds.first().getBlockAt(0, 64, 0)

        assertTrue(CBlockDB.link(block, recipe, listOf(ItemStack(Material.STONE))) != null)
        assertTrue(CBlockDB.isLinked(block))
        assertTrue(CBlockDB.getContainedItems(block).size == 1)
    }

    @Test
    fun linkWithoutItemsTest() {
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val block = server.worlds.first().getBlockAt(0, 64, 0)

        assertTrue(CBlockDB.linkWithoutItems(block, recipe) != null)
        assertTrue(CBlockDB.isLinked(block))
        assertTrue(CBlockDB.getContainedItems(block).isEmpty())
    }

    @Test
    fun unlinkTest() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )

        assertTrue(CBlockDB.linkWithoutItems(block, recipe) != null)

        try {
            CBlockDB.unlink(block)
        } catch (_: Exception) {
            // Skipped this cause MockBukkit does not have `Crafter` implementation class
            // no Crafter impl class -> PersistentDataContainer access will be fail
        }

        assertFalse(CBlockDB.isLinked(block))
    }

    @Test
    fun addItemTest() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )

        assertTrue(CBlockDB.linkWithoutItems(block, recipe) != null)

        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))

        val containedItems = CBlockDB.getContainedItems(block)
        assertTrue(containedItems.size == 8)
        assertTrue(containedItems.all { item -> item.isSimilar(ItemStack(Material.STONE)) })

        assertFalse(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))
    }

    @Test
    fun clearItemTest() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )

        assertTrue(CBlockDB.linkWithoutItems(block, recipe) != null)

        assertTrue(CBlockDB.addItems(cBlock, ItemStack(Material.STONE)))

        CBlockDB.clearContainedItems(block)
        assertTrue(CBlockDB.getContainedItems(block).isEmpty())
    }

    @Test
    fun containedItemsCacheTest() {
        val block = server.worlds.first().getBlockAt(0, 64, 0)
        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )

        CustomCrafterAPI.registerRecipe(recipe)

        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            type = recipe.type,
            name = recipe.name,
            publisherName = recipe.publisherPluginName,
            slots = recipe.items.keys.map { c -> c.toIndex() }.toList(),
            block = block
        )

        assertFalse(cBlock.isPlayerModifyMode())

        CBlockDB.linkWithoutItems(cBlock.block, cBlock.getRecipe()!!)
        assertTrue(cBlock.enterPlayerModifyMode())
        assertTrue(cBlock.setToCache(0, ItemStack.of(Material.STONE)))
        assertTrue(cBlock.getCacheItems() != null)
        assertTrue(cBlock.getCacheItems()!!.size == 8)
        assertTrue(cBlock.getCacheItems()!!.count { item -> item.type == Material.STONE } == 1)

        cBlock.removeCache(0)
        assertTrue(cBlock.getCacheItems()!!.count { item -> item.type == Material.STONE } == 0)

        repeat(8) { i ->
            assertTrue(cBlock.setToCache(i, ItemStack.of(Material.STONE)))
        }

        assertThrows<IllegalArgumentException> {
            cBlock.setToCache(8, ItemStack.of(Material.STONE))
        }
    }
}
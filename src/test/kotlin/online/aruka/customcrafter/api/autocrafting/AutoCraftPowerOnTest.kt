package online.aruka.customcrafter.api.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.AutoCraftRecipeImpl
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.listener.AutoCraftPowerOnListener
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.Test
import kotlin.test.assertTrue

object AutoCraftPowerOnTest {
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

        val matter = CMatterImpl.single(Material.STONE)
        val items = CoordinateComponent.square(3).associateWith { c -> matter }
        val recipe = AutoCraftRecipeImpl(
            name = "",
            items = items,
            type = CRecipeType.NORMAL,
            publisherPluginName = "Custom_Crafter"
        )
        CustomCrafterAPI.registerRecipe(recipe)

        CBlockDB.initTables(useInMemoryDatabase = true)
        try {
            CBlockDB.unlink(server.worlds.first().getBlockAt(0, 64, 0))
        } catch (_: Exception) {
            //
        }
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun turnOnTest() {
        val recipe: AutoCraftRecipe = CustomCrafterAPI.getRecipes().filterIsInstance<AutoCraftRecipe>().first()
        val block: Block = server.worlds.first().getBlockAt(0, 64, 0)
        val items: List<ItemStack> = List(8) { ItemStack(Material.STONE) }
        val cBlock = CBlockDB.link(block, recipe, items)

        assertTrue(cBlock != null)
        assertTrue(cBlock.getContainedItems().size == 8)

        AutoCraftPowerOnListener.turnOn(cBlock)

        assertTrue(cBlock.getContainedItems().isEmpty())
    }
}
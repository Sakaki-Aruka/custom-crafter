package online.aruka.custom_crafter.api.ui

import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.ui.AllCandidateUIDesigner
import io.github.sakaki_aruka.customcrafter.ui.AllCandidateUIDesigner.Companion.bakeWithEmptyContext
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertTrue

object AllCandidateUIDesignerTest {
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
    fun deflectEmptyRecipeSlotsTest() {
        val empty: AllCandidateUIDesigner = object : AllCandidateUIDesigner {
            override fun recipeSlots(context: AllCandidateUIDesigner.Context): Set<CoordinateComponent> {
                return emptySet()
            }
        }

        assertTrue(empty.bakeWithEmptyContext().isValid().isFailure)
    }

    @Test
    fun deflectOversizeRecipeSlotsTest() {
        val oversize: AllCandidateUIDesigner = object : AllCandidateUIDesigner {
            override fun recipeSlots(context: AllCandidateUIDesigner.Context): Set<CoordinateComponent> {
                return (0..54).map { CoordinateComponent.fromIndex(it) }.toSet()
            }
        }

        assertTrue(oversize.bakeWithEmptyContext().isValid().isFailure)
    }

    @Test
    fun deflectInvalidCoordinateButtonTest() {
        val previous: AllCandidateUIDesigner = object : AllCandidateUIDesigner {
            override fun previousPageButton(context: AllCandidateUIDesigner.Context): Pair<CoordinateComponent, ItemStack> {
                return CoordinateComponent(-1, 0) to ItemStack.of(Material.STONE)
            }
        }
        assertTrue(previous.bakeWithEmptyContext().isValid().isFailure)
    }

    @Test
    fun detectDuplicatedCoordinateButtonTest() {
        val duplicated: AllCandidateUIDesigner = object : AllCandidateUIDesigner {
            override fun recipeSlots(context: AllCandidateUIDesigner.Context): Set<CoordinateComponent> {
                return (0..<45).map { CoordinateComponent.fromIndex(it) }.toSet()
            }

            override fun previousPageButton(context: AllCandidateUIDesigner.Context): Pair<CoordinateComponent, ItemStack> {
                return CoordinateComponent(0, 0) to ItemStack.of(Material.STONE)
            }
        }
        // recipe slots set contains (0, 0)
        // and previous button placed (0, 0)

        assertTrue(duplicated.bakeWithEmptyContext().isValid().isFailure)
    }
}
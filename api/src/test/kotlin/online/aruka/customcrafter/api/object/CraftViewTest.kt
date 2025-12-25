package online.aruka.customcrafter.api.`object`

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object CraftViewTest {
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
    fun decrementViewTest() {
        /*
         * mass matter        : false
         * shift              : false
         * .amount >= 1 matter: false
         */
        val matter = CMatterImpl.of(Material.STONE)
        val map: Map<CoordinateComponent, CMatter> = CoordinateComponent.squareFill(4).associateWith { matter }
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(4).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.squareFill(4).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = false, recipe, relation)

        assertTrue(
            decremented.materials.all { (_, item) -> item.amount == 63 }
        )
    }

    @Test
    fun decrementViewWithMultiAmountMatterRecipeTest() {
        /*
         * mass matter        : false
         * shift              : false
         * .amount >= 1 matter: true
         */
        val matter = CMatterImpl("", setOf(Material.STONE), amount = 4)
        val map: Map<CoordinateComponent, CMatter> = CoordinateComponent.squareFill(4).associateWith { matter }
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(4).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.squareFill(4).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = false, recipe, relation)

        assertTrue(
            decremented.materials.all { (_, item) -> item.amount == 60 }
        )
    }

    @Test
    fun decrementViewWithShiftTest() {
        /*
         * mass matter        : false
         * shift              : true
         * .amount >= 1 matter: false
         */
        val matter = CMatterImpl.of(Material.STONE)
        val map: Map<CoordinateComponent, CMatter> = CoordinateComponent.squareFill(4).associateWith { matter }
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(4).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.squareFill(4).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = true, recipe, relation)

        assertTrue(
            decremented.materials.all { (_, item) -> item.type.isAir }
        )
    }

    @Test
    fun decrementViewWithMultiAmountMatterRecipeAndShiftTest() {
        /*
         * mass matter        : false
         * shift              : true
         * .amount >= 1 matter: true
         */
        val matter = CMatterImpl("", setOf(Material.STONE), amount = 4)
        val map: Map<CoordinateComponent, CMatter> = CoordinateComponent.squareFill(4).associateWith { matter }
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(4).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.squareFill(4).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = true, recipe, relation)

        assertTrue(decremented.materials.all { (_, item) -> item.type.isAir })
    }

    @Test
    fun decrementViewWithMassMatterRecipeTest() {
        /*
         * mass matter        : true
         * shift              : false
         * .amount >= 1 matter: false
         */
        val matter = CMatterImpl.of(Material.STONE)
        val massMatter = CMatterImpl("", setOf(Material.STONE), mass = true)
        val map = CoordinateComponent.square(3).associateWith { matter } + mapOf(CoordinateComponent(1, 1) to massMatter)
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(3).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.squareFill(3).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = false, recipe, relation)

        assertTrue(decremented.materials.all { (_, item) -> item.amount == 63 })
    }

    @Test
    fun decrementViewWithMassMatterRecipeAndShiftTest() {
        /*
         * mass matter        : true
         * shift              : true
         * .amount >= 1 matter: false
         */
        val matter = CMatterImpl.of(Material.STONE)
        val massMatter = CMatterImpl("", setOf(Material.WATER_BUCKET), mass = true)
        val map = CoordinateComponent.square(3).associateWith { matter } + mapOf(CoordinateComponent(1, 1) to massMatter)
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(3).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.square(3).associateWith { ItemStack.of(Material.STONE, 64) } + mapOf(
                CoordinateComponent(1, 1) to ItemStack.of(Material.WATER_BUCKET, 2)),
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = true, recipe, relation)

        assertTrue(
            /*
             * #: Air, =: Not Air
             * ###
             * #=#
             * ###
             */
            CoordinateComponent.square(3).all {
                decremented.materials.getValue(it).type.isAir
            }
        )

        assertEquals(1, decremented.materials.getValue(CoordinateComponent(1, 1)).amount)
    }

    @Test
    fun decrementViewWithMassMatterRecipeAndMultiAmountMatterRecipeTest() {
        /*
         * mass matter        : true
         * shift              : false
         * .amount >= 1 matter: true
         */
        val multiMatter = CMatterImpl("", setOf(Material.STONE), amount = 4)
        val massMatter = CMatterImpl("", setOf(Material.STONE), mass = true, amount = 4)
        val map = CoordinateComponent.square(3).associateWith { multiMatter } + mapOf(CoordinateComponent(1, 1) to massMatter)
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(3).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.squareFill(3).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = false, recipe, relation)

        assertTrue(
            CoordinateComponent.square(3).all {
                decremented.materials.getValue(it).amount == 60
            }
        )

        assertEquals(63, decremented.materials.getValue(CoordinateComponent(1, 1)).amount)
    }

    @Test
    fun decrementViewWithMassMatterRecipeShiftAndMultiAmountMatterRecipeTest() {
        /*
         * mass matter        : true
         * shift              : true
         * .amount >= 1 matter: true
         */
        val multiMatter = CMatterImpl("", setOf(Material.STONE), amount = 4)
        val massMatter = CMatterImpl("", setOf(Material.STONE), mass = true, amount = 4)
        val map = CoordinateComponent.square(3).associateWith { multiMatter } + mapOf(CoordinateComponent(1, 1) to massMatter)
        val recipe = CRecipeImpl("", map, CRecipe.Type.SHAPED)
        val relation = MappedRelation(
            CoordinateComponent.squareFill(3).map {
                MappedRelationComponent(it, it)
            }.toSet()
        )
        val view = CraftView(
            materials = CoordinateComponent.squareFill(3).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.empty()
        )

        val decremented = view.getDecremented(shiftUsed = true, recipe, relation)

        assertTrue(
            CoordinateComponent.square(3).all {
                decremented.materials.getValue(it).type.isAir
            }
        )

        assertEquals(63, decremented.materials.getValue(CoordinateComponent(1, 1)).amount)
    }

    @Test
    fun cloneTest() {
        val source = CraftView(
            materials = CoordinateComponent.squareFill(6).associateWith { ItemStack.of(Material.STONE, 64) },
            result = ItemStack.of(Material.DIRT, 16)
        )

        val cloned = source.clone()

        assertTrue(
            CoordinateComponent.squareFill(6).all {
                val item = cloned.materials.getValue(it)
                item.type == Material.STONE && item.amount == 64
            }
        )

        assertTrue(cloned.result.type == Material.DIRT && cloned.result.amount == 16)
    }
}
package online.aruka.custom_crafter.api.impl.recipe

import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.recipe.MatchGroup
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

object MatchGroupTest {

    @Test
    fun ofSuccess() {
        val group = MatchGroup.of(
            members = setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 0)),
            min = 1,
            max = 2
        )
        assertEquals(2, group.members.size)
        assertEquals(1, group.min)
        assertEquals(2, group.max)
    }

    @Test
    fun mandatoryIsMinMaxOne() {
        val group = MatchGroup.mandatory(CoordinateComponent(0, 0))
        assertEquals(1, group.members.size)
        assertEquals(1, group.min)
        assertEquals(1, group.max)
    }

    @Test
    fun emptyMembersThrows() {
        assertThrows<IllegalArgumentException> {
            MatchGroup.of(members = emptySet(), min = 0, max = 0)
        }
    }

    @Test
    fun negativeMinThrows() {
        assertThrows<IllegalArgumentException> {
            MatchGroup.of(members = setOf(CoordinateComponent(0, 0)), min = -1, max = 1)
        }
    }

    @Test
    fun maxLessThanMinThrows() {
        assertThrows<IllegalArgumentException> {
            MatchGroup.of(members = setOf(CoordinateComponent(0, 0), CoordinateComponent(1, 0)), min = 2, max = 1)
        }
    }

    @Test
    fun maxExceedingMembersSizeThrows() {
        assertThrows<IllegalArgumentException> {
            MatchGroup.of(members = setOf(CoordinateComponent(0, 0)), min = 0, max = 2)
        }
    }
}

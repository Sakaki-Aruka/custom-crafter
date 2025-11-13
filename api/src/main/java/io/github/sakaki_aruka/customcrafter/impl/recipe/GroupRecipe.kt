package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.reflect.KClassifier

class GroupRecipe (
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    override val filters: Set<CRecipeFilter<CMatter>>,
    val groups: Set<Context>,
    override val containers: List<CRecipeContainer>?,
    override val results: List<ResultSupplier>?,
    override val type: CRecipeType = CRecipeType.NORMAL
): CRecipe {

    companion object {
        fun createFilters(
            filters: Set<CRecipeFilter<CMatter>>
        ): Set<CRecipeFilter<CMatter>> {
            return setOf(Filter(filters.associateBy { it::class }))
        }

        fun createGroups(
            items: Map<CoordinateComponent, CMatter>,
            missingGroups: Set<Context>
        ): Set<Context> {
            val grouped: Set<CoordinateComponent> = missingGroups.map { it.members }.flatten().toSet()
            val result: MutableSet<Context> = mutableSetOf(*missingGroups.toTypedArray())
            for (c in items.keys - grouped) {
                result.add(Context.default(c))
            }

            Context.isValidGroups(result).getOrThrow()
            return result
        }
    }

    class Context internal constructor(
        val members: Set<CoordinateComponent>,
        val min: Int,
        val name: String = UUID.randomUUID().toString()
    ) {
        companion object {
            fun of(
                members: Set<CoordinateComponent>,
                min: Int,
                name: String
            ): Context {
                if (members.isEmpty()) {
                    throw IllegalArgumentException("")
                } else if (min < 0) {
                    throw IllegalArgumentException("")
                }
                return Context(members, min, name)
            }

            fun default(
                coordinate: CoordinateComponent
            ): Context {
                return Context(
                    min = 1,
                    members = setOf(coordinate),
                    name = "DefaultContext: ${UUID.randomUUID()}"
                )
            }

            fun isValidGroups(groups: Set<Context>): Result<Unit> {
                // TODO: impl here
            }
        }
    }

    class Filter internal constructor(
        val filterMapping: Map<KClassifier, CRecipeFilter<CMatter>>
    ): CRecipeFilter<Matter> {
        override fun itemMatterCheck(
            item: ItemStack,
            matter: Matter
        ): Pair<CRecipeFilter.ResultType, Boolean> {
            return this.filterMapping[matter.original::class]
                ?.itemMatterCheck(item, matter.original)
                ?: CRecipeFilter.CHECK_NOT_REQUIRED
        }
    }

    class Matter internal constructor(
        override val name: String,
        override val candidate: Set<Material>,
        override val amount: Int,
        override val mass: Boolean,
        override val predicates: Set<CMatterPredicate>?,
        val original: CMatter
    ): CMatter {
        companion object {
            fun of(
                matter: CMatter,
                includeAir: Boolean
            ): Matter {
                return Matter(
                    name = matter.name,
                    candidate = if (includeAir) matter.candidate + Material.AIR else matter.candidate,
                    amount = matter.amount,
                    mass = matter.mass,
                    predicates = /* TODO: impl here*/,
                    original = matter
                )
            }
        }

        override fun isValidMatter(): Result<Unit> {
            return if (this.candidate.isEmpty()) {
                Result.failure(IllegalStateException("'candidate' must contain correct materials at least one."))
            } else if (this.candidate.any { !it.isItem }) {
                Result.failure(IllegalStateException("'candidate' not allowed to contain materials that are '!Material#isItem'."))
            } else if (this.amount < 1) {
                Result.failure(IllegalStateException("'amount' must be 1 or more."))
            } else Result.success(Unit)
        }

    }

    override fun replaceItems(newItems: Map<CoordinateComponent, CMatter>): GroupRecipe {
        return GroupRecipe(
            name = this.name,
            items = newItems,
            filters = this.filters,
            groups = this.groups,
            containers = this.containers,
            results = this.results
        )
    }

    override fun requiresInputItemAmountMin(): Int {

    }

    override fun isValidRecipe(): Result<Unit> {
        //
    }
}
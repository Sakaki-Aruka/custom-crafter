package io.github.sakaki_aruka.customcrafter.impl.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatterPredicate
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterPredicateImpl
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.reflect.KClassifier

/**
 * Implementation of [CRecipe].
 *
 * This recipe only provides [CRecipeType.NORMAL].
 *
 *
 * @param[name] Name of this recipe
 * @param[items] Item mapping
 * @param[groups] Air-containable context set. It can be empty.
 * @param[filters] [CRecipeFilter] of apply this recipe. This is required if items contains a CMatter that requires a CRecipeFilter. Use the set created by [GroupRecipe.createFilters].
 * @param[containers] Containers when run on finished to search
 * @param[results] [ResultSupplier] list
 * @param[type] Always be [CRecipeType.NORMAL]
 * @see[CRecipe]
 * @see[Matter]
 * @see[Context]
 * @see[Filter]
 * @since 5.0.15
 */
class GroupRecipe (
    override val name: String,
    override val items: Map<CoordinateComponent, CMatter>,
    val groups: Set<Context>,
    override val filters: List<CRecipeFilter<CMatter>>? = null,
    override val containers: List<CRecipeContainer>? = null,
    override val results: List<ResultSupplier>? = null,
    override val type: CRecipeType = CRecipeType.NORMAL
): CRecipe {

    companion object {
        fun createFilters(
            filters: Collection<CRecipeFilter<CMatter>>
        ): List<CRecipeFilter<CMatter>> {
            return listOf(Filter(filters.filter { it !is Filter }.associateBy { it::class }))
        }

        fun createGroups(
            items: Map<CoordinateComponent, CMatter>,
            missingGroups: Set<Context>
        ): Set<Context> {
            val grouped: Set<CoordinateComponent> = missingGroups.map { it.members }.flatten().toSet()
            val result: MutableSet<Context> = missingGroups.toMutableSet()
            for (c in items.keys - grouped) {
                result.add(Context.default(c))
            }

            Context.isValidGroups(result, items).exceptionOrNull()?.let { throw it }
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
                name: String = UUID.randomUUID().toString()
            ): Context {
                if (members.isEmpty()) {
                    throw IllegalArgumentException("'members' must not be empty.")
                } else if (min < 0) {
                    throw IllegalArgumentException("'min' must be zero or positive number.")
                }
                return Context(members, min, name)
            }

            fun default(
                coordinate: CoordinateComponent
            ): Context {
                return Context(
                    min = 1,
                    members = setOf(coordinate),
                    name = "DefaultContext: RandomId-${UUID.randomUUID().toString().take(5)}"
                )
            }

            fun isValidGroups(
                groups: Set<Context>,
                items: Map<CoordinateComponent, CMatter>
            ): Result<Unit> {
                if (groups.isEmpty()) {
                    return Result.success(Unit)
                } else if (groups.any { it.members.isEmpty() }) {
                    return Result.failure(IllegalArgumentException("'GroupRecipe.Context' must contains any coordinates."))
                }

                val counts: MutableMap<CoordinateComponent, Int> = mutableMapOf()
                for (group in groups) {
                    for (c in group.members) {
                        counts[c] = (counts[c] ?: 0) + 1
                    }
                }

                if (!items.keys.containsAll(counts.keys)) {
                    val builder = StringBuilder("All CoordinateComponents included in the members of the Context in 'groups' must exist as keys in 'items'.${System.lineSeparator()}")
                    for (notContained in counts.keys - items.keys) {
                        builder.append("  'items' not contains: $notContained ${System.lineSeparator()}")
                    }
                    return Result.failure(IllegalArgumentException(builder.toString()))
                } else if (counts.values.any { it > 1 }) {
                    val builder = StringBuilder()
                    builder.append("GroupRecipe.Context is not allowed to contain duplicate coordinates. ${System.lineSeparator()}")
                    for ((c, count) in counts.filter { (_, c) -> c > 1 }) {
                        builder.append("  duplicated: (${c.x}, ${c.y}), times: $count ${System.lineSeparator()}")
                    }
                    return Result.failure(IllegalArgumentException(builder.toString()))
                }

                for (ctx in groups) {
                    val airContainableCount: Int = ctx.members.count { c ->
                        items.getValue(c).candidate.any { m -> m.isAir }
                    }
                    if (airContainableCount < ctx.members.size - ctx.min) {
                        return Result.failure(IllegalArgumentException("There are not enough CMatters that are Air-Containable. (Required: ${ctx.members.size - ctx.min}, Provided: ${airContainableCount})"))
                    }
                }

                val minCoordinate: CoordinateComponent = items.entries.minBy { (c, _) -> c.toIndex() }.key

                if (items.getValue(minCoordinate).candidate.any { it.isAir }) {
                    return Result.failure(IllegalArgumentException("The GroupRecipe.Matter with the smallest CoordinateComponent#toIndex in the recipe cannot have an element in candidate that satisfies Material#isAir."))
                }

                return Result.success(Unit)
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

    /**
     * Air-containable [CMatter] implementation.
     *
     * Use [Matter.of] to initialize.
     * @see[CMatter]
     * @see[GroupRecipe]
     * @since 5.0.15
     */
    class Matter internal constructor(
        override val name: String,
        override val candidate: Set<Material>,
        override val amount: Int,
        override val mass: Boolean,
        override val predicates: List<CMatterPredicate>?,
        val original: CMatter
    ): CMatter {

        private class InspectionResult(
            val createdBy: Int,
            val result: Map<Int, Boolean>,
            val resultConsumed: MutableSet<Int> = mutableSetOf()
        ) {
            fun copy(
                createdBy: Int = this.createdBy,
                result: Map<Int, Boolean> = this.result.toMap(),
                resultConsumed: MutableSet<Int> = this.resultConsumed.toMutableSet()
            ): InspectionResult {
                return InspectionResult(createdBy, result, resultConsumed)
            }
        }

        companion object {
            /**
             * Create [Matter] instance from [CMatter].
             *
             * @throws[IllegalArgumentException] If specified [matter] already has been [Matter]
             * @throws[IllegalStateException] If found some invalid part from specified matter. From [Matter.isValidMatter]
             * @param[matter] Base matter
             * @param[includeAir] Add 'Material.AIR' to [matter] candidate or not. (Default false)
             * @since 5.0.15
             */
            fun of(
                matter: CMatter,
                includeAir: Boolean = false
            ): Matter {
                if (matter is Matter) {
                    throw IllegalArgumentException("'matter' must not be 'GroupRecipe.Matter'.")
                }
                val matter = Matter(
                    name = matter.name,
                    candidate = if (includeAir) matter.candidate + Material.AIR else matter.candidate,
                    amount = matter.amount,
                    mass = matter.mass,
                    predicates = listOf(INSPECTOR, CHECKER),
                    original = matter
                )

                matter.isValidMatter().exceptionOrNull()?.let { throw it }
                return matter
            }

            private val INSPECTION_RESULTS: MutableMap<UUID, InspectionResult> = mutableMapOf()
            private fun putResult(key: UUID, result: InspectionResult) {
                synchronized(INSPECTION_RESULTS) { INSPECTION_RESULTS[key] = result }
            }

            private fun getCurrent(key: UUID): InspectionResult? {
                synchronized(INSPECTION_RESULTS) { return INSPECTION_RESULTS[key]?.copy() }
            }

            private fun remove(key: UUID) {
                synchronized(INSPECTION_RESULTS) { INSPECTION_RESULTS.remove(key) }
            }

            private val INSPECTOR: CMatterPredicate = CMatterPredicateImpl { ctx ->
                if (ctx.recipe !is GroupRecipe /* || ctx.matter !is Matter */) {
                    return@CMatterPredicateImpl true
                } else if (getCurrent(ctx.crafterID) != null) {
                    return@CMatterPredicateImpl true
                }

                val recipeFirstCoordinate: CoordinateComponent = ctx.recipe.items.keys.minBy { it.toIndex() }
                val inputFirstCoordinate: CoordinateComponent = ctx.mapped.keys.minBy { it.toIndex() }
                val dx: Int = recipeFirstCoordinate.x - inputFirstCoordinate.x
                val dy: Int = recipeFirstCoordinate.y - inputFirstCoordinate.y
                val groupResult: MutableMap<Context, Int> = mutableMapOf()
                for ((c, matter) in ctx.recipe.items.entries) {
                    val context: Context = ctx.recipe.groups.firstOrNull { c in it.members } ?: continue
                    val input: ItemStack = ctx.mapped[CoordinateComponent(c.x - dx, c.y - dy)]
                        ?: ItemStack.empty()
                    val s: Int = if (input.type.isAir) 0 else 1
                    groupResult[context] = groupResult.getOrDefault(context, 0) + s
                }

                val coordinateResult: MutableMap<Int, Boolean> = mutableMapOf()
                for ((context, amount) in groupResult.entries) {
                    val checkedResult = context.min <= amount
                    for (coordinate in context.members) {
                        coordinateResult[coordinate.toIndex()] = checkedResult
                    }
                }
                val inspectionResults = InspectionResult(
                    createdBy = ctx.coordinate.toIndex(),
                    result = coordinateResult
                )
                putResult(ctx.crafterID, inspectionResults)
                true
            }

            private val CHECKER: CMatterPredicate = CMatterPredicateImpl { ctx ->
                if (ctx.recipe !is GroupRecipe /* || ctx.matter !is Matter */) {
                    return@CMatterPredicateImpl ctx.matter.predicatesResult(ctx)
                }
                val inspectionResult: InspectionResult = getCurrent(ctx.crafterID)
                    ?: return@CMatterPredicateImpl if (ctx.matter is Matter) ctx.matter.original.predicatesResult(ctx)
                        else ctx.matter.predicatesResult(ctx)
                val result: Boolean = inspectionResult.result[ctx.coordinate.toIndex()] ?: true
                if (inspectionResult.resultConsumed.size == ctx.recipe.groups.sumOf { it.members.size } - 1) {
                    // last
                    remove(ctx.crafterID)
                } else {
                    putResult(
                        ctx.crafterID,
                        inspectionResult.copy(resultConsumed = (inspectionResult.resultConsumed + ctx.coordinate.toIndex()).toMutableSet())
                    )
                }
                return@CMatterPredicateImpl result
                        && if (ctx.matter is Matter) ctx.matter.original.predicatesResult(ctx) else true
            }
        }

        override fun isValidMatter(): Result<Unit> {
            return if (this.candidate.isEmpty()) {
                Result.failure(IllegalStateException("'candidate' must contain correct materials at least one."))
            } else if (this.candidate.any { !it.isAir && !it.isItem }) {
                Result.failure(IllegalStateException("'candidate' not allowed to contain materials that are '!Material#isItem'."))
            } else if (this.amount < 1) {
                Result.failure(IllegalStateException("'amount' must be 1 or more."))
            } else if (this.candidate.all { it.isAir }) {
                Result.failure(IllegalStateException("GroupMatter#candidate must contain materials what are '!Material#isAir' 1 or more."))
            }else Result.success(Unit)
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
        val notGroupedSize: Int = (items.keys - this.groups.map { it.members }.flatten().toSet()).size
        return notGroupedSize + this.groups.sumOf { it.min }
    }

    override fun isValidRecipe(): Result<Unit> {
        if (this.type != CRecipeType.NORMAL) {
            return Result.failure(
                NotImplementedError("GroupRecipe is not implemented for `CRecipeType.NORMAL`."))
        } else if (this.items.isEmpty() || this.items.size > 36) {
            return Result.failure(IllegalStateException("'items' must contain 1 to 36 valid CMatters."))
        } else if (this.items.entries.minBy { (c, _) -> c.toIndex() }.value.candidate.any { it.isAir }) {
            return Result.failure(IllegalArgumentException("GroupRecipe must not contain Material.AIR at first coordinate."))
        } else if (this.items.values.any { it.isValidMatter().isFailure }) {
            val builder = StringBuilder()
            for ((c, matter) in this.items.entries) {
                val t: Throwable = matter.isValidMatter().exceptionOrNull()
                    ?: continue
                builder.append("[items] x: ${c.x}, y: ${c.y}, ${t.message} ${System.lineSeparator()}")
            }
            return Result.failure(IllegalStateException(builder.toString()))
        }

        Context.isValidGroups(this.groups, this.items).takeIf { it.isFailure }?.let { return it }
        return Result.success(Unit)
    }
}
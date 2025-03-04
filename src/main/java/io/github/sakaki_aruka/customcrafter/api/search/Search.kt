package io.github.sakaki_aruka.customcrafter.api.search

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.filter.CRecipeFilter
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.CraftView
import io.github.sakaki_aruka.customcrafter.api.`object`.AmorphousFilterCandidate
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelationComponent
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import java.util.UUID
import kotlin.math.abs

object Search {

    /**
     * A result of [Search.search].
     *
     * @param[vanilla] A found vanilla recipe.
     * @param[customs] Found custom recipes.
     *
     */
    class SearchResult internal constructor(
        private val vanilla: Recipe?,
        private val customs: List<Pair<CRecipe, MappedRelation>>,
    ) {
        /**
         * returns a nullable vanilla recipe.
         *
         * When below situations, [vanilla] is null.
         * - If [customs] is not empty and a search query's 'natural' is true.
         * - If an input is not matched all registered vanilla recipes.
         * @return[vanilla] PaperMCs Recipe ([Recipe]). This is NOT a [CRecipe].
         */
        fun vanilla() = this.vanilla

        /**
         * returns custom recipes.
         *
         */
        fun customs() = this.customs

        /**
         * converts to a json string from specified SearchResult
         *
         * @return[String] serialized SearchResult string
         * @since 5.0.8
         */
        fun toJson(): String {
            val vanillaHashcode: String = vanilla?.let { r -> (r as CraftingRecipe).key.toString() } ?: ""
            val customsList: MutableList<Pair<String, MappedRelation>> = mutableListOf()
            customs.forEach { (recipe, mapped) ->
                val recipeString = "${recipe.name}-${recipe.items.hashCode()}-${recipe.type.name}"
                customsList.add(recipeString to mapped)
            }
            return Json.encodeToString(vanillaHashcode to customsList)
        }

        /**
         * returns search result size that is sum of vanilla and customs.
         *
         * examples
         * ```
         * vanilla = null, customs.size = 3 -> 0 + 3 = 3
         * vanilla != null, customs.size = 0 -> 1 + 0 = 1
         * vanilla != null, customs.size = 1 -> 1 + 1 = 2
         * ```
         *
         * @return[Int] size of result
         * @since 5.0.8
         */
        fun size(): Int {
            val v: Int = if (this.vanilla != null) 1 else 0
            val c: Int = this.customs.size
            return v + c
        }

        companion object {
            /**
             * converts to a SearchResult instance from a specified json string
             *
             * @return[SearchResult] deserialized SearchResult instance
             * @since 5.0.8
             */
            fun fromJson(json: String): SearchResult {
                val pair: Pair<String, List<Pair<String, MappedRelation>>> = Json.decodeFromString(json)
                val vanilla: Recipe? = Bukkit.recipeIterator()
                    .asSequence()
                    .filter { r -> r is CraftingRecipe }
                    .firstOrNull { r -> (r as CraftingRecipe).key.toString() == pair.first }
                val list: List<Pair<String, MappedRelation>> = pair.second
                val customs: List<Pair<CRecipe, MappedRelation>> = list.map { (cRecipe, mapped) ->
                    val c: CRecipe = CustomCrafterAPI.getRecipes().first { r ->
                        "${r.name}-${r.items.hashCode()}-${r.type.name}" == cRecipe
                    }
                    c to mapped
                }
                return SearchResult(vanilla, customs)
            }
        }

        // when call Search#search with natural: Boolean
        // - true: when this finds matched custom recipes, does not search about vanilla.
        // - false: always search vanilla, but this does not mean 'vanilla' is non-null.

        // why 'customs' is List?
        //  -> Cause the search method does not know recipes class implements 'hashCode' and 'equals'.
        // why this class does not support 'hashCode' and 'equals'?
        //  -> There is same reason with the above q.
    }

    // one: Boolean

    /**
     * search main method.
     *
     * this is more recommended than [search] (use List<ItemStack>).
     *
     * @param[crafterID] a crafter's UUID
     * @param[view] input crafting gui's view
     * @param[natural] Force to search vanilla recipes or not.(true=not, false=force). The default is true.
     * @param[onlyFirst] get only first matched custom recipe and mapped. (default = false)
     * @return[SearchResult?] A result of a request. If you send one that contains invalid params, returns null.
     */
    fun search(
        crafterID: UUID,
        view: CraftView,
        natural: Boolean = true,
        onlyFirst: Boolean = false
    ): SearchResult? {
        val gui: Inventory = view.toCraftingGUI()
        return search(crafterID, gui, natural, onlyFirst)
    }

    /**
     * 6x6 crafting items.
     *
     * zero origin & do not skip empty slots (do padding = use ItemStack#empty())
     *
     *  A search-result is not guaranteed what is not empty.
     *
     * @param[player] A craft-request sender.
     * @param[items] Materials of crafting. this size must equal 36(6*6).
     * @param[natural] Force to search vanilla recipes or not.(true=not, false=force). The default is true.
     * @param[onlyFirst] get only first matched custom recipe and mapped. (default = false)
     * @return[SearchResult?] A result of a request. If you send one that contains invalid params, returns null.
     */
    fun search(
        player: Player,
        items: List<ItemStack>,
        natural: Boolean = true,
        onlyFirst: Boolean = false
    ): SearchResult? {
        if (items.size != 36) return null
        val inventory: Inventory = Bukkit.createInventory(null, 54)
        val chunkedInput: List<List<ItemStack>> = items.chunked(6)
        for (y: Int in (0..<6)) {
            for (x: Int in (0..<6)) {
                val index: Int = x + y * 9
                inventory.setItem(index, chunkedInput[y][x])
            }
        }

        return search(player.uniqueId, inventory, natural, onlyFirst)
    }

    internal fun search(
        crafterID: UUID,
        inventory: Inventory,
        natural: Boolean = true,
        onlyFirst: Boolean = false
    ): SearchResult? {
        if (!CustomCrafterAPI.isCustomCrafterGUI(inventory) || CustomCrafterAPI.isGUITooOld(inventory)) return null
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory)
            .takeIf { it?.isNotEmpty() == true } ?: return null

        val customs: List<Pair<CRecipe, MappedRelation>> = CustomCrafterAPI.getRecipes()
            .filter { it.items.size == mapped.size }
            .map { recipe ->
                val relation: MappedRelation? =
                    when (recipe.type) {
                        CRecipeType.NORMAL -> {
                            if (normal(mapped, recipe, crafterID)) {
                                val components: Set<MappedRelationComponent> =
                                    recipe.items.keys.zip(mapped.keys)
                                        .map { MappedRelationComponent(it.first, it.second) }
                                        .toSet()
                                if (onlyFirst) return SearchResult(null, listOf(recipe to MappedRelation(components)))
                                MappedRelation(components)
                            } else {
                                null
                            }
                        }
                        CRecipeType.AMORPHOUS -> amorphous(mapped, recipe, crafterID)
                    }
                Pair(recipe, relation)
            }
            .filter { p -> p.second != null }
            .map { Pair(it.first, it.second as MappedRelation) }

        val vanilla: Recipe? =
            if (natural && customs.isNotEmpty()) null
            else {
                val world: World = Bukkit.getPlayer(crafterID)
                    ?.world
                    ?: Bukkit.getWorlds().first()
                VanillaSearch.search(world, inventory)
            }

        return SearchResult(vanilla, customs)
    }

    private fun normal(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID,
        fromAmorphous: Boolean = false
    ): Boolean {
        val basic: Boolean =
            if (fromAmorphous) allCandidateContains(mapped, recipe)
            else {
                sameShape(mapped.keys, recipe)
                        && allCandidateContains(mapped, recipe)
            }

        val inputSorted: List<ItemStack> = coordinateSort(mapped)
        val recipeSorted: List<CMatter> = coordinateSort(recipe.items)

        for ((i, m) in inputSorted.zip(recipeSorted)) {
            if (!m.mass && m.amount != 1 && i.amount < m.amount) return false
            val inOne: ItemStack = i.asOne()
            val recipeOne: CMatter = m.asOne()

            if (!recipeOne.predicatesResult(inOne, mapped, recipe, crafterID)) return false
            recipe.filters?.let { set ->
                for (filter in set) {
                    try {
                        val (type, result) = applyNormalFilters(inOne, recipeOne, filter)
                        return when (type) {
                            CRecipeFilter.ResultType.NOT_REQUIRED -> continue
                            CRecipeFilter.ResultType.FAILED -> false
                            CRecipeFilter.ResultType.SUCCESS -> {
                                if (result) continue
                                else false
                            }
                        }
                    } catch (e: ClassCastException) {
                        continue
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return false
                    }
                }
            }
        }
        return basic
    }

    private inline fun <reified T: CMatter> applyNormalFilters(
        item: ItemStack,
        matter: T,
        filter: CRecipeFilter<T>
    ): Pair<CRecipeFilter.ResultType, Boolean> {
        return filter.normal(item, matter)
    }

    private fun candidateAmorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe
    ): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val recipes: List<CoordinateComponent> = recipe.items.keys.toList()
        val inputs: List<CoordinateComponent> = mapped.keys.toList()

        if (recipes.size > inputs.size) return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
        else if (recipes.isEmpty()) return Pair(AmorphousFilterCandidate.Type.NOT_REQUIRED, emptyList())

        val map: MutableMap<Int, List<Int>> = mutableMapOf()
        for (index: Int in recipes.indices) {
            val candidates: Set<Material> = recipe.items[recipes[index]]!!.candidate
            val matched: List<Int> = inputs
                .withIndex()
                .filter { (_, component) ->
                    val inType: Material = mapped[component]!!.type
                    candidates.contains(inType)
                }
                .map { (index, _) -> index }
            map[index] = matched
        }

        val result: MutableList<AmorphousFilterCandidate> = mutableListOf()
        for (slice in map.entries) {
            val R: CoordinateComponent = recipes[slice.key]
            val list: List<CoordinateComponent> = inputs
                .withIndex()
                .filter { slice.value.contains(it.index) }
                .map { it.value }
            if (list.isEmpty()) {
                return Pair(AmorphousFilterCandidate.Type.NOT_ENOUGH, emptyList())
            }
            result.add(AmorphousFilterCandidate(R, list))
        }

        val type =
            if (result.isEmpty()) AmorphousFilterCandidate.Type.NOT_ENOUGH
            else AmorphousFilterCandidate.Type.SUCCESSFUL

        return Pair(type, result)
    }

    private fun checkAmorphousCoordinates(
        mapped: Map<CoordinateComponent, ItemStack>,
        matter: CMatter,
        matterCoordinate: CoordinateComponent,
        filter: CRecipeFilter<CMatter>
    ): Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>> {
        val coordinateList: MutableSet<CoordinateComponent> = mutableSetOf()
        for ((c, item) in mapped) {
            try {
                val (type, result) = filter.normal(item, matter)
                if (type == CRecipeFilter.ResultType.SUCCESS && result) {
                    coordinateList.add(c)
                }
            } catch (e: Exception) {
                return AmorphousFilterCandidate.Type.NOT_ENOUGH to emptyList()
            }
        }

        return if (coordinateList.isEmpty()) {
            AmorphousFilterCandidate.Type.NOT_REQUIRED to emptyList()
        } else AmorphousFilterCandidate.Type.SUCCESSFUL to listOf(AmorphousFilterCandidate(matterCoordinate, coordinateList.toList()))
    }

    private fun amorphous(
        mapped: Map<CoordinateComponent, ItemStack>,
        recipe: CRecipe,
        crafterID: UUID
    ): MappedRelation? {
        val filterResults: MutableList<Pair<AmorphousFilterCandidate.Type, List<AmorphousFilterCandidate>>> = mutableListOf(
            candidateAmorphous(mapped, recipe)
        )

        recipe.items.forEach { (c, matter) ->
            recipe.filters?.forEach { filter ->
                val (type, list) = checkAmorphousCoordinates(mapped, matter, c, filter)
                if (type != AmorphousFilterCandidate.Type.NOT_REQUIRED) {
                    filterResults.add(type to list)
                }
            }
        }

        if (filterResults.any { pair -> pair.first == AmorphousFilterCandidate.Type.NOT_ENOUGH }) return null//return false

        val targets: Set<CoordinateComponent> = filterResults
            .map { e -> e.second.map { afc -> afc.coordinate } }
            .flatten()
            .toSet()

        val limits: MutableMap<CoordinateComponent, Int> = mutableMapOf()
        for (coordinate: CoordinateComponent in targets) {
            filterResults
                .filter { it.first != AmorphousFilterCandidate.Type.NOT_REQUIRED }
                .let { pairs ->
                    limits[coordinate] = pairs.count { p -> p.second.any { f -> f.coordinate == coordinate } }
                }
        }

        val filters: MutableSet<AmorphousFilterCandidate> = mutableSetOf()
        for (coordinate: CoordinateComponent in targets) {
            val list: MutableList<List<CoordinateComponent>> = mutableListOf()
            filterResults
                .filter { it.first == AmorphousFilterCandidate.Type.SUCCESSFUL }
                .map { it.second } // List<List<AFC>>
                .forEach { l -> // List<AFC>
                    l.filter { f -> f.coordinate == coordinate }
                        .takeIf { it.count() == limits[coordinate] }
                        ?.forEach { f -> list.add(f.list) }
                        ?: return null//return false
                }
            val merged: MutableList<CoordinateComponent> = mutableListOf()
            list.forEach { e -> merged.addAll(e) }
            filters.add(AmorphousFilterCandidate(coordinate, merged))
        }

        val confirmed: MutableMap<CoordinateComponent, CoordinateComponent> = mutableMapOf()
        val removeMarked: MutableSet<CoordinateComponent> = mutableSetOf()
        filters.filter { f -> f.list.size == 1 }
            .forEach { f ->
                confirmed[f.coordinate] = f.list.first()
                removeMarked.add(f.coordinate)
            }
        removeMarked.forEach { r -> filters.removeIf { f -> f.coordinate == r } }

        // CSP solver here
        //confirmed.putAll(csp(filters).takeIf { it.isNotEmpty() } ?: return null)//return false)
        // type =List<MappedRelation>?
        return solveCSP(filters, mapped, recipe, crafterID)
            .firstOrNull()
            ?.let { r ->
                val components: MutableSet<MappedRelationComponent> = mutableSetOf()
                confirmed.forEach { (recipe, input) ->
                    components.add(MappedRelationComponent(recipe, input))
                }
                r.components.forEach { c -> components.add(c) }
                MappedRelation(components)
            }
        //return recipe.items.size == confirmed.size
    }

    private fun solveCSP(
        candidates: Set<AmorphousFilterCandidate>,
        mapped: Map<CoordinateComponent, ItemStack>,
        cRecipe: CRecipe,
        crafterID: UUID,
        getFull: Boolean = false
    ): List<MappedRelation> {
        val solutions = mutableListOf<Map<CoordinateComponent, CoordinateComponent>>()
        val currentSolution = mutableMapOf<CoordinateComponent, CoordinateComponent>()

        fun backtrack(index: Int, candidateList: List<AmorphousFilterCandidate>) {
            if (index == candidateList.size) {
                // 全ての候補を割り当てた
                solutions.add(currentSolution.toMap())
                return
            }

            val current = candidateList[index]
            for (value in current.list) {
                // 現在の解に値が既に使われていないかチェック
                if (!currentSolution.values.contains(value)) {
                    currentSolution[current.coordinate] = value

                    // 次の候補に進む
                    backtrack(index + 1, candidateList)

                    // 戻り操作
                    currentSolution.remove(current.coordinate)
                }
            }
        }

        backtrack(0, candidates.toList())

        if (getFull) {
            return solutions.filter { solution ->
                val mappedRelation: MappedRelation = mapToMappedRelation(solution)
                val (temporaryRecipe, temporaryInventory) = replaced(mapped, cRecipe, mappedRelation)
                val temporaryMapped: Map<CoordinateComponent, ItemStack> = temporaryMapped(temporaryInventory)
                normal(temporaryMapped, temporaryRecipe, crafterID, fromAmorphous = true)
            }.map { s -> mapToMappedRelation(s) }
        } else {
            val candidate: Map<CoordinateComponent, CoordinateComponent>? =
                solutions
                    .asSequence()
                    .take(1)
                    .firstOrNull { solution ->
                        val mappedRelation: MappedRelation = mapToMappedRelation(solution)
                        val (temporaryRecipe, temporaryInventory) = replaced(mapped, cRecipe, mappedRelation)
                        val temporaryMapped: Map<CoordinateComponent, ItemStack> = temporaryMapped(temporaryInventory)
                        normal(temporaryMapped, temporaryRecipe, crafterID, fromAmorphous = true)
                    }

            return candidate?.let { listOf(mapToMappedRelation(it)) } ?: emptyList()
        }
    }

    private fun mapToMappedRelation(
        map: Map<CoordinateComponent, CoordinateComponent>
    ): MappedRelation {
        return MappedRelation(
            map.entries
                .map { pair -> MappedRelationComponent(pair.key, pair.value) }
                .toSet()
        )
    }

    private fun replaced(
        mapped: Map<CoordinateComponent, ItemStack>,
        cRecipe: CRecipe,
        mappedRelation: MappedRelation
    ): Pair<CRecipe, Inventory> {
        val newItems: MutableMap<CoordinateComponent, CMatter> = mutableMapOf()
        val temporaryInventory: Inventory = Bukkit.createInventory(null, 54)
        mappedRelation.components.forEach { component ->
            val matter: CMatter = cRecipe.items[component.recipe]!!
            newItems[component.input] = matter
            val c: CoordinateComponent = component.input
            val index: Int = c.x + c.y * 9
            temporaryInventory.setItem(index, mapped[c])
        }
        return cRecipe.replaceItems(newItems) to temporaryInventory
    }

    private fun temporaryMapped(
        temporaryInventory: Inventory
    ): MutableMap<CoordinateComponent, ItemStack> {
        val temporaryMapped: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        Converter.getAvailableCraftingSlotComponents().filter { i ->
            val item: ItemStack? = temporaryInventory.getItem(i.toIndex())
            item != null && item.type != Material.AIR
        }.forEach { c ->
            temporaryMapped[c] = temporaryInventory.getItem(c.toIndex())!!
        }
        return temporaryMapped
    }

    private fun allCandidateContains(mapped: Map<CoordinateComponent, ItemStack>, recipe: CRecipe): Boolean {
        val inputCoordinateSorted: List<CoordinateComponent> = coordinateSort(mapped.keys)
        val recipeCoordinateSorted: List<CoordinateComponent> = coordinateSort(recipe.items.keys)
        for ((ic, rc) in inputCoordinateSorted.zip(recipeCoordinateSorted)) {
            if (!recipe.items[rc]!!.candidate.contains(mapped[ic]!!.type)) return false
        }
        return true
    }

    private fun sameShape(mapped: Set<CoordinateComponent>, recipe: CRecipe): Boolean {
        if (recipe.type == CRecipeType.AMORPHOUS || mapped.size != recipe.items.size) return false
        val r: List<CoordinateComponent> = coordinateSort(recipe.items.keys)
        val i: List<CoordinateComponent> = coordinateSort(mapped)
        val set: MutableSet<Pair<Int, Int>> = mutableSetOf()
        for ((inputCoordinate, recipeCoordinate) in i.zip(r)) {
            set.add(Pair(abs(inputCoordinate.x - recipeCoordinate.x), abs(inputCoordinate.y - recipeCoordinate.y)))
        }
        return set.size == 1
    }

    private fun <T> coordinateSort(coordinates: Map<CoordinateComponent, T>): List<T> {
        val result: MutableList<T> = mutableListOf()
        for (c in coordinateSort(coordinates.keys)) {
            coordinates[c]?.let { result.add(it) }
        }
        return result
    }

    private fun coordinateSort(coordinates: Collection<CoordinateComponent>): List<CoordinateComponent> {
        val result: MutableList<CoordinateComponent> = mutableListOf()
        coordinates.groupBy { it.y }
            .let {
                for (y in it.keys.sorted()) {
                    it[y]?.let { list -> list.sortedBy { c -> c.x }
                        .forEach { c -> result.add(c) }
                    }
                }
            }
        return result
    }
}
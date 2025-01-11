package io.github.sakaki_aruka.customcrafter.api.processor

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toByteArray
import io.github.sakaki_aruka.customcrafter.api.search.Search
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import kotlin.math.min

/**
 * @suppress
 * @since 5.0.8
 */
object InventoryModifier {

    internal val INPUT_INVENTORY_KEY = NamespacedKey(CustomCrafter.getInstance(), "input_inventory")
    internal val INPUT_INVENTORY_TYPE = PersistentDataType.BYTE_ARRAY

    internal val SEARCH_RESULT_KEY = NamespacedKey(CustomCrafter.getInstance(), "search_result")
    internal val SEARCH_RESULT_TYPE = PersistentDataType.BYTE_ARRAY // Search#SearchResult#toByteArray

    private val VANILLA_RESULT_KEY = NamespacedKey(CustomCrafter.getInstance(), "vanilla_result")
    private val VANILLA_RESULT_TYPE = PersistentDataType.BYTE_ARRAY

    internal val RESULT_INDEX_KEY = NamespacedKey(CustomCrafter.getInstance(), "result_index")
    internal val RESULT_INDEX_TYPE = PersistentDataType.INTEGER


    private const val PREVIOUS_PAGE_INDEX = 45
    internal const val CURRENT_PAGE_INDEX = 49
    private const val NEXT_PAGE_INDEX = 53

    private const val PAGE_SLOTS_SIZE = 54
    private const val ONE_PAGE_RESULT_LIMIT = 45
    private const val MULTIPLE_RESULT_PAGE_TITLE = "<black>Custom Crafter Multiple Result Page"

    private fun pageBase(): Inventory {
        return Bukkit.createInventory(
            null,
            PAGE_SLOTS_SIZE,
            MiniMessage.miniMessage().deserialize(MULTIPLE_RESULT_PAGE_TITLE)
        )
    }

    private fun nextPage(): ItemStack {
        return ItemStack(Material.AMETHYST_CLUSTER).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Next Page"))
            }
        }
    }

    private fun previousPage(): ItemStack {
        return ItemStack(Material.AMETHYST_SHARD).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Previous Page"))
            }
        }
    }

    internal fun currentPage(index: Int): ItemStack {
        return ItemStack(Material.GRASS_BLOCK).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Page $index"))
            }
        }
    }

    internal fun getBlankSlots(): Set<Int> {
        return setOf(46, 47, 48, CURRENT_PAGE_INDEX, 50, 51, 52) // CURRENT_PAGE_INDEX does not have any processes
    }


    internal fun displayedPageButtonClick(
        gui: Inventory,
        slot: Int,
        //container: PersistentDataContainer,
        playerID: UUID
    ): Inventory {
        //TODO: impl this
        return when (slot) {
            PREVIOUS_PAGE_INDEX ->  getMultipleResultGUI(gui, -1, playerID)

            NEXT_PAGE_INDEX -> getMultipleResultGUI(gui, 1, playerID)

            else -> throw IllegalArgumentException("'slot' must be ${PREVIOUS_PAGE_INDEX} or ${NEXT_PAGE_INDEX}.")
        }
    }

    internal fun getMultipleResultGUI(
        current: Inventory,
        diff: Int, // -1 = previous, 1 = next
        playerID: UUID
    ): Inventory {
        if (!CustomCrafterAPI.isCustomCrafterMultipleResultGUI(current)) {
            throw IllegalArgumentException("'current' must be Custom Crafter Multiple Result gui.")
        }

        val currentPageItem: ItemStack = current.getItem(CURRENT_PAGE_INDEX)
            ?: throw NoSuchElementException("'current' has no item in the current page info item's slot.")

        val container: PersistentDataContainer = currentPageItem.itemMeta.persistentDataContainer

        val searchResult: Search.SearchResult = Search.SearchResult.fromByteArray(
            currentPageItem.itemMeta.persistentDataContainer.get(
                SEARCH_RESULT_KEY, SEARCH_RESULT_TYPE
            ) ?: throw NoSuchElementException("'searchResult' not found from the provided inventory item.")
        )

        val startIndex: Int =
            if (diff == 1 ) getIndex(current, getMinimum = false) + 1
            else (getIndex(current, getMinimum = true) - ONE_PAGE_RESULT_LIMIT)
                .takeIf { i -> i >= 0 }
                ?: throw IllegalArgumentException("minus 'diff' does not match this page.")

        val displayItems: Map<Int, ItemStack> = getDisplayItems(
            container,
            startIndex,
            displayVanilla = (startIndex == 0 && searchResult.vanilla() != null),
            crafterID = playerID,
            displayPreviousButton = startIndex > ONE_PAGE_RESULT_LIMIT,
            displayNextButton = (startIndex + ONE_PAGE_RESULT_LIMIT) < searchResult.size()
        )

        val result: Inventory = pageBase()
        displayItems.forEach { (index, item) -> result.setItem(index, item) }
        result.setItem(CURRENT_PAGE_INDEX, currentPageItem)
        return result
    }

    /**
     * returns first page of multiple results pages if the provided inventory is Custom Crafter GUI.
     *
     * if 'current' is not a Custom Crafter GUI, this returns 'current'.
     *
     * @param[current] an input inventory. this must be Custom Crafter GUI.
     * @param[result] a result of search.
     * @throws[IllegalArgumentException] this is thrown if 'current' is not a Custom Crafter GUI.
     * @return[Inventory] a first page of multiple result display inventories
     *
     * @since 5.0.8
     */
    internal fun getFirstMultipleResultGUI(
        current: Inventory,
        result: Search.SearchResult,
        //clickEvents: Map<Int, (Player, CRecipe, Boolean) -> Unit>,
        playerID: UUID
    // Int -> slot, Player -> clicked player, CRecipe -> a target recipe, Boolean -> shift click
    ): Inventory {
        val resultsAmount: Int = result.customs().size + (if (result.vanilla() != null) 1 else 0)
        if (!CustomCrafterAPI.isCustomCrafterGUI(current)) {
            throw IllegalArgumentException("'current' must be Custom Crafter's gui.")
        } else if (resultsAmount < 2 ) {
            return current
        }

        // can display 45 items on a page.
        val inputByteArray: ByteArray = current.toByteArray()
        val vanillaResultByteArray: ByteArray? = result.vanilla()?.result?.serializeAsBytes()
        val currentPageDisplayItem: ItemStack = currentPage(0)

        currentPageDisplayItem.apply {
            itemMeta = itemMeta.apply {
                persistentDataContainer.set(
                    INPUT_INVENTORY_KEY,
                    INPUT_INVENTORY_TYPE,
                    inputByteArray)

                persistentDataContainer.set(
                    SEARCH_RESULT_KEY,
                    SEARCH_RESULT_TYPE,
                    result.toByteArray()
                )

                persistentDataContainer.set(
                    VANILLA_RESULT_KEY,
                    VANILLA_RESULT_TYPE,
                    vanillaResultByteArray ?: ByteArray(0)
                )

                val (key, type, epochTime) = CustomCrafterAPI.genCCKey()
                persistentDataContainer.set(key, type, epochTime)
            }

        }

        val page: Inventory = pageBase()

        page.setItem(CURRENT_PAGE_INDEX, currentPageDisplayItem)
        if (resultsAmount > ONE_PAGE_RESULT_LIMIT) {
            page.setItem(NEXT_PAGE_INDEX, nextPage())
        }

        getDisplayItems(
            currentPageDisplayItem.itemMeta.persistentDataContainer,
            startIndex = 0,
            displayVanilla = result.vanilla() != null,
            crafterID = playerID,
            displayPreviousButton = false,
            displayNextButton = resultsAmount > ONE_PAGE_RESULT_LIMIT
        ).forEach { (index, item) ->
            page.setItem(index, item)
        }

        return page
    }

    private val NO_DISPLAYABLE_RESULT_ITEM_TYPE = PersistentDataType.BOOLEAN
    private fun noDisplayableResultItem(
        recipeName: String
    ): Pair<NamespacedKey, ItemStack> {
        val key = NamespacedKey(CustomCrafter.getInstance(), "no_displayable_result_item")
        val type = PersistentDataType.BOOLEAN
        val item = ItemStack(Material.BARRIER).apply {
            itemMeta = itemMeta.apply {
                displayName(MiniMessage.miniMessage().deserialize("<white>${recipeName} <b><red>NO RESULT ITEM"))
                persistentDataContainer.set(key, type, true) // marker
            }
        }
        return Pair(key, item)
    }

    internal fun getIndex(
        gui: Inventory,
        getMinimum: Boolean
    ): Int {
        val candidate: MutableSet<Int> = mutableSetOf()
        gui.contents
            .filterNotNull()
            .filter { it.type != Material.AIR }
            .forEach { item ->
                item.itemMeta.persistentDataContainer.get(
                    RESULT_INDEX_KEY,
                    RESULT_INDEX_TYPE
                )?.let { v -> candidate.add(v) }
            }

        if (candidate.isEmpty()) throw IllegalArgumentException("'gui' does not have indexed display result.")
        return if (getMinimum) candidate.min() else candidate.max()
    }

    private fun getDisplayItems(
        container: PersistentDataContainer,
        startIndex: Int, // from
        displayVanilla: Boolean,
        crafterID: UUID,
        displayPreviousButton: Boolean,
        displayNextButton: Boolean
    ): Map<Int, ItemStack> {
        /*
         *
         * this requires multiple Search.SearchResult
         *
         * 0. get various values from container ByteArray (e.g. SearchResult)
         * 1. calc display items size
         * 2. get display items
         * 3. mapping items
         * 4. return items
         */

        if (startIndex < 0) throw IllegalArgumentException("'startIndex' must be a positive integer.")

        val searchResult: Search.SearchResult = Search.SearchResult.fromByteArray(
            container.get(SEARCH_RESULT_KEY, SEARCH_RESULT_TYPE)
                ?: throw IllegalArgumentException("'container' does not have a search result's binary array.")
        )

        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(
            Converter.inventoryFromByteArray(
                container.get(INPUT_INVENTORY_KEY, INPUT_INVENTORY_TYPE)
                    ?: throw NoSuchElementException("'container' does not have an input inventory's binary array.")
            )
        ) ?: throw IllegalStateException("'container' has an illegal formatted inventory's binary array.")

        if (searchResult.size() < startIndex) return emptyMap()

        val vanillaResult: ItemStack? =
            if (searchResult.vanilla() != null && displayVanilla && startIndex == 0) { // vanilla result must be placed in index 0 slot.
                searchResult.vanilla()?.let { recipe ->
                    val modified = ItemStack(recipe.result)
                    modified.apply {
                        itemMeta = itemMeta.apply {
                            lore(listOf(MiniMessage.miniMessage().deserialize("<aqua><u><b>Vanilla Recipe Result")))
                        }
                    }
                }
            } else null

        val customsLimit: Int = ONE_PAGE_RESULT_LIMIT - (if (vanillaResult != null) 1 else 0)
        val end: Int = min(startIndex + customsLimit, searchResult.size()) // exclude
        val items: MutableMap<Int, ItemStack> = mutableMapOf()

        vanillaResult?.let { v -> items[0] = v }

        for (index in startIndex..<end) {
            val (recipe, relation) = searchResult.getCustomResult(index)
            val item: ItemStack = recipe.getResults(
                crafterID, relation, mapped,
                shiftClicked = false,
                calledTimes = 1,
                preDisplaying = true
            ).getOrElse(0) { _ -> noDisplayableResultItem(recipe.name).second }
                .let { item ->
                    item.apply {
                        itemMeta = itemMeta.apply {
                            persistentDataContainer.set(
                                RESULT_INDEX_KEY,
                                RESULT_INDEX_TYPE,
                                index
                            )
                        }
                    }
                }
            items[index] = item
        }

        if (displayPreviousButton) items[PREVIOUS_PAGE_INDEX] = previousPage()
        if (displayNextButton) items[NEXT_PAGE_INDEX] = nextPage()

        return items
    }
}
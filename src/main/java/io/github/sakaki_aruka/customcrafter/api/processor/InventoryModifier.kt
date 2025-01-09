package io.github.sakaki_aruka.customcrafter.api.processor

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.listener.InventoryClickListener
import io.github.sakaki_aruka.customcrafter.api.`object`.CraftView
import io.github.sakaki_aruka.customcrafter.api.processor.Converter.toByteArray
import io.github.sakaki_aruka.customcrafter.api.search.Search
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * @since 5.0.8
 */
object InventoryModifier {

    private val INPUT_INVENTORY_KEY = NamespacedKey(CustomCrafter.getInstance(), "input_inventory")
    private val INPUT_INVENTORY_TYPE = PersistentDataType.BYTE_ARRAY

    private val SEARCH_RESULT_KEY = NamespacedKey(CustomCrafter.getInstance(), "search_result")
    private val SEARCH_RESULT_TYPE = PersistentDataType.BYTE_ARRAY // Search#SearchResult#toByteArray

    private val VANILLA_RESULT_KEY = NamespacedKey(CustomCrafter.getInstance(), "vanilla_result")
    private val VANILLA_RESULT_TYPE = PersistentDataType.BYTE_ARRAY

    private val RESULT_INDEX_KEY = NamespacedKey(CustomCrafter.getInstance(), "result_index")
    private val RESULT_INDEX_TYPE = PersistentDataType.INTEGER

    private val CURRENT_PAGE_INDEX_KEY = NamespacedKey(CustomCrafter.getInstance(), "current_page_index")
    private val CURRENT_PAGE_INDEX_TYPE = PersistentDataType.INTEGER

    private fun pageCacheContainer(
        currentPage: Int,
        nextPage: Boolean // true = get a next page info, false = get a previous page info
    ): Pair<NamespacedKey, PersistentDataType<*, *>> {
        val targetPage: Int = currentPage + (if (nextPage) 1 else -1)
        if (targetPage < 0) {
            throw IllegalArgumentException("'currentPage' must be greater than zero.")
        }
        return NamespacedKey(CustomCrafter.getInstance(), "page_cache_${targetPage}") to PersistentDataType.BYTE_ARRAY
    }

    private const val PREVIOUS_PAGE_INDEX = 45
    private const val CURRENT_PAGE_INDEX = 49
    private const val NEXT_PAGE_INDEX = 53

    private const val PAGE_SLOTS_SIZE = 54
    private const val ONE_PAGE_RESULT_LIMIT = 45
    private const val MULTIPLE_RESULT_PAGE_TITLE = "<black>Custom Crafter Multiple Result Page"

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

    private fun currentPage(index: Int): ItemStack {
        return ItemStack(Material.GRASS_BLOCK).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Page $index"))
            }
        }
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
    fun getFirstMultipleResultGui(
        current: Inventory,
        result: Search.SearchResult,
        clickEvents: Map<Int, (Player, CRecipe, Boolean) -> Unit>
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

        currentPageDisplayItem.itemMeta.apply {
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

            persistentDataContainer.set(
                CURRENT_PAGE_INDEX_KEY,
                CURRENT_PAGE_INDEX_TYPE,
                0
            )
        }

        val page: Inventory = Bukkit.createInventory(
            null,
            PAGE_SLOTS_SIZE,
            MiniMessage.miniMessage().deserialize(MULTIPLE_RESULT_PAGE_TITLE)
        )

        page.setItem(CURRENT_PAGE_INDEX, currentPageDisplayItem)
        if (resultsAmount > ONE_PAGE_RESULT_LIMIT) {
            page.setItem(NEXT_PAGE_INDEX, nextPage())
        }


    }

    private val DEFAULT_CLICK_LAMBDA: (Player, CRecipe, Inventory, Boolean) -> Unit = { player, recipe, input, shift ->
        InventoryClickListener.process()
    }

    private fun noDisplayableResultItem(
        recipeName: String
    ): Triple<NamespacedKey, PersistentDataType<*, *>, ItemStack> {
        val key = NamespacedKey(CustomCrafter.getInstance(), "no_displayable_result_item")
        val type = PersistentDataType.BOOLEAN
        val item = ItemStack(Material.BARRIER).apply {
            itemMeta = itemMeta.apply {
                displayName(MiniMessage.miniMessage().deserialize("<white>${recipeName} <b><red>NO RESULT ITEM"))
                persistentDataContainer.set(key, type, true) // marker
            }
        }
        return Triple(key, type, item)
    }

    private fun getDisplayItems(
        // refer 'currentPage' only here
        page: Inventory
    ): List<ItemStack> {
        val tagItem: ItemStack = page.getItem(CURRENT_PAGE_INDEX)!!
        val searchResult: Search.SearchResult = Search.SearchResult.fromByteArray(
            tagItem.itemMeta.persistentDataContainer.get(SEARCH_RESULT_KEY, SEARCH_RESULT_TYPE
            ) ?: run {
                throw IllegalStateException("the current page item does not have a container what contains recipe's hashcode.")
        })
        val result: MutableList<ItemStack> = mutableListOf()
        //
    }

    fun getNextMultipleResultGui(
        current: Inventory
    ) {
        //
    }

    private fun deserializeCraftView(
        current: Inventory
    ): CraftView {
        //
    }
}
package io.github.sakaki_aruka.customcrafter.api.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.event.PreCreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.CraftView
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter
import io.github.sakaki_aruka.customcrafter.api.processor.InventoryModifier
import io.github.sakaki_aruka.customcrafter.api.search.Search
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataContainer
import kotlin.math.max

/**
 * @suppress
 */
object InventoryClickListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClick() {
        val player: Player = Bukkit.getPlayer(whoClicked.uniqueId) ?: return
        val gui: Inventory = clickedInventory?.takeIf {
            CustomCrafterAPI.isCustomCrafterGUI(it)
        } ?: run {
            clickedInventory?.let { inv ->
                if (inv is PlayerInventory) {
                    val topInventory: Inventory = player.openInventory.topInventory
                    if (CustomCrafterAPI.isCustomCrafterGUI(topInventory)
                        || CustomCrafterAPI.isCustomCrafterMultipleResultGUI(topInventory)) {
                        if (click == ClickType.LEFT
                            || click == ClickType.RIGHT
                            || click == ClickType.SHIFT_LEFT
                        ) return
                        isCancelled = true
                        return
                    }
                }
            }
            return
        }

        if (CustomCrafterAPI.isGUITooOld(gui)) {
            isCancelled = true
            player.closeInventory()
            return
        }

        if (CustomCrafterAPI.isCustomCrafterGUI(gui)) {
            craftingGUI(this, gui, player)
        } else if (CustomCrafterAPI.isCustomCrafterMultipleResultGUI(gui)) {
            multipleGUI(this, gui, player)
        }
    }

    private fun craftingGUI(
        event: InventoryClickEvent,
        gui: Inventory,
        player: Player
    ) {
        if (event.isCancelled) return
        else if (event.rawSlot == CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT) {
            // click result slot
            event.isCancelled = true
            gui.getItem(event.rawSlot)?.let { item ->
                player.inventory.addItem(item).forEach { (_, over) ->
                    player.world.dropItem(player.location, over)
                }
            }
            return
        } else if (event.rawSlot == CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT) {
            // click make button
            event.isCancelled = true
            if (gui.contents.isEmpty()) return

            val view: CraftView = CraftView.fromInventory(gui) ?: return
            val preEvent = PreCreateCustomItemEvent(player, view, event.click)
            Bukkit.getPluginManager().callEvent(preEvent)
            if (preEvent.isCancelled) return

            val result: Search.SearchResult = Search.search(player.uniqueId, gui) ?: return

            CreateCustomItemEvent(player, view, result, event.click).callEvent()
            if (CustomCrafterAPI.RESULT_GIVE_CANCEL) return

            if (result.customs().isEmpty() && result.vanilla() == null) return

            val mass: Boolean = event.click == ClickType.SHIFT_LEFT

            process(result, gui, mass, player)
        } else if (!Converter.getAvailableCraftingSlotIndices().contains(event.rawSlot)) {
            // click a blank slot
            event.isCancelled = true
            return
        }
    }

    private fun multipleGUI(
        event: InventoryClickEvent,
        gui: Inventory,
        player: Player
    ) {
        if (InventoryModifier.getBlankSlots().contains(event.rawSlot)) return
        val clicked: ItemStack = gui.getItem(event.rawSlot)
            ?.takeIf { it.type != Material.AIR }
            ?: return
        val currentPageInfoItem: ItemStack = gui.getItem(InventoryModifier.CURRENT_PAGE_INDEX)
            ?: throw IllegalStateException("'gui' must have page info item, but not found.")

        val container: PersistentDataContainer = currentPageInfoItem.itemMeta.persistentDataContainer

        when (event.rawSlot) {
            in 0..<45 -> run {
                // click result (not air)
                // need to decrement and re-calculating results.
                val result: Search.SearchResult = Search.SearchResult.fromByteArray(
                    container.get(
                        InventoryModifier.SEARCH_RESULT_KEY,
                        InventoryModifier.SEARCH_RESULT_TYPE
                    ) ?: throw NoSuchElementException("'gui' does not have searchResult byte array.")
                )

                val resultIndex: Int = clicked.itemMeta.persistentDataContainer
                    .get(
                        InventoryModifier.RESULT_INDEX_KEY,
                        InventoryModifier.RESULT_INDEX_TYPE
                    ) ?: throw NoSuchElementException("clicked item does not have 'resultIndex' container.")
                val pair: Pair<CRecipe, MappedRelation> = result.getCustomResult(resultIndex)

                val inputInventory: Inventory = Converter.inventoryFromByteArray(
                    container.get(
                        InventoryModifier.INPUT_INVENTORY_KEY,
                        InventoryModifier.INPUT_INVENTORY_TYPE
                    ) ?: throw NoSuchElementException("'gui' does not have the input inventory byte array.")
                )

                //1つでも空になったらクラフト画面に戻る
                //空にならなかった場合は、その時のインベントリで再度検索をかけてその結果を表示する(複数じゃなくても複数表示する)

                fun getPlacedSlots(inventory: Inventory): Set<Int> {
                    return inventory.contents
                        .withIndex()
                        .filter { (_, item) -> item != null && item.type != Material.AIR }
                        .map { (slot, _) -> slot }
                        .toSet()
                }

                val placedSlotsBefore: Set<Int> = getPlacedSlots(inputInventory)
                val newSearchResult = Search.SearchResult(null, listOf(pair))
                process(newSearchResult, gui,event.click == ClickType.SHIFT_LEFT, player)
                val placedSlotsAfter: Set<Int> = getPlacedSlots(inputInventory)

                if (placedSlotsBefore.size != placedSlotsAfter.size) {
                    player.openInventory(inputInventory)
                    return
                }

                val reSearchResult: Search.SearchResult = Search.search(
                    crafterID = player.uniqueId,
                    inventory = inputInventory
                ) ?: run {
                    player.openInventory(inputInventory)
                    return
                }

                player.openInventory(
                    InventoryModifier.getFirstMultipleResultGUI(
                        inputInventory,
                        reSearchResult,
                        player.uniqueId
                    ))
            }

            in InventoryModifier.getBlankSlots() -> return

            else -> run {
                // click page change button (not air)
                val newPage: Inventory = InventoryModifier.displayedPageButtonClick(
                    gui,
                    event.rawSlot,
                    player.uniqueId
                )//container, player.uniqueId)

                player.openInventory(newPage)
            }
        }
    }

    private fun process(
        result: Search.SearchResult,
        gui: Inventory,
        mass: Boolean,
        player: Player,
        forceMultiple: Boolean = result.customs().size > 1
    ) {
        if (result.customs().isEmpty() && result.vanilla() == null) return
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(gui) ?: return

        if (result.customs().isNotEmpty()) {
            if (CustomCrafterAPI.IS_ENABLE_MULTIPLE_RESULT && forceMultiple) {
                // multiple result display
                val page: Inventory = InventoryModifier.getFirstMultipleResultGUI(
                    gui, result, player.uniqueId
                )

                player.openInventory(page)
                return

            } else {
                // normal (single) result display
                // custom recipe
                val (recipe, relate) = result.customs().first()
                if (getMinAmountWithoutMass(relate, recipe, mapped) <= 0) return
                var amount = 1
                recipe.items.forEach { (c, m) ->
                    val inputCoordinate = relate.components.find { it.recipe == c }?.input ?: return
                    val min: Int = getMinAmountWithoutMass(relate, recipe, mapped)
                    amount = if (m.mass) 1 else (m.amount * (if (mass) min else 1))

                    val slot: Int = inputCoordinate.x + inputCoordinate.y * 9
                    decrement(gui, slot, amount)
                }

                recipe.getResults(player.uniqueId, relate, mapped, mass, amount)
                    .takeIf { it.isNotEmpty() }
                    ?.let { itemList ->
                        recipe.runContainers(player.uniqueId, relate, mapped, itemList)
                        // TODO must impl add result items to the result slot
                        itemList.takeIf { it.isNotEmpty() }
                            ?.forEach { item ->
                                player.inventory.addItem(item).forEach { (_, over) ->
                                    player.world.dropItem(player.location, over)
                                }
                            }
                    }

            }


        } else if (result.vanilla() != null) {
            val min: Int = mapped.values.minOf { it.amount }
            val amount: Int = if (mass) min else 1
            Converter.getAvailableCraftingSlotIndices()
                .filter { s -> gui.getItem(s)?.takeIf { it.type != Material.AIR } != null}
                .forEach { s -> decrement(gui, s, amount) }
            val item: ItemStack = result.vanilla()!!.result.asQuantity(amount)

            // TODO must impl add result items to the result slot
            player.inventory.addItem(item).forEach { (_, over) ->
                player.world.dropItem(player.location, over)
            }
        }
    }

    private fun getMinAmountWithoutMass(relation: MappedRelation, recipe: CRecipe, mapped: Map<CoordinateComponent, ItemStack>): Int {
        return relation.components
            .filter { c -> Converter.getAvailableCraftingSlotComponents().contains(c.input) }
            .filter { c -> mapped[c.input] != null && mapped[c.input]!!.type != Material.AIR }
            .filter { c -> !recipe.items[c.recipe]!!.mass }
            .minOf { (r, i) -> mapped[i]!!.amount / recipe.items[r]!!.amount }
    }

    private fun decrement(gui: Inventory, slot: Int, amount: Int) {
        gui.getItem(slot)?.let { item ->
            item.amount = max(item.amount - amount, 0)
        }
    }
}
package io.github.sakaki_aruka.customcrafter.api.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.event.PreCreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.CraftView
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter
import io.github.sakaki_aruka.customcrafter.api.search.Search
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType
import kotlin.math.max
import kotlin.math.min

/**
 * @suppress
 */
object InventoryClickListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClick() {
        val player: Player = Bukkit.getPlayer(whoClicked.uniqueId) ?: return
        val topInv: Inventory = player.openInventory.topInventory
        val bottomInv: Inventory = player.openInventory.bottomInventory
        val clicked: Inventory = this.clickedInventory ?: return

        if (CustomCrafterAPI.isCustomCrafterGUI(clicked)) {
            // crafting clicked
            crafting(player, clicked, this)
        } else if (CustomCrafterAPI.isAllCandidatesPageGUI(clicked)) {
            // all clicked
            allCandidatesClickedProcess(player, clicked, this)
        } else {
            if (clicked is PlayerInventory && clicked == bottomInv) {
                if (CustomCrafterAPI.isCustomCrafterGUI(topInv)) {
                    // move materials to crafting gui (only allow 'collect' or 'place')
                    if (click !in setOf(
                            ClickType.LEFT,
                            ClickType.SHIFT_LEFT,
                            ClickType.RIGHT,
                            ClickType.DOUBLE_CLICK)
                        ) {
                        isCancelled = true
                    }
                } else if (CustomCrafterAPI.isAllCandidatesPageGUI(topInv)) {
                    // to swap? (ignore all)
                    this.isCancelled = true
                } else {
                    // not CustomCrafter's event
                    return
                }
            }
        }
    }

    private fun crafting(
        player: Player,
        gui: Inventory,
        event: InventoryClickEvent
    ) {
        // TODO no drop marker place
        if (CustomCrafterAPI.isGUITooOld(gui)) {
            event.isCancelled = true
            player.closeInventory()
            return
        } else if (event.isCancelled) return
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

            val result: Search.SearchResult = Search.search(
                player.uniqueId,
                gui,
                natural = !CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE
            ) ?: return

            CreateCustomItemEvent(player, view, result, event.click).callEvent()
            if (CustomCrafterAPI.RESULT_GIVE_CANCEL) return

            if (result.customs().isEmpty() && result.vanilla() == null) return

            val mass: Boolean = event.click == ClickType.SHIFT_LEFT

            if (CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE && result.size() > 1) {
                allCandidatesProcess(result, gui, player)
            } else {
                process(result, gui, mass, player)
            }
        } else if (!Converter.getAvailableCraftingSlotIndices().contains(event.rawSlot)) {
            // click a blank slot
            event.isCancelled = true
            return
        }
    }

    private fun process(
        result: Search.SearchResult,
        gui: Inventory,
        mass: Boolean,
        player: Player
    ) {
        if (result.customs().isEmpty() && result.vanilla() == null) return
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(gui) ?: return

        gui.getItem(CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT)?.let { button ->
            if (result.size() == 1) {
                InventoryCloseListener.deleteNoDropMarker(button)
            } else {
                InventoryCloseListener.setNoDropMarker(button)
            }
        }

        if (result.customs().isNotEmpty()) {
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

            recipe.getResults(player.uniqueId, relate, mapped, mass, amount, isMultipleDisplayCall = false)
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

    private fun allCandidatesClickedProcess(
        player: Player,
        gui: Inventory,
        event: InventoryClickEvent,
    ) {
        if (CustomCrafterAPI.isGUITooOld(gui)) {
            player.closeInventory()
            return
        }

        if (event.rawSlot !in (0..<CustomCrafterAPI.CRAFTING_TABLE_TOTAL_SIZE)) {
            // a player clicks inventory or something
            return
        } else if (gui.getItem(event.rawSlot)?.let { it.type == Material.AIR } != false) {
            // clicked slot is empty
            return
        }

        val signature: ItemStack = gui.getItem(CustomCrafterAPI.ALL_CANDIDATE_SIGNATURE_SLOT) ?: return

        val currentPage: Int = signature.itemMeta.persistentDataContainer.get(
            CustomCrafterAPI.ALL_CANDIDATE_CURRENT_PAGE_NK,
            PersistentDataType.INTEGER
        ) ?: throw NoSuchElementException("Couldn't find 'all_candidate_current_page' key and value.")

        val result: Search.SearchResult = signature.itemMeta.persistentDataContainer.get(
            CustomCrafterAPI.ALL_CANDIDATE_RESULTS_NK,
            PersistentDataType.STRING
        )?.let { json -> Search.SearchResult.fromJson(json) }
            ?: throw NoSuchElementException("Couldn't find 'all_candidate_results' key and value.")

        val input: CraftView = signature.itemMeta.persistentDataContainer.get(
            CustomCrafterAPI.ALL_CANDIDATE_INPUT_NK,
            PersistentDataType.STRING
        )?.let { json -> CraftView.fromJson(json) }
            ?: throw NoSuchElementException("Couldn't find 'all_candidate_input' key and value.")

        val shiftUsed: Boolean = event.click.isShiftClick

        when (event.rawSlot) {
            CustomCrafterAPI.ALL_CANDIDATE_PREVIOUS_SLOT -> {
                // previous button SLOT
                allCandidatesOpenPage(result, input, currentPage, player, isPrevious = true)
            }

            CustomCrafterAPI.ALL_CANDIDATE_NEXT_SLOT -> {
                // next button SLOT
                allCandidatesOpenPage(result, input, currentPage, player, isPrevious = false)
            }

            in (0..<CustomCrafterAPI.ALL_CANDIDATE_PREVIOUS_SLOT) -> {
                // item slots
                val fromVanillaRecipe: Boolean = (currentPage == 0 && event.rawSlot == 0)
                val newCraftView: CraftView

                if (fromVanillaRecipe) {
                    result.vanilla()?.let { recipe ->
                        val firstItemCoordinate: CoordinateComponent =
                            CoordinateComponent.fromIndex(
                                Converter.getAvailableCraftingSlotIndices()
                                    .sorted()
                                    .firstOrNull { i ->
                                        input.materials[CoordinateComponent.fromIndex(i, followLimit = false)]?.type != Material.AIR
                                    } ?: return,
                                followLimit = false
                            )
                        val interestedSlots: Set<CoordinateComponent> = CoordinateComponent.squareFill(
                            size = 3,
                            dx = firstItemCoordinate.x,
                            dy = firstItemCoordinate.y,
                            safeTrim = false
                        )
                        val materials: List<ItemStack> = interestedSlots.map { c ->
                            input.materials[c] ?: ItemStack.empty()
                        }
                        val resultItem: ItemStack = recipe.result
                        val resultAmountTimes: Int = if (shiftUsed) materials
                            .filter { item -> !item.type.isAir }
                            .minOf { item -> item.amount } else 1
                        if (shiftUsed) resultItem.amount *= resultAmountTimes
                        player.inventory.addItem(resultItem)
                            .forEach { (_, over) ->
                                player.world.dropItem(player.location, over)
                            }
                    }
                    newCraftView = getDecrementedCraftView(input, shiftUsed = shiftUsed)
                } else {
                    val indexInCraftingSlots: Int = Converter.getAvailableCraftingSlotIndices()
                        .sorted()
                        .indexOf(event.rawSlot)
                    val index: Int =
                        if (currentPage == 0) indexInCraftingSlots - 1
                        else 44 + 45 * (currentPage - 1) + indexInCraftingSlots

                    val (cRecipe: CRecipe, mapped: MappedRelation) = result.customs()[index]
                    val times: Int =
                        if (shiftUsed) {
                            mapped.components
                                .filter { c -> !cRecipe.items[c.recipe]!!.mass }
                                .map { c -> c.input }
                                .minOfOrNull { c -> input.materials[c]?.amount ?: 1 } ?: 1
                        } else 1

                    cRecipe.getResults(
                        crafterID = player.uniqueId,
                        relate = mapped,
                        mapped = input.materials,
                        shiftClicked = shiftUsed,
                        calledTimes = times,
                        isMultipleDisplayCall = false
                    ).toTypedArray().let { arr ->
                        player.inventory.addItem(*arr).forEach { (_, over) ->
                            player.world.dropItem(player.location, over)
                        }
                    }

                    newCraftView = getDecrementedCraftView(input, shiftUsed, cRecipe to mapped)
                }

                gui.getItem(CustomCrafterAPI.ALL_CANDIDATE_SIGNATURE_SLOT)?.let { s ->
                    InventoryCloseListener.setNoDropMarker(s)
                }
                player.closeInventory()

                player.openInventory(newCraftView.toCraftingGUI())  // this gui will always close before open a new gui. So, needless to set 'no-drop marker'.
                val continueResult: Search.SearchResult = Search.search(
                    player.uniqueId,
                    newCraftView,
                    natural = !CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE
                ) ?: return

                if (CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE && continueResult.size() > 1) {
                    allCandidatesProcess(continueResult, newCraftView.toCraftingGUI(), player)
                } else {
                    process(
                        continueResult,
                        newCraftView.toCraftingGUI(),
                        mass = event.click.isShiftClick,
                        player
                    )
                }
            }

            else -> event.isCancelled = true
        }
    }

    /**
     * open previous or next page
     *
     * @param[result] a current SearchResult
     * @param[input] an input CraftView
     * @param[currentPage] a current result page index
     * @param[player] a button-clicked crafter
     * @param[isPrevious] which button clicked (true = previous, false = next)
     * @suppress
     * @since 5.0.8
     */
    private fun allCandidatesOpenPage(
        result: Search.SearchResult,
        input: CraftView,
        currentPage: Int,
        player: Player,
        isPrevious: Boolean
    ) {
        val maxPage: Int = result.customs().size.let { size ->
            if (size <= 44) 1
            else 1 + (size - 44) / 45 + if ((size - 44) % 45 != 0) 1 else 0
        }
        if (currentPage == 0 && isPrevious) return
        else if (currentPage == maxPage && !isPrevious) return
        val destinationPage: Int = currentPage + if (isPrevious) -1 else 1
        val startIndex: Int =
            if (destinationPage == 0) 0
            else 44 + 45 * (destinationPage - 1)
        val endIndex: Int =
            if (destinationPage == 0) 43
            else 44 + currentPage * 45 - 1
        val items: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        var index = 0
        if (destinationPage == 0) {
            items[CoordinateComponent.fromIndex(0)] = result.vanilla()?.result ?: ItemStack.empty()
            index++
        }
        (startIndex..min(result.customs().size, endIndex)).forEach { i ->
            val (recipe, relate) = result.customs()[i]
            val results: List<ItemStack> = recipe.getResults(
                player.uniqueId,
                relate,
                Converter.standardInputMapping(input.toCraftingGUI())!!,
                recipe.multipleCandidateDisplaySettingDefaultShiftClicked(),
                recipe.multipleCandidateDisplaySettingDefaultCalledTimes(),
                isMultipleDisplayCall = true
            )
            items[CoordinateComponent.fromIndex(index, followLimit = false)] =
                results.firstOrNull { item -> item.type.isItem } ?: notDisplayableItem(recipe.name)
            index++
        }
        // currentPage, results(SearchResult), input(CraftView)
        val signature: ItemStack = CustomCrafterAPI.allCandidatesSignature()
        CustomCrafterAPI.setAllCandidatesSignatureValues(currentPage, result, input, signature)
        val gui: Inventory = CustomCrafterAPI.getAllCandidateGUI(
            items,
            currentPage = destinationPage,
            signature = signature,
            placePreviousButton = destinationPage > 0,
            placeNextButton = destinationPage < maxPage
        )
        player.closeInventory()
        player.openInventory(gui)
    }

    /**
     * decrement items from the provided CraftView
     *
     * if [forCustomSettings] is not null, this runs a process for CRecipe.
     *
     * only for Shift clicked
     *
     * @param[view] current CraftView
     * @param[shiftUsed] a crafter used shift-click or not
     * @param[forCustomSettings] a matched result info. (requires these when matched custom recipe)
     * @suppress
     * @since 5.0.8
     */
    private fun getDecrementedCraftView(
        view: CraftView,
        shiftUsed: Boolean = true,
        forCustomSettings: Pair<CRecipe, MappedRelation>? = null
    ): CraftView {
        val minAmount: Int = view.materials.minOf { (_, i) -> i.amount }
        return forCustomSettings?.let { (cRecipe, mapped) ->
            val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
            mapped.components.forEach { component ->
                val matter: CMatter = cRecipe.items[component.recipe]!!
                val isMass: Boolean = matter.mass
                val decrementAmount: Int =
                    if (isMass) 1
                    else if (shiftUsed) (minAmount / matter.amount) * matter.amount
                    else matter.amount
                val newAmount: Int = max(0, view.materials[component.input]!!.amount - decrementAmount)
                map[component.input] =
                    if (newAmount == 0) ItemStack.empty()
                    else let {
                        val newItem: ItemStack = view.materials[component.input]?.clone() ?: ItemStack.empty()
                        newItem.amount = newAmount
                        newItem
                    }
            }
            CraftView(map, ItemStack.empty())
        } ?: run {
            val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
            view.materials.forEach { (c, item) ->
                val newAmount: Int = max(0, item.amount - if (shiftUsed) minAmount else 1)
                map[c] =
                    if (newAmount == 0) ItemStack.empty()
                    else let {
                        val newItem: ItemStack = item.clone()
                        newItem.amount = newAmount
                        newItem
                    }
            }
            CraftView(map, ItemStack.empty())
        }
    }

    private fun allCandidatesProcess(
        result: Search.SearchResult,
        gui: Inventory,
        player: Player
    ) {
        /*
        * - call MultiResultGuiOpenEvent (new event)
        * - add argument `isMultipleDisplayCall` to these.
        *   - CRecipe.getResult
        *   - ResultSupplier.func (field)
        *   - ResultSupplier.invoke (method)
         */

        if (result.customs().isEmpty() && result.vanilla() == null) return
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(gui) ?: return
        val craftViewJson: String = CraftView.fromInventory(gui)?.toJson() ?: return
        val resultJson: String = result.toJson()
        val signatureItem: ItemStack = CustomCrafterAPI.allCandidatesSignature()
        signatureItem.editMeta { meta ->
            meta.persistentDataContainer.set(
                NamespacedKey(CustomCrafter.getInstance(), "all_candidate_input"),
                PersistentDataType.STRING,
                craftViewJson
            )

            meta.persistentDataContainer.set(
                NamespacedKey(CustomCrafter.getInstance(), "all_candidate_results"),
                PersistentDataType.STRING,
                resultJson
            )
        }
        val displayItems: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        displayItems[CoordinateComponent.fromIndex(0)] = result.vanilla()?.result ?: ItemStack.empty()
        for (index in (1..<45)) {
            if (index > result.customs().size) continue
            val (recipe: CRecipe, relation: MappedRelation) = result.customs()[index - 1]
            displayItems[CoordinateComponent.fromIndex(index, followLimit = false)] = recipe.getResults(
                player.uniqueId,
                relation,
                mapped,
                shiftClicked = recipe.multipleCandidateDisplaySettingDefaultShiftClicked(),
                calledTimes = recipe.multipleCandidateDisplaySettingDefaultCalledTimes(),
                isMultipleDisplayCall = true
            ).firstOrNull { item -> item.type != Material.AIR && item.type.isItem }
                ?: notDisplayableItem(recipe.name)
        }
        val allCandidateGUI: Inventory = CustomCrafterAPI.getAllCandidateGUI(
            displayItems,
            currentPage = 0,
            signature = signatureItem,
            placeNextButton = result.customs().size >= 45,
            dropItemsOnClose = true
        )

        gui.getItem(CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT)?.let { button ->
            InventoryCloseListener.setNoDropMarker(button)
        }
        player.closeInventory()

        player.openInventory(allCandidateGUI)
    }

    private fun notDisplayableItem(recipeName: String): ItemStack {
        val item = ItemStack(Material.COMMAND_BLOCK)
        item.editMeta { meta ->
            meta.displayName(MiniMessage.miniMessage().deserialize("<red>Not Displayable Item"))
            meta.lore(listOf(Component.text("Recipe Name: $recipeName")))
        }
        return item
    }
}
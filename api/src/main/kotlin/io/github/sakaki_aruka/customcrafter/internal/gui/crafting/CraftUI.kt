package io.github.sakaki_aruka.customcrafter.internal.gui.crafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.event.failure.CraftInputInterruptEvent
import io.github.sakaki_aruka.customcrafter.event.failure.PreventDoubleCraftEvent
import io.github.sakaki_aruka.customcrafter.event.failure.ResultItemGiveFailEvent
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.objects.AsyncContext
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.search.Search
import io.github.sakaki_aruka.customcrafter.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.util.InventoryUtil.giveItems
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.CraftUIState
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate.AllCandidateUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class CraftUI(
    val dropOnClose: AtomicBoolean = AtomicBoolean(true),
    caller: Player? = null,
    baked: CraftUIDesigner.Baked? = null
): CustomCrafterUI, InventoryHolder {

    private val inventory: Inventory
    val bakedDesigner: CraftUIDesigner.Baked

    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val uiState: AtomicReference<CraftUIState> = AtomicReference(CraftUIState.IDLE)
    private val asyncContext: AtomicReference<AsyncContext?> = AtomicReference(null)

    init {
        val designContext = CraftUIDesigner.Context(player = caller)
        bakedDesigner = baked ?: CraftUIDesigner.bake(CustomCrafterAPI.getCraftUIDesigner(), designContext)

        bakedDesigner.isValid().exceptionOrNull()?.let { throw it }

        inventory = Bukkit.createInventory(
            this,
            54,
            bakedDesigner.title
        )

        bakedDesigner.apply(inventory)
    }

    companion object: CustomCrafterUI.InteractTriggered, CraftUIDesigner {
        override fun isTrigger(event: PlayerInteractEvent): Boolean {
            if (!CustomCrafterAPI.getUseCustomCraftUI()) {
                return false
            } else if (!event.action.isRightClick) {
                return false
            } else if (!event.player.hasPermission("cc.craftui.click.open")) {
                return false
            }
            val clicked: Block = event.clickedBlock?.takeIf { b -> b.type == Material.CRAFTING_TABLE }
                ?: return false
            val underCenter: Block = clicked.getRelative(0, -1, 0)
            val half: Int = CustomCrafterAPI.getBaseBlockSideSize() / 2
            for (dx in (-half..half)) {
                for (dz in (-half..half)) {
                    if (underCenter.getRelative(dx, 0, dz).type != CustomCrafterAPI.getBaseBlock()) {
                        return false
                    }
                }
            }
            return true
        }

        override fun open(event: PlayerInteractEvent) {
            event.isCancelled = true
            event.player.openInventory(CraftUI(caller = event.player).inventory)
        }

        const val MAKE_BUTTON = 35

        override fun title(context: CraftUIDesigner.Context): Component {
            return "Custom Crafter".toComponent()
        }

        override fun makeButton(context: CraftUIDesigner.Context): Pair<CoordinateComponent, ItemStack> {
            return CoordinateComponent.fromIndex(MAKE_BUTTON) to ItemStack(Material.ANVIL).apply {
                itemMeta = itemMeta.apply {
                    customName(Component.text("Making items"))
                }
            }
        }

        override fun blankSlots(context: CraftUIDesigner.Context): Map<CoordinateComponent, ItemStack> {
            val blank = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
                itemMeta = itemMeta.apply {
                    displayName(Component.empty())
                    // An additional datum to prevent accidental menu operation
                    persistentDataContainer.set(
                        NamespacedKey("custom_crafter", UUID.randomUUID().toString()),
                        PersistentDataType.STRING,
                        UUID.randomUUID().toString()
                    )
                }
            }
            return (0..<54)
                .filter { it % 9 >= 6 }
                .minus(MAKE_BUTTON)
                .associate { CoordinateComponent.fromIndex(it) to blank }
        }
    }

    override fun onPlayerInventoryClick(clicked: Inventory, event: InventoryClickEvent) {
        when (event.action) {
            InventoryAction.MOVE_TO_OTHER_INVENTORY -> {
                // Shift+click from player inventory to CraftUI
                val currentItem: ItemStack = event.currentItem?.clone() ?: return
                if (currentItem.type.isAir) {
                    return
                }
                if (uiState.get() != CraftUIState.IDLE) {
                    event.isCancelled = true
                    return
                }
                event.result = Event.Result.DENY
                event.currentItem = ItemStack.empty()
                val remaining: List<ItemStack> = event.inventory.addItem(currentItem).values.toList()
                (event.whoClicked as Player).give(remaining)
            }

            InventoryAction.COLLECT_TO_CURSOR -> {
                // Double-click to collect items; can pull from CraftUI slots as well
                if (uiState.get() != CraftUIState.IDLE) {
                    event.isCancelled = true
                }
            }

            else -> {
                // Purely player-inventory operations; ignore
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        this.isClosed.set(true)
        if (!this.dropOnClose.get()) {
            return
        }

        val player: Player = event.player as? Player ?: return

        // Try to transition from any interruptible state to IDLE.
        // Loop handles the race where state changes between read and CAS.
        var interrupted = false
        var current: CraftUIState = uiState.get()
        while (current == CraftUIState.SEARCHING || current == CraftUIState.GENERATING_RESULTS) {
            if (uiState.compareAndSet(current, CraftUIState.IDLE)) {
                asyncContext.getAndSet(null)?.interrupt()
                interrupted = true
                break
            }
            current = uiState.get()
        }

        // GIVING means runAtEntity was already submitted; the atomic block will complete the craft.
        // Materials are consumed there, so we must not return them here.
        if (uiState.get() == CraftUIState.GIVING) {
            return
        }

        if (interrupted) {
            CraftInputInterruptEvent(player).callEvent()
        }
        player.giveItems(saveLimit = true, *toView().materials.values.toTypedArray())
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        val clickedCoordinate: CoordinateComponent = CoordinateComponent.fromIndex(event.rawSlot)

        event.isCancelled = true
        when (clickedCoordinate) {
            this.bakedDesigner.makeButton.first -> {
                val view: CraftView = this.toView()
                if (view.materials.values.none { i -> !i.isEmpty }) {
                    return
                }
                asyncSearchHandler(event)
            }

            in bakedDesigner.craftSlots() -> {
                val current: CraftUIState = uiState.get()
                when (current) {
                    CraftUIState.IDLE -> {
                        event.isCancelled = false
                    }

                    CraftUIState.GIVING -> {
                        // runAtEntity submitted; block to preserve atomicity
                    }

                    else -> {
                        // SEARCHING or GENERATING_RESULTS: interrupt and pass the click through
                        val player: Player = event.whoClicked as? Player ?: return
                        if (uiState.compareAndSet(current, CraftUIState.IDLE)) {
                            asyncContext.getAndSet(null)?.interrupt()
                            CraftInputInterruptEvent(player).callEvent()
                            event.isCancelled = false
                        } else if (uiState.get() != CraftUIState.GIVING) {
                            // CAS raced to IDLE (e.g., double-click edge case); allow
                            event.isCancelled = false
                        }
                        // else: lost the race to GIVING → stay blocked
                    }
                }
            }
        }
    }

    private fun asyncSearchHandler(event: InventoryClickEvent) {
        val player: Player = (event.whoClicked as? Player) ?: return
        val shiftUsed: Boolean = event.isShiftClick

        if (!uiState.compareAndSet(CraftUIState.IDLE, CraftUIState.SEARCHING)) {
            PreventDoubleCraftEvent(player).callEvent()
            return
        }

        // Capture the view and create the search context on the main thread (current tick),
        // before any slot interaction could occur in subsequent ticks.
        val capturedView: CraftView = this.toView()
        val searchCtx: AsyncContext = AsyncContext.ofTurnOff()
        asyncContext.set(searchCtx)

        CompletableFuture.runAsync({
            try {
                val result: Search.SearchResult = Search.asyncSearch(
                    crafterId = player.uniqueId,
                    view = capturedView,
                    searchQuery = Search.SearchQuery.defaultModeOf(searchCtx)
                ).get()

                if (!uiState.compareAndSet(CraftUIState.SEARCHING, CraftUIState.GENERATING_RESULTS)) {
                    asyncContext.set(null)
                    return@runAsync
                }
                asyncContext.set(null)

                if (CustomCrafterAPI.getUseMultipleResultCandidateFeature() && result.size() > 1) {
                    openAllCandidateUI(result, shiftUsed, player)
                } else if (result.customs().isNotEmpty()) {
                    giveCustomRecipeResults(result, shiftUsed, player)
                } else if (result.vanilla() != null) {
                    giveVanillaRecipeResults(result, shiftUsed, player)
                } else {
                    uiState.set(CraftUIState.IDLE)
                }
            } catch (e: Exception) {
                asyncContext.set(null)
                uiState.set(CraftUIState.IDLE)
            }
        }, InternalAPI.executor)
    }

    private fun openAllCandidateUI(
        result: Search.SearchResult,
        shiftUsed: Boolean,
        player: Player
    ) {
        // Release the state lock before transferring control to AllCandidateUI.
        uiState.set(CraftUIState.IDLE)
        this.dropOnClose.set(false)
        val allUI = AllCandidateUI(
            view = this.toView(),
            player = player,
            result = result,
            useShift = shiftUsed,
            bakedCraftUIDesigner = this.bakedDesigner
        )

        InternalAPI.foliaLib.scheduler.runAtEntity(player) {
            if (!this.isClosed.get()) {
                player.openInventory(allUI.inventory)
            }
        }.get()
    }

    private fun giveCustomRecipeResults(
        result: Search.SearchResult,
        shiftUsed: Boolean,
        player: Player
    ) {
        val (recipe: CRecipe, relate: MappedRelation) = result.customs().firstOrNull() ?: run {
            uiState.set(CraftUIState.IDLE)
            return
        }
        val view: CraftView = this.toView()
        val amount: Int = recipe.getTimes(view.materials, relate, shiftUsed)
        val decrementedView: CraftView = view.clone().getDecremented(shiftUsed, recipe, relate)

        InternalAPI.foliaLib.scheduler.runAtEntity(player) {
            CreateCustomItemEvent(player, view, result, shiftUsed, isAsync = false).callEvent()
        }.get()

        val resultCtx: AsyncContext = AsyncContext.ofTurnOff()
        asyncContext.set(resultCtx)
        val resultSupplierContext: ResultSupplier.Context = ResultSupplier.Context(
            recipe, relate, view.materials, shiftUsed, amount, player.uniqueId,
            ResultSupplier.Context.CallMode.CRAFT, asyncContext = resultCtx
        )
        val results: List<ItemStack> = recipe.asyncGetResults(resultSupplierContext).get()
        asyncContext.set(null)

        // Transition to GIVING. If interrupted between here and the GENERATING_RESULTS CAS, abort.
        if (!uiState.compareAndSet(CraftUIState.GENERATING_RESULTS, CraftUIState.GIVING)) {
            ResultItemGiveFailEvent(results, resultSupplierContext, true).callEvent()
            return
        }

        if (!player.isOnline) {
            uiState.set(CraftUIState.IDLE)
            ResultItemGiveFailEvent(results, resultSupplierContext, true).callEvent()
            return
        }

        // Decrement and give are in one atomic tick; no interaction can interleave here.
        InternalAPI.foliaLib.scheduler.runAtEntity(player) {
            decrementedView.materials.forEach { (c, item) ->
                this.inventory.setItem(c.toIndex(), item)
            }
            player.giveItems(saveLimit = true, *results.toTypedArray())
            uiState.set(CraftUIState.IDLE)
        }.get()
    }

    private fun giveVanillaRecipeResults(
        result: Search.SearchResult,
        shiftUsed: Boolean,
        player: Player
    ) {
        val view: CraftView = this.toView()
        val min: Int = view.materials.values.minOf { it.amount }
        val decrementAmount: Int = if (shiftUsed) { min } else { 1 }
        val item: ItemStack = result.vanilla()!!.result.clone().apply { this.amount *= decrementAmount }

        if (!uiState.compareAndSet(CraftUIState.GENERATING_RESULTS, CraftUIState.GIVING)) {
            return
        }

        if (!player.isOnline) {
            uiState.set(CraftUIState.IDLE)
            ResultItemGiveFailEvent(listOf(item), null, true).callEvent()
            return
        }

        InternalAPI.foliaLib.scheduler.runAtEntity(player) {
            bakedDesigner.craftSlots()
                .filter { c -> this.inventory.getItem(c.toIndex())?.takeIf { i -> !i.type.isEmpty } != null }
                .forEach { c ->
                    val slot: ItemStack? = this.inventory.getItem(c.toIndex())
                    this.inventory.setItem(c.toIndex(), slot?.asQuantity(slot.amount - decrementAmount))
                }
            if (!item.type.isAir) {
                player.giveItems(saveLimit = true, item)
            }
            uiState.set(CraftUIState.IDLE)
        }.get()
    }

    override fun getInventory(): Inventory = this.inventory

    fun toView(
        noAir: Boolean = true
    ): CraftView {
        val materials: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        for (c in bakedDesigner.craftSlots()) {
            val item: ItemStack = this.inventory.getItem(c.toIndex())
                ?.takeIf { item -> !item.isEmpty }
                ?: if (noAir) continue else ItemStack.empty()
            materials[c] = item
        }

        return CraftView(materials)
    }
}
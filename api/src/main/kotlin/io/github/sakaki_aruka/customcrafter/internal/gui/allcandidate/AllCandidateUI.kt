package io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.event.failure.ResultItemGiveFailEvent
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.objects.AsyncContext
import io.github.sakaki_aruka.customcrafter.objects.CraftView
import io.github.sakaki_aruka.customcrafter.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.search.Search
import io.github.sakaki_aruka.customcrafter.recipe.CVanillaRecipe
import io.github.sakaki_aruka.customcrafter.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.util.InventoryUtil.giveItems
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import io.github.sakaki_aruka.customcrafter.ui.AllCandidateUIDesigner
import io.github.sakaki_aruka.customcrafter.ui.AllCandidateUIDesigner.Companion.bake
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

internal class AllCandidateUI(
    override val currentPage: AtomicInteger = AtomicInteger(0),
    private var view: CraftView,
    private val player: Player,
    private val result: Search.SearchResult,
    useShift: Boolean,
    private val bakedCraftUIDesigner: CraftUIDesigner.Baked,
    private val dropOnClose: AtomicBoolean = AtomicBoolean(true)
) : CustomCrafterUI.Pageable, InventoryHolder {
    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val iconGeneratorAsyncContext: AtomicReference<AsyncContext> = AtomicReference(AsyncContext.ofTurnOff())
    private val pages: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Pair<ItemStack, CRecipe>>> = ConcurrentHashMap()
    private val bakedAllCandidateUIDesigner: AllCandidateUIDesigner.Baked =
        CompletableFuture.supplyAsync({
            CustomCrafterAPI.getAllCandidateUIDesigner()
                .bake(AllCandidateUIDesigner.Context(result, player.uniqueId))
        }, InternalAPI.executor)
            .completeOnTimeout(AllCandidateUIDesigner.BAKED_DEFAULT, 50, TimeUnit.MILLISECONDS)
            .get()
            .let {
                if (it.isValid().isFailure) AllCandidateUIDesigner.BAKED_DEFAULT
                else it
            }

    private val inventory: Inventory = Bukkit.createInventory(
        this,
        54,
        bakedAllCandidateUIDesigner.title
    )

    init {
        val chunked = this.result.getMergedResults().chunked(bakedAllCandidateUIDesigner.recipeSlots.size)
        chunked.withIndex().forEach { (pageIndex, list) ->
            val pageMap = ConcurrentHashMap<Int, Pair<ItemStack, CRecipe>>()
            pages[pageIndex] = pageMap
            list.zip(bakedAllCandidateUIDesigner.recipeSlots).forEach { (pair, slotComponent) ->
                val (recipe, relation: MappedRelation?) = pair
                pageMap[slotComponent.toIndex()] = bakedAllCandidateUIDesigner.ungeneratedIcon(recipe) to recipe

                CompletableFuture.runAsync({
                    if (recipe is CVanillaRecipe) {
                        pages[pageIndex]?.put(slotComponent.toIndex(), recipe.original.result to recipe)
                        iconsUpdate(pageIndex)
                        return@runAsync
                    }

                    val context = ResultSupplier.Context(
                        recipe = recipe,
                        relation = relation ?: MappedRelation(emptySet()),
                        mapped = this.view.materials,
                        shiftClicked = useShift,
                        calledTimes = 1,
                        callMode = ResultSupplier.Context.CallMode.ICON,
                        crafterId = this.player.uniqueId,
                        asyncContext = iconGeneratorAsyncContext.get()
                    )

                    val icon: ItemStack = recipe.asyncGetResults(context).get().firstOrNull()
                        ?: bakedAllCandidateUIDesigner.noDisplayableItem

                    pages[pageIndex]?.put(slotComponent.toIndex(), icon to recipe)

                    iconsUpdate(pageIndex)

                }, InternalAPI.executor)
            }
        }

        this.inventory.apply {
            pageContentsAt(0).forEach { (slot, icon) -> setItem(slot, icon) }
        }
    }

    private fun iconsUpdate(pageNum: Int) {
        if (this.isClosed.get() || this.currentPage.get() != pageNum) {
            return
        }

        InternalAPI.foliaLib.scheduler.runAtEntity(this.player) {
            this.player.openInventory.topInventory.let { opening ->
                if (opening.holder !is AllCandidateUI) {
                    return@runAtEntity
                }
                pages[pageNum]?.let { contents ->
                    contents.entries.forEach { (slot, pair) ->
                        opening.setItem(slot, pair.first)
                    }
                }
            }
        }.get()
    }

    fun pageContentsAt(pageNumber: Int): Map<Int, ItemStack> {
        if (pageNumber !in (0..<pages.size)) {
            throw IllegalArgumentException("'pageNumber' must be in range of 0 ~ ${pages.size - 1}.")
        }

        val page: MutableMap<Int, ItemStack> = this.pages[pageNumber]
            ?.let { map -> map.entries.associate { (index, pair) -> index to pair.first }.toMutableMap() }
            ?: return emptyMap()

        val (backToCraftSlot, backToCraftItem) = this.bakedAllCandidateUIDesigner.backToCraftUIButton
        page[backToCraftSlot.toIndex()] = backToCraftItem

        if (pageNumber > 0) {
            val (previousButtonSlot, previousButtonItem) = this.bakedAllCandidateUIDesigner.previousPageButton
            page[previousButtonSlot.toIndex()] = previousButtonItem
        }

        if (pageNumber < pages.size - 1) {
            val (nextButtonSlot, nextButtonItem) = this.bakedAllCandidateUIDesigner.nextPageButton
            page[nextButtonSlot.toIndex()] = nextButtonItem
        }
        return page.toMap()
    }

    override fun flipPage() {
        if (!canFlipPage()) return
        this.currentPage.incrementAndGet()
        this.inventory.clear()
        pageContentsAt(this.currentPage.get()).entries.forEach { (slot, icon) ->
            this.inventory.setItem(slot, icon)
        }
    }

    override fun canFlipPage(): Boolean {
        return this.currentPage.get() < this.pages.size - 1
    }

    override fun flipBackPage() {
        if (!canFlipBackPage()) return
        this.currentPage.decrementAndGet()
        this.inventory.clear()
        pageContentsAt(this.currentPage.get()).entries.forEach { (slot, icon) ->
            this.inventory.setItem(slot, icon)
        }
    }

    override fun canFlipBackPage(): Boolean {
        return this.currentPage.get() > 0
    }

    override fun onClose(event: InventoryCloseEvent) {
        this.isClosed.set(true)
        this.iconGeneratorAsyncContext.get()?.interrupt()
        if (!this.dropOnClose.get()) {
            return
        }
        player.giveItems(saveLimit = true, *this.view.materials.values.toTypedArray())
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        event.isCancelled = true
        when (event.rawSlot) {

            this.bakedAllCandidateUIDesigner.previousPageButton.first.toIndex() -> if (canFlipBackPage()) {
                flipBackPage()
            }

            this.bakedAllCandidateUIDesigner.nextPageButton.first.toIndex() -> if (canFlipPage()) {
                flipPage()
            }

            this.bakedAllCandidateUIDesigner.backToCraftUIButton.first.toIndex() -> {
                val craftUI = CraftUI(
                    caller = event.whoClicked as? Player,
                    baked = this.bakedCraftUIDesigner
                )
                this.view.materials.entries.forEach { (c, item) ->
                    craftUI.inventory.setItem(c.toIndex(), item)
                }

                this.dropOnClose.set(false)
                this.player.openInventory(craftUI.inventory)
                return
            }

            in this.bakedAllCandidateUIDesigner.recipeSlotsIndex -> {
                if (event.currentItem == null) {
                    return
                }
                val player: Player = event.whoClicked as? Player ?: return
                val recipe: CRecipe = this.pages[this.currentPage.get()]?.let { contents ->
                    contents[event.rawSlot]?.second
                } ?: return
                val mappedRelation: MappedRelation? = this.result.getMergedResults()
                    .firstOrNull { (r, _) -> r == recipe }?.second

                if (recipe !is CVanillaRecipe && mappedRelation == null) {
                    return
                }

                val relation: MappedRelation = (recipe as? CVanillaRecipe)?.relateWith(view)
                    ?: mappedRelation
                        ?: return

                CompletableFuture.runAsync ({
                    val resultSupplierContext = ResultSupplier.Context(
                        recipe = recipe,
                        relation = relation,
                        mapped = view.materials,
                        shiftClicked = event.isShiftClick,
                        calledTimes = recipe.getTimes(view.materials, relation, event.isShiftClick),
                        callMode = ResultSupplier.Context.CallMode.CRAFT,
                        crafterId = player.uniqueId,
                        asyncContext = AsyncContext.ofTurnOff()
                    )
                    val results: List<ItemStack> = recipe.asyncGetResults(resultSupplierContext).get()

                    InternalAPI.foliaLib.scheduler.runAtEntity(player) {
                        CreateCustomItemEvent(player, this.view, this.result, event.isShiftClick, isAsync = false).callEvent()
                    }.get()

                    if (player.isOnline) {
                        InternalAPI.foliaLib.scheduler.runAtEntity(player) {
                            player.giveItems(saveLimit = true, *results.toTypedArray())
                        }.get()
                    } else {
                        ResultItemGiveFailEvent(results, resultSupplierContext, true).callEvent()
                    }
                }, InternalAPI.executor)

                this.view = this.view.getDecremented(
                    shiftUsed = event.isShiftClick,
                    recipe = recipe,
                    relations = relation,
                )

                val craftUI = CraftUI(
                    caller = event.whoClicked as? Player,
                    baked = this.bakedCraftUIDesigner
                )
                this.view.materials.entries.forEach { (c, item) ->
                    craftUI.inventory.setItem(c.toIndex(), item)
                }
                this.dropOnClose.set(false)
                player.openInventory(craftUI.inventory)
            }
        }
    }

    override fun getInventory(): Inventory = this.inventory
}
package io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.interfaces.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.recipe.CVanillaRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.AsyncUtil.fromBukkitMainThread
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal class AllCandidateUI(
    override val currentPage: AtomicInteger = AtomicInteger(0),
    private var view: CraftView,
    private val player: Player,
    private val result: Search.SearchResult,
    useShift: Boolean,
    private val bakedCraftUIDesigner: CraftUIDesigner.Baked,
    private var dropOnClose: Boolean = true,
) : CustomCrafterUI.Pageable, InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(
        this,
        54,
        "All Candidate".toComponent()
    )
    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val pages: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Pair<ItemStack, CRecipe>>> = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Pair<ItemStack, CRecipe>>>()
        .apply {
            // Initialize with empty data
            repeat((result.size() + 44) / 45) { index ->
                put(index, ConcurrentHashMap())
            }
        }

    init {
        val workers: MutableSet<CompletableFuture<Pair<ItemStack, CRecipe>>> = mutableSetOf()
        this.inventory.apply {
            pageContentsAt(0).forEach { (slot, icon) -> setItem(slot, icon) }
        }

        // Start Async Processes
        result.vanilla()?.let { vanilla ->
            CVanillaRecipe.fromVanilla(vanilla as CraftingRecipe)?.let { recipe ->
                workers.add(CompletableFuture.completedFuture(vanilla.result to recipe))
            }
        }
        result.customs().forEach { (recipe, relation) ->
            workers.add(CompletableFuture.supplyAsync ({
                val context = ResultSupplier.Context(
                    recipe = recipe,
                    relation = relation,
                    mapped = this.view.materials,
                    shiftClicked = useShift,
                    calledTimes = 1,
                    isMultipleDisplayCall = true,
                    crafterID = this.player.uniqueId,)
                Pair(
                    recipe.asyncGetResults(context).get().firstOrNull()
                        ?: replaceRecipeNameTemplate(CustomCrafterAPI.ALL_CANDIDATE_NO_DISPLAYABLE_ITEM, recipe.name),
                    recipe
                )
            }, InternalAPI.asyncExecutor())
                .exceptionallyAsync { InternalAPI.warn(it.stackTraceToString()); null }
            )
        }

        CompletableFuture.allOf(*workers.toTypedArray())
            .exceptionallyAsync ({ throwable ->
                if (throwable !is CancellationException) {
                    throw throwable
                }
                null
            }, InternalAPI.asyncExecutor())
            .thenAcceptAsync ({
                if (this.isClosed.get()) {
                    return@thenAcceptAsync
                }
                val elements: List<Pair<ItemStack, CRecipe>> = workers.map { it.join() }
                elements.chunked(45).withIndex()
                    .associate { (index, list) -> index to list }
                    .forEach { (index, list) ->
                        // Page and Elements
                        this.pages[index] = list.withIndex().associate { (i, l) -> i to l }.let { ConcurrentHashMap(it) }
                    }
                iconsUpdate(this.currentPage.get())
            }, InternalAPI.asyncExecutor())
    }

    private fun iconsUpdate(pageNum: Int) {
        Callable {
            if (this.isClosed.get()) {
                return@Callable
            } else if (this.currentPage.get() != pageNum) {
                return@Callable
            }

            this.player.openInventory.topInventory.let { opening ->
                if (opening.holder !is AllCandidateUI) {
                    return@Callable
                }
                pages[pageNum]?.let { contents ->
                    contents.entries.forEach { (slot, pair) ->
                        opening.setItem(slot, pair.first)
                    }
                }
            }
        }.fromBukkitMainThread()
    }

    companion object {
        const val NEXT = 53
        const val PREVIOUS = 45
        const val BACK_TO_CRAFT = 49

        val BACK_TO_CRAFT_BUTTON = ItemStack.of(Material.CRAFTING_TABLE).apply {
            itemMeta = itemMeta.apply {
                displayName("<b>BACK TO CRAFT".toComponent())
            }
        }
    }

    fun pageContentsAt(pageNumber: Int): Map<Int, ItemStack> {
        if (pageNumber !in (0..<pages.size)) {
            throw IllegalArgumentException("'pageNumber' must be in range of 0 ~ ${pages.size - 1}.")
        }

        val page: MutableMap<Int, ItemStack> = this.pages[pageNumber]
            ?.let { map -> map.entries.associate { (index, pair) -> index to pair.first }.toMutableMap() }
            ?: return emptyMap()

        page[BACK_TO_CRAFT] = BACK_TO_CRAFT_BUTTON

        if (pageNumber > 0) {
            page[PREVIOUS] = CustomCrafterUI.PREVIOUS_BUTTON
        }

        if (pageNumber < pages.size - 1) {
            page[NEXT] = CustomCrafterUI.NEXT_BUTTON
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
        if (!this.dropOnClose) {
            return
        }
        player.give(this.view.materials.values + this.view.result)
        this.isClosed.set(true)
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        event.isCancelled = true
        when (event.rawSlot) {

            PREVIOUS -> if (canFlipBackPage()) {
                flipBackPage()
            }

            NEXT -> if (canFlipPage()) {
                flipPage()
            }

            BACK_TO_CRAFT -> {
                val craftUI = CraftUI(
                    caller = event.whoClicked as? Player,
                    baked = this.bakedCraftUIDesigner
                )
                this.view.materials.entries.forEach { (c, item) ->
                    craftUI.inventory.setItem(c.toIndex(), item)
                }
                craftUI.inventory.setItem(
                    this.bakedCraftUIDesigner.resultInt(),
                    this.view.result
                )
                this.dropOnClose = false
                this.player.openInventory(craftUI.inventory)
                return
            }

            in PREVIOUS + 1..<NEXT -> {
                return
            }

            else -> {
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
                    val results: List<ItemStack> = recipe.getResults(ResultSupplier.Context(
                        recipe = recipe,
                        relation = relation,
                        mapped = view.materials,
                        shiftClicked = event.isShiftClick,
                        calledTimes = recipe.getTimes(view.materials, relation, event.isShiftClick),
                        isMultipleDisplayCall = false,
                        crafterID = player.uniqueId
                    ))

                    Callable {
                        results.forEach { item ->
                            if (!item.isEmpty) {
                                Bukkit.getPlayer(player.uniqueId)?.let { p ->
                                    p.location.world.dropItem(player.location, item)
                                }
                            }
                        }
                    }.fromBukkitMainThread()
                }, InternalAPI.asyncExecutor())

                if (!this.view.result.isEmpty) {
                    player.location.world.dropItem(player.location, this.view.result)
                    this.view = CraftView(this.view.materials, ItemStack.empty())
                }

                this.view = this.view.getDecremented(
                    shiftUsed = event.isShiftClick,
                    recipe = recipe,
                    relations = mappedRelation!!,
                )

                val craftUI = CraftUI(
                    caller = event.whoClicked as? Player,
                    baked = this.bakedCraftUIDesigner
                )
                this.view.materials.entries.forEach { (c, item) ->
                    craftUI.inventory.setItem(c.toIndex(), item)
                }
                craftUI.inventory.setItem(this.bakedCraftUIDesigner.resultInt(), this.view.result)
                this.dropOnClose = false
                player.openInventory(craftUI.inventory)
            }
        }
    }

    override fun getInventory(): Inventory = this.inventory

    private fun replaceRecipeNameTemplate(
        item: ItemStack,
        name: String
    ): ItemStack {
        val clone: ItemStack = item.clone()
        clone.lore(CustomCrafterAPI.ALL_CANDIDATE_NO_DISPLAYABLE_ITEM_LORE_SUPPLIER(name))
        return clone
    }
}
package io.github.sakaki_aruka.customcrafter.internal.gui.crafting

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.event.PreCreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.interfaces.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.giveItems
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

internal class CraftUI(
    var dropItemsOnClose: Boolean = true,
    caller: Player? = null,
    baked: CraftUIDesigner.Baked? = null
): CustomCrafterUI, InventoryHolder {

    private val inventory: Inventory
    val bakedDesigner: CraftUIDesigner.Baked

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

        const val RESULT_SLOT = 44
        const val MAKE_BUTTON = 35

        override fun title(context: CraftUIDesigner.Context): Component {
            return "Custom Crafter".toComponent()
        }

        override fun resultSlot(context: CraftUIDesigner.Context): CoordinateComponent {
            return CoordinateComponent.fromIndex(RESULT_SLOT)
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
                .minus(RESULT_SLOT)
                .minus(MAKE_BUTTON)
                .associate { CoordinateComponent.fromIndex(it) to blank }
        }
    }

    override fun onPlayerInventoryClick(clicked: Inventory, event: InventoryClickEvent) {
        val currentItem: ItemStack = event.currentItem?.clone() ?: return
        if (event.action != InventoryAction.MOVE_TO_OTHER_INVENTORY
            || currentItem.type.isAir) {
            return
        }

        event.result = Event.Result.DENY
        event.currentItem = ItemStack.empty()

        val resultSlotItem: ItemStack = event.inventory.getItem(this.bakedDesigner.resultInt())?.clone()
            ?: ItemStack.empty()

        // pseudo
        val stainedGlass: ItemStack = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply {
                persistentDataContainer.set(
                    NamespacedKey(CustomCrafter.getInstance(), "protect_result_slot"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString()
                )
            }
        }
        event.inventory.setItem(this.bakedDesigner.resultInt(), stainedGlass)

        val remaining: List<ItemStack> = event.inventory.addItem(currentItem).values.toList()
        // delete pseudo
        event.inventory.setItem(this.bakedDesigner.resultInt(), resultSlotItem)
        (event.whoClicked as Player).giveItems(saveLimit = true, *remaining.toTypedArray())
    }

    override fun onClose(event: InventoryCloseEvent) {
        if (!this.dropItemsOnClose) {
            return
        }
        val view: CraftView = this.toView()
        val player: Player = event.player as? Player ?: return
        player.giveItems(saveLimit = true, *view.materials.values.toTypedArray(), view.result)
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        val player: Player = event.whoClicked as? Player ?: return
        val clickedCoordinate = CoordinateComponent.fromIndex(event.rawSlot)

        event.isCancelled = true
        when (clickedCoordinate) {
            this.bakedDesigner.result -> {
                if (event.action in setOf(
                        InventoryAction.PLACE_ONE,
                        InventoryAction.PLACE_ALL,
                        InventoryAction.PLACE_SOME,
                        InventoryAction.PLACE_FROM_BUNDLE
                )) {
                    return
                }
                event.isCancelled = false
                return
            }

            this.bakedDesigner.makeButton.first -> {
                val view: CraftView = this.toView()
                if (view.materials.values.none { i -> !i.isEmpty }) return
                val preEvent = PreCreateCustomItemEvent(player, view, event.click)
                Bukkit.getPluginManager().callEvent(preEvent)
                if (preEvent.isCancelled) return

                val result: Search.SearchResult = Search.search(
                    player.uniqueId,
                    view,
                    forceSearchVanillaRecipe = !CustomCrafterAPI.getUseMultipleResultCandidateFeature()
                )

                if (result.size() == 0) return
                CreateCustomItemEvent(player, view, result, event.click).callEvent()
                if (CustomCrafterAPI.getResultGiveCancel()) return

                if (CustomCrafterAPI.getUseMultipleResultCandidateFeature() && result.size() > 1) {
                    val allCandidateUI = AllCandidateUI(
                        view = view,
                        player = player,
                        result = result,
                        useShift = event.isShiftClick,
                        bakedCraftUIDesigner = this.bakedDesigner
                    )
                    if (!allCandidateUI.inventory.isEmpty) {
                        this.dropItemsOnClose = false
                        player.openInventory(allCandidateUI.inventory)
                    }
                } else {
                    normalCrafting(result, event.isShiftClick, player)
                }
            }

            in bakedDesigner.craftSlots() -> {
                event.isCancelled = false
            }
        }
    }

    private fun normalCrafting(
        result: Search.SearchResult,
        shiftUsed: Boolean,
        player: Player
    ) {
        if (result.size() < 1) return

        val mapped: Map<CoordinateComponent, ItemStack> = bakedDesigner.craftSlots()
            .map { c -> c to (this.inventory.getItem(c.toIndex()) ?: ItemStack.empty()) }
            .filter { (_, item) -> !item.type.isAir }
            .toMap()
        if (result.customs().isNotEmpty()) {
            val (recipe: CRecipe, relate: MappedRelation) = result.customs().firstOrNull() ?: return

            val amount: Int = recipe.getTimes(mapped, relate, shift = shiftUsed)

            val decrementedView: CraftView = this.toView().clone().getDecremented(
                shiftUsed = shiftUsed, recipe = recipe, relations = relate
            )

            decrementedView.materials.forEach { (c, item) ->
                this.inventory.setItem(c.toIndex(), item)
            }

            val results: MutableList<ItemStack> = recipe.getResults(
                ResultSupplier.Context(
                    recipe = recipe,
                    crafterID = player.uniqueId,
                    relation = relate,
                    mapped = mapped,
                    list = mutableListOf(),
                    shiftClicked = shiftUsed,
                    calledTimes = amount,
                    isMultipleDisplayCall = false)
            )

            recipe.runNormalContainers(CRecipeContainer.Context(
                userID = player.uniqueId,
                relation = relate,
                mapped = mapped,
                results = results,
                isAllCandidateDisplayCall = false
            ))

            if (results.isNotEmpty()) {
                player.giveItems(saveLimit = true, *results.toTypedArray())
            }

        } else if (result.vanilla() != null) {
            // customs: Empty, vanilla: Exists
            val min: Int = mapped.values.minOf { i -> i.amount }
            val amount: Int = if (shiftUsed) min else 1
            bakedDesigner.craftSlots()
                .filter { c -> this.inventory.getItem(c.toIndex())?.takeIf { item -> !item.type.isEmpty } != null }
                .forEach { c ->
                    val newItem = this.inventory.getItem(c.toIndex())
                    this.inventory.setItem(c.toIndex(), newItem?.asQuantity(newItem.amount - min))
                }
            val item: ItemStack = result.vanilla()!!.result.apply { this.amount *= amount }
            player.giveItems(items = arrayOf(item))
        }
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

        val result: ItemStack = this.inventory.getItem(this.bakedDesigner.resultInt())
            ?: ItemStack.empty()
        return CraftView(materials, result)
    }
}
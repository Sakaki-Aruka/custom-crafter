package io.github.sakaki_aruka.customcrafter.internal.gui.crafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.event.PreCreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipeContainer
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
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
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import kotlin.math.max

internal class CraftUI(
    var dropItemsOnClose: Boolean = true
): CustomCrafterUI, InventoryHolder {

    private val inventory: Inventory = Bukkit.createInventory(
        this,
        54,
        "Custom Crafter".toComponent()
    )

    init {
        (0..<54).forEach { slot ->
            this.inventory.setItem(slot, ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
                itemMeta = itemMeta.apply {
                    displayName(Component.empty())
                    // An additional datum to prevent accidental menu operation
                    persistentDataContainer.set(
                        NamespacedKey("custom_crafter", UUID.randomUUID().toString()),
                        PersistentDataType.STRING,
                        UUID.randomUUID().toString()
                    )
                }
            })
        }
        Converter.getAvailableCraftingSlotComponents().forEach { c ->
            val index: Int = c.x + c.y * 9
            this.inventory.setItem(index, ItemStack.empty())
        }
        val makeButton: ItemStack = ItemStack(Material.ANVIL).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Making items"))
            }
        }
        this.inventory.setItem(CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT, makeButton)
        this.inventory.setItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT, ItemStack.empty())
    }

    companion object: CustomCrafterUI.InteractTriggered {
        override fun isTrigger(event: PlayerInteractEvent): Boolean {
            if (!CustomCrafterAPI.getUseCustomCraftUI()) {
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
            event.player.openInventory(CraftUI().inventory)
        }
    }

    override fun onPlayerInventoryClick(clicked: Inventory, event: InventoryClickEvent) {
        //
    }

    override fun onClose(event: InventoryCloseEvent) {
        if (!this.dropItemsOnClose) {
            return
        }
        val view: CraftView = CraftView.fromInventory(event.inventory) ?: return
        val player: Player = event.player as? Player ?: return
        player.giveItems(saveLimit = true, *view.materials.values.toTypedArray(), view.result)
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        val player: Player = event.whoClicked as? Player ?: return
        event.isCancelled = true
        when (event.rawSlot) {
            CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT -> {
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

            CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT -> {
                val view: CraftView = CraftView.fromInventory(inventory) ?: return
                if (view.materials.values.none { i -> !i.isEmpty }) return
                val preEvent = PreCreateCustomItemEvent(player, view, event.click)
                Bukkit.getPluginManager().callEvent(preEvent)
                if (preEvent.isCancelled) return

                val result: Search.SearchResult = Search.search(
                    player.uniqueId,
                    view,
                    natural = !CustomCrafterAPI.getUseMultipleResultCandidateFeature()
                )

                CreateCustomItemEvent(player, view, result, event.click).callEvent()
                if (CustomCrafterAPI.getResultGiveCancel()) return

                if (result.customs().isEmpty() && result.vanilla() == null) return

                if (CustomCrafterAPI.getUseMultipleResultCandidateFeature() && result.size() > 1) {
                    val allCandidateUI = AllCandidateUI(
                        view = view,
                        player = player,
                        result = result,
                        useShift = event.isShiftClick
                    )
                    if (!allCandidateUI.inventory.isEmpty) {
                        this.dropItemsOnClose = false
                        player.openInventory(allCandidateUI.inventory)
                    }
                } else {
                    normalCrafting(result, event.isShiftClick, player)
                }
            }

            in Converter.getAvailableCraftingSlotIndices() -> {
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

        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(this.inventory) ?: return
        if (result.customs().isNotEmpty()) {
            val (recipe: CRecipe, relate: MappedRelation) = result.customs().firstOrNull() ?: return

            val amount: Int = recipe.getMinAmount(mapped, relate, isCraftGUI = true, shift = shiftUsed)
                //?.takeIf { it > 0 }
                ?: return

            relate.components.forEach { (_, input) -> decrement(this.inventory, input.toIndex(), amount) }

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
            val min: Int = mapped.values.minOf { i -> i.amount }
            val amount: Int = if (shiftUsed) min else 1
            Converter.getAvailableCraftingSlotIndices()
                .filter { s -> this.inventory.getItem(s)?.takeIf { item -> !item.type.isEmpty } != null }
                .forEach { s -> decrement(this.inventory, s, amount) }
            val item: ItemStack = result.vanilla()!!.result.apply { this.amount *= amount }
            player.giveItems(items = arrayOf(item))
        }
    }

    private fun decrement(
        gui: Inventory,
        slot: Int,
        amount: Int
    ) {
        val item: ItemStack = gui.getItem(slot) ?: return
        val qty: Int = max(item.amount - amount, 0)
        if (qty == 0) {
            gui.setItem(slot, ItemStack.empty())
        } else {
            gui.setItem(slot, item.asQuantity(qty))
        }
    }

    override fun getInventory(): Inventory = this.inventory

    fun toView(
        noAir: Boolean = true
    ): CraftView {
        val materials: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
        for (slot in Converter.getAvailableCraftingSlotIndices()) {
            val c: CoordinateComponent = CoordinateComponent.fromIndex(slot)
            val item: ItemStack = this.inventory.getItem(slot)
                ?.takeIf { item -> !item.isEmpty }
                ?: if (noAir) continue else ItemStack.empty()
            materials[c] = item
        }

        val result: ItemStack = this.inventory.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT)
            ?: ItemStack.empty()
        return CraftView(materials, result)
    }
}
package io.github.sakaki_aruka.customcrafter.api.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.event.PreCreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.`object`.CraftView
import io.github.sakaki_aruka.customcrafter.api.`object`.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.`object`.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.processor.Converter
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
                if (inv is PlayerInventory
                    && CustomCrafterAPI.isCustomCrafterGUI(player.openInventory.topInventory)) {
                    if (click == ClickType.LEFT
                        || click == ClickType.RIGHT
                        || click == ClickType.SHIFT_LEFT
                    ) return
                    isCancelled = true
                    return
                }
            }
            return
        }

        if (CustomCrafterAPI.isGUITooOld(gui)) {
            isCancelled = true
            player.closeInventory()
            return
        } else if (isCancelled) return
        else if (rawSlot == CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT) {
            // click result slot
            isCancelled = true
            gui.getItem(rawSlot)?.let { item ->
                player.inventory.addItem(item).forEach { (_, over) ->
                    player.world.dropItem(player.location, over)
                }
            }
            return
        } else if (rawSlot == CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT) {
            // click make button
            isCancelled = true
            if (gui.contents.isEmpty()) return

            val view: CraftView = CraftView.fromInventory(gui) ?: return
            val preEvent = PreCreateCustomItemEvent(player, view, click)
            Bukkit.getPluginManager().callEvent(preEvent)
            if (preEvent.isCancelled) return

            val result: Search.SearchResult = Search.search(player.uniqueId, gui) ?: return

            CreateCustomItemEvent(player, view, result, click).callEvent()
            if (CustomCrafterAPI.RESULT_GIVE_CANCEL) return

            if (result.customs().isEmpty() && result.vanilla() == null) return

            val mass: Boolean = click == ClickType.SHIFT_LEFT

            process(result, gui, mass, player)
        } else if (!Converter.getAvailableCraftingSlotIndices().contains(rawSlot)) {
            // click a blank slot
            isCancelled = true
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

        if (result.customs().isNotEmpty()) {
            // custom recipe
            val (recipe, relate) = result.customs().first()
            if (getMinAmountWithoutMass(relate, recipe, mapped) <= 0) return
            recipe.items.forEach { (c, m) ->
                val inputCoordinate = relate.components.find { it.recipe == c }?.input ?: return
                val min: Int = getMinAmountWithoutMass(relate, recipe, mapped)
                val amount: Int = if (m.mass) 1 else m.amount * (if (mass) min else 1)
                val slot: Int = inputCoordinate.x + inputCoordinate.y * 9
                decrement(gui, slot, amount)
            }

            recipe.getResults(player.uniqueId, relate, mapped)
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
}
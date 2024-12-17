package com.github.sakakiaruka.customcrafter.customcrafter.api.listener

import com.github.sakakiaruka.customcrafter.customcrafter.api.CustomCrafterAPI
import com.github.sakakiaruka.customcrafter.customcrafter.api.event.CreateCustomItemEvent
import com.github.sakakiaruka.customcrafter.customcrafter.api.event.PreCreateCustomItemEvent
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.CraftView
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.MappedRelation
import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.recipe.CoordinateComponent
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import com.github.sakakiaruka.customcrafter.customcrafter.api.search.Search
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
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
        } ?: return

        if (CustomCrafterAPI.isGUITooOld(gui)) {
            isCancelled = true
            val world: World = player.world
            val location: Location = player.location
            Converter.getAvailableCraftingSlotIndices().forEach { slot ->
                gui.getItem(slot)?.takeIf { i -> i.type != Material.AIR }
                    ?.let { item ->
                        player.inventory.addItem(item).forEach { (_, over) ->
                            world.dropItem(location, over)
                        }
                    }
            }
            gui.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT)
                ?.takeIf { it.type != Material.AIR }
                ?.let { item ->
                    player.inventory.addItem(item).forEach { (_, over) ->
                        player.world.dropItem(player.location, over)
                    }
                }
            player.closeInventory()
            return
        } else if (isCancelled) return
        else if (rawSlot < 0) {
            isCancelled = true
            return
        } else if (rawSlot >= CustomCrafterAPI.CRAFTING_TABLE_TOTAL_SIZE) {
            if (click == ClickType.LEFT
                || click == ClickType.RIGHT
                || click == ClickType.SHIFT_LEFT
                ) return
            isCancelled = true
            return
        } else if ((0..54).contains(rawSlot)
            && !Converter.getAvailableCraftingSlotIndices().contains(rawSlot)) {
            // click a blank slot
            isCancelled = true
            return
        } else if (rawSlot == CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT) {
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
        }

//        when (slot) {
//            CustomCrafterAPI.CRAFTING_TABLE_MAKE_BUTTON_SLOT -> {
//                //
//            }
//            CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT -> {
//                val resultItem: ItemStack = gui.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT) ?: return
//                player.inventory.addItem(resultItem).forEach { (_, over) ->
//                    player.world.dropItem(player.location, over)
//                }
//            }
//            else -> {
//                isCancelled = true
//            }//isCancelled = true
//        }
    }

    private fun process(result: Search.SearchResult, gui: Inventory, mass: Boolean, player: Player) {
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
            .filter { c -> !recipe.items[c.recipe]!!.mass }
            .minOf { (r, i) -> mapped[i]!!.amount / recipe.items[r]!!.amount }
    }

    private fun decrement(gui: Inventory, slot: Int, amount: Int) {
        gui.getItem(slot)?.let { item ->
            item.amount = max(item.amount - amount, 0)
        }
    }
}
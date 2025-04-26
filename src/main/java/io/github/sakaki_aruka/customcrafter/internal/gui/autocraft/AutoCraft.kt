package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftingIdentifier
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.listener.NoPlayerListener
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.event.Event
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import java.util.UUID
import kotlin.math.max

object AutoCraft : NoPlayerListener{

    private val PSEUDO_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    override fun <T : Event> predicate(event: T): Boolean? {
        if (event !is BlockRedstoneEvent) return false
        else if (event.block.type !in InternalAPI.AUTO_CRAFTING_BLOCKS) return false
        return true
    }

    override fun <T : Event> func(event: T) {
        if (event !is BlockRedstoneEvent) return

        val cBlock: CBlock = CBlock.fromBlock(event.block) ?: return
        if (event.oldCurrent > 0 && event.newCurrent == 0) {
            turnOff(event.block, cBlock)
        }
        else if (event.oldCurrent == 0 && event.newCurrent > 0) {
            turnOn(event.block, cBlock)
        }
    }

    private fun turnOff(
        block: Block,
        cBlock: CBlock
    ) {}

    private fun turnOn(
        block: Block,
        cBlock: CBlock
    ) {
        // TODO impl
        if (!block.chunk.isLoaded) return
        else if (cBlock.recipes.isEmpty()) return

        val container: Container = block.state as? Container ?: return
        val elements: List<ItemStack> = container.inventory
            .filter { i -> !i.isEmpty }

        if (36 - cBlock.ignoreSlots.size != elements.size) return

        val pseudoInventory: Inventory = CustomCrafterAPI.getCraftingGUI()
        Converter.getAvailableCraftingSlotIndices()
            .zip(elements)
            .forEach { (index, item) ->
                pseudoInventory.setItem(index, item)
            }
        val sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
            .filterIsInstance<AutoCraftingIdentifier>()
            .filter { r -> r.autoCraftID.toString() in cBlock.recipes }

        val result: Search.SearchResult = Search.search(
            crafterID = PSEUDO_UUID,
            inventory = pseudoInventory,
            sourceRecipes = sourceRecipes
        ) ?: return

        if (result.size() == 0) return

        val (pair: Pair<CRecipe, MappedRelation>?, vanilla: Recipe?) = CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER(sourceRecipes, result, block)

        if (pair?.first != null && vanilla != null) {
            if (CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER_PRIORITIZE_CUSTOM) {
                giveCustomResult(pair, pseudoInventory, block)
            } else giveVanillaResult(vanilla, pseudoInventory, block)
        } else if (pair?.first != null) {
            giveCustomResult(pair, pseudoInventory, block)
        } else if (vanilla != null) {
            giveVanillaResult(vanilla, pseudoInventory, block)
        } else return
    }

    private fun giveCustomResult(
        pair: Pair<CRecipe, MappedRelation>,
        gui: Inventory,
        block: Block
    ) {
        val transformed: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(gui)
            ?: return
        val recipe: CRecipe = pair.first
        val results: MutableList<ItemStack> = recipe.getResults(
            crafterID = PSEUDO_UUID,
            relate = pair.second,
            mapped = transformed,
            shiftClicked = true,
            calledTimes = recipe.getMinAmount(transformed,  isCraftGUI = true, shift = true) ?: 1,
            isMultipleDisplayCall = false
        )

        recipe.runContainers(
            crafterID = PSEUDO_UUID,
            relate = pair.second,
            mapped = transformed,
            results = results,
            isMultipleDisplayCall = false
        )

        val view: CraftView = CraftView.fromInventory(gui)!!
            .getDecrementedCraftView(true, pair)

        block.world.let { w ->
            setOf(*view.materials.values.toTypedArray(), *results.toTypedArray()).forEach { i ->
                w.dropItem(block.getRelative(BlockFace.DOWN, 1).location, i)
            }
        }
    }
    private fun giveVanillaResult(
        recipe: Recipe,
        gui: Inventory,
        block: Block
    ) {
        val minAmount: Int = Converter.getAvailableCraftingSlotIndices()
            .mapNotNull { slot -> gui.getItem(slot) }
            .minOf { item -> item.amount }
        val result: ItemStack = recipe.result.apply { amount *= minAmount }
        block.world.dropItem(block.getRelative(BlockFace.DOWN, 1).location, result)

        val minCoordinate: CoordinateComponent = CoordinateComponent.fromIndex(
            index = Converter.getAvailableCraftingSlotComponents()
                .filter { c -> gui.getItem(c.toIndex()) != null && gui.getItem(c.toIndex())?.isEmpty == false }
                .minOf { c -> c.toIndex() }
        )
        CoordinateComponent.squareFill(3, minCoordinate.x, minCoordinate.y)
            .forEach { c ->
                gui.getItem(c.toIndex())?.let { item ->
                    item.asQuantity(max(0, item.amount - minAmount))
                }
            }
        block.world.let { w ->
            Converter.getAvailableCraftingSlotIndices()
                .filter { i ->  gui.getItem(i) != null && gui.getItem(i)?.isEmpty == false }
                .forEach { slot ->
                    w.dropItem(block.getRelative(BlockFace.DOWN, 1).location, gui.getItem(slot) ?: ItemStack.empty())
                }
        }
    }
}
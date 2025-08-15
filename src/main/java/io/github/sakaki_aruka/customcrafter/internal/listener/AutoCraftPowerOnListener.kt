package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.event.AutoCraftPowerOnEvent
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import java.util.UUID
import kotlin.math.floor
import kotlin.math.max

object AutoCraftPowerOnListener: Listener {

    private val PSEUDO_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @EventHandler
    fun AutoCraftPowerOnEvent.onPower() {
        val cBlock: CBlock = CBlock.of(this.crafter) ?: return

        turnOn(this.crafter.block, cBlock)
    }

    private fun turnOn(
        block: Block,
        cBlock: CBlock
    ) {
        if (!block.chunk.isLoaded) return
        else if (!CBlockDB.isLinked(block)) return
        else if (cBlock.getContainedItems().any { i -> i.isEmpty }) return

        //debug
        println("Turn on!!!")
        println("Items=${cBlock.getContainedItems()}")

        val pseudoInventory: Inventory = CraftUI().inventory
        cBlock.getContainedItems().zip(cBlock.slots).forEach { (item, index) ->
            pseudoInventory.setItem(index, item)
        }
        val sourceRecipes: List<CRecipe> = listOf(cBlock.getRecipe() ?: return)
        val result: Search.SearchResult = Search.search(
            crafterID = PSEUDO_UUID,
            inventory = pseudoInventory,
            sourceRecipes = sourceRecipes
        ) ?: return

        //debug
        println("Result=${result}")
        println("Custom=${result.customs()}")

        if (result.size() == 0) return

        val (autoCraftRecipe, relation, vanilla) = CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER(sourceRecipes, result, block)

        if (autoCraftRecipe != null  && relation != null) {
            if (CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER_PRIORITIZE_CUSTOM) {
                giveCustomResult(autoCraftRecipe, relation, pseudoInventory, cBlock)
            } else if (vanilla != null) {
                giveVanillaResult(vanilla, pseudoInventory, cBlock)
            }
        }  else if (vanilla != null) {
            giveVanillaResult(vanilla, pseudoInventory, cBlock)
        } else return
    }

    private fun giveCustomResult(
        autoCraftRecipe: AutoCraftRecipe,
        relation: MappedRelation,
        gui: Inventory,
        cBlock: CBlock
    ) {
        val transformed: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(gui)
            ?: return

        val results: MutableList<ItemStack> = autoCraftRecipe.getAutoCraftResults(
            block =  cBlock.block,
            relate = relation,
            mapped = transformed,
            calledTimes = autoCraftRecipe.getMinAmount(transformed, isCraftGUI = false, shift = true) ?: 1
        )

        autoCraftRecipe.runAutoCraftContainers(
            block = cBlock.block,
            relate = relation,
            mapped = transformed,
            results = results
        )

        val view: CraftView = CraftView.fromInventory(gui)!!
            .getDecrementedCraftView(true, autoCraftRecipe to relation)

        cBlock.block.world.let { w ->
            val dropLocation = Location(
                w,
                floor(cBlock.block.location.x) + 0.5,
                floor(cBlock.block.location.y) - 0.5,
                floor(cBlock.block.location.z) + 0.5
            )
            setOf(*view.materials.values.toTypedArray(), *results.toTypedArray()).forEach { i ->
                w.dropItem(dropLocation, i)
            }
        }

        cBlock.clearContainedItems()
    }
    private fun giveVanillaResult(
        recipe: Recipe,
        gui: Inventory,
        cBlock: CBlock
    ) {
        val minAmount: Int = Converter.getAvailableCraftingSlotIndices()
            .mapNotNull { slot -> gui.getItem(slot) }
            .minOf { item -> item.amount }
        val result: ItemStack = recipe.result.apply { amount *= minAmount }
        cBlock.block.world.dropItem(
            Location(
                cBlock.block.world,
                floor(cBlock.block.location.x) + 0.5,
                floor(cBlock.block.location.y) - 0.5,
                floor(cBlock.block.location.z) + 0.5
            ),
            result
        )

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
        cBlock.block.world.let { w ->
            val dropLocation: Location = cBlock.getDropLocation()
            Converter.getAvailableCraftingSlotIndices()
                .filter { i ->  gui.getItem(i) != null && gui.getItem(i)?.isEmpty == false }
                .forEach { slot ->
                    w.dropItem(dropLocation, gui.getItem(slot) ?: ItemStack.empty())
                }
        }
        cBlock.clearContainedItems()
    }
}
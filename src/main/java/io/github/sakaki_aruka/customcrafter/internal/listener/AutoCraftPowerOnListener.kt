package io.github.sakaki_aruka.customcrafter.internal.listener

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.event.AutoCraftPowerOnEvent
import io.github.sakaki_aruka.customcrafter.internal.gui.autocraft.ContainedItemsUI
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.jetbrains.annotations.TestOnly
import java.util.UUID
import kotlin.math.floor

object AutoCraftPowerOnListener: Listener {

    private val PSEUDO_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @EventHandler
    fun AutoCraftPowerOnEvent.onPower() {
        val cBlock: CBlock = CBlock.of(this.crafter) ?: return

        if (cBlock.isItemModifyCacheModeEnabled()) {
            turnOnInCacheMode(cBlock)
        } else {
            turnOnNormal(cBlock)
        }
    }

    @TestOnly
    internal fun turnOn(
        cBlock: CBlock
    ) {
        turnOnNormal(cBlock)
    }

    private fun turnOnInCacheMode(cBlock: CBlock) {
        if (!cBlock.block.chunk.isLoaded) return
        else if (!CBlockDB.isLinked(cBlock.block)) return

        val containedUI = ContainedItemsUI.of(cBlock, createNewIfNotExist = false) ?: return
        val view: CraftView = CraftView.fromInventory(containedUI.inventory) ?: return
        containedUI.placeableSlots.forEach { slot -> containedUI.inventory.setItem(slot, ItemStack.empty()) }
        val sourceRecipe: List<CRecipe> = listOf(cBlock.getRecipe() ?: return)

        turnOn(cBlock, sourceRecipe, view)
    }

    private fun turnOnNormal(cBlock: CBlock) {
        if (!cBlock.block.chunk.isLoaded) return
        else if (!CBlockDB.isLinked(cBlock.block)) return

        val view = CraftView(
            materials = cBlock.getContainedItems().zip(cBlock.slots)
                .associate { (item, slot) -> CoordinateComponent.fromIndex(slot) to item },
            result = ItemStack.empty()
        )
        val sourceRecipes: List<CRecipe> = listOf(cBlock.getRecipe() ?: return)

        turnOn(cBlock, sourceRecipes, view)
    }

    private fun turnOn(
        cBlock: CBlock,
        sourceRecipes: List<CRecipe>,
        view: CraftView,
    ) {
        val result: Search.SearchResult = Search.search(
            crafterID = PSEUDO_UUID,
            view = view,
            sourceRecipes = sourceRecipes
        ) ?: run {
            // No match -> drop all contained items
            view.drop(cBlock.block.world, cBlock.getDropLocation())
            return
        }

        if (result.size() == 0) {
            view.drop(cBlock.block.world, cBlock.getDropLocation())
            return
        }

        val (autoCraftRecipe, relation, vanilla) = CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER(sourceRecipes, result, cBlock.block)

        if (autoCraftRecipe != null  && relation != null) {
            if (CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER_PRIORITIZE_CUSTOM) {
                giveCustomResult(autoCraftRecipe, relation, view, cBlock)
            } else if (vanilla != null) {
                giveVanillaResult(vanilla, view, cBlock)
            }
        }  else if (vanilla != null) {
            giveVanillaResult(vanilla, view, cBlock)
        } else {
            view.drop(cBlock.block.world, cBlock.getDropLocation())
        }
    }

    private fun giveCustomResult(
        autoCraftRecipe: AutoCraftRecipe,
        relation: MappedRelation,
        view: CraftView,
        cBlock: CBlock
    ) {
        val transformed: Map<CoordinateComponent, ItemStack> = view.materials.filter { (_, item) -> !item.isEmpty }

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

        val decrementedView: CraftView = view.getDecrementedCraftView(
            shiftUsed = true,
            forCustomSettings = autoCraftRecipe to relation
        )

        cBlock.block.world.let { w ->
            val dropLocation = Location(
                w,
                floor(cBlock.block.location.x) + 0.5,
                floor(cBlock.block.location.y) - 0.5,
                floor(cBlock.block.location.z) + 0.5
            )
            setOf(*decrementedView.materials.values.toTypedArray(), *results.toTypedArray())
                .filter { item -> !item.isEmpty && item.type.isItem }
                .forEach { i ->
                    w.dropItem(dropLocation, i)
                }
        }

        cBlock.clearContainedItems()
    }
    private fun giveVanillaResult(
        recipe: Recipe,
        view: CraftView,
        cBlock: CBlock
    ) {
        val minAmount: Int = view.materials.values
            .filter { i -> !i.isEmpty }
            .minOf { i -> i.amount }
        val result: ItemStack = recipe.result.apply { amount *= minAmount }
        cBlock.block.world.dropItem(
            cBlock.getDropLocation(),
            result
        )

        val minCoordinate: CoordinateComponent =
            view.materials.entries.filter { (_, item) -> !item.isEmpty }.minByOrNull { (c, _) -> c.toIndex() }?.key ?: return

        view.reduceMaterials(
            amount = minAmount,
            coordinates = CoordinateComponent.squareFill(3, minCoordinate.x, minCoordinate.y)
        )

        cBlock.block.world.let { world ->
            val dropLocation: Location = cBlock.getDropLocation()
            view.materials.values
                .filter { item -> !item.isEmpty }
                .forEach { item -> world.dropItem(dropLocation, item) }
        }
        cBlock.clearContainedItems()
    }
}
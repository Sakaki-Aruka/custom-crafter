package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.listener.NoPlayerListener
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Crafter
import org.bukkit.event.Event
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

object AutoCraft {

    private val PSEUDO_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    object AutoCraftRedstoneSignalReceiver: NoPlayerListener {
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

        override fun <T : Event> predicate(event: T): Boolean? {
            return event is BlockRedstoneEvent
                    && event.block.type in InternalAPI.AUTO_CRAFTING_BLOCKS
                    && baseBlockCheck(event.block)
                    && CustomCrafterAPI.USE_AUTO_CRAFTING_FEATURE
        }
    }

    object AutoCraftItemInputSignalReceiver: NoPlayerListener {
        override fun <T : Event> func(event: T) {
            if (event !is InventoryMoveItemEvent) return

            val block: Block = (event.destination as Crafter).block
            val cBlock: CBlock = CBlock.fromBlock(block) ?: return

            event.isCancelled = true

            val (map: Map<Int, ItemStack>?, remaining: ItemStack?) = tryItemMerge(cBlock, event.item)
            if (map == null && remaining == null) {
                block.world.dropItem(block.getRelative(BlockFace.DOWN, 1).location, event.item)
                return
            } else if (map != null && remaining != null) {
                block.world.dropItem(block.getRelative(BlockFace.DOWN, 1).location, remaining)
            }

            CBlockDB.updateOrCreate(
                block,
                cBlock.copy(containedItems = map!!.toMutableMap()),
                setOf(CBlockDB.CBlockTableType.ITEM)
            )
        }


        /**
         * Returns
         * - (null to null)
         * - (Map<Int, ItemStack>, ItemStack>)
         * - (Map<Int, ItemStack>, null)
         */
        private fun tryItemMerge(
            cBlock: CBlock,
            addItem: ItemStack
        ): Pair<Map<Int, ItemStack>?, ItemStack?> {
            if (addItem.amount == 0 || cBlock.containedItems.size > 36) return (null to null)
            var amount: Int = addItem.amount
            val addAmounts: MutableMap<Int, Int> = mutableMapOf() // (index, amount)
            for ((slot: Int, item: ItemStack) in cBlock.containedItems.entries) {
                if (amount == 0) break
                else if (!item.isSimilar(addItem) || item.amount == item.type.maxStackSize) continue
                val enableAddAmount: Int = min(item.type.maxStackSize - item.amount, amount)
                amount -= enableAddAmount
                addAmounts[slot] = enableAddAmount
            }

            val modifiedItems: Map<Int, ItemStack> = cBlock.containedItems.entries.associate { (index, item) ->
                if (index !in addAmounts.keys) index to item
                else index to item.asQuantity(item.amount + addAmounts[index]!!)
            }
            return if (amount != 0) {
                modifiedItems to addItem.asQuantity(amount) // remaining contained
            } else {
                modifiedItems to null // all merged (success)
            }
        }

        override fun <T : Event> predicate(event: T): Boolean? {
            return event is InventoryMoveItemEvent
                    && event.destination.holder is Crafter
                    && baseBlockCheck((event.destination.holder as Crafter).block)
                    && CustomCrafterAPI.USE_AUTO_CRAFTING_FEATURE
        }
    }

    internal fun baseBlockCheck(crafter: Block): Boolean {
        val crafterLoc: Location = crafter.location
        val crafterWorld: World = crafter.world
        val underCenter = Location(crafterWorld, crafterLoc.x, crafterLoc.y - 1, crafterLoc.z)
        if (crafterWorld.getBlockAt(underCenter).type != Material.AIR) return false
        for (dz in (-1..1)) {
            for (dx in (-1..1)) {
                if (dx == 0 && dz == 0) continue
                val loc = Location(crafterWorld, underCenter.x + dx, underCenter.y, underCenter.z + dz)
                if (crafterWorld.getBlockAt(loc).type != CustomCrafterAPI.getAutoCraftingBaseBlock()) return false
            }
        }
        return true
    }

    private fun turnOff(
        block: Block,
        cBlock: CBlock
    ) {}

    private fun turnOn(
        block: Block,
        cBlock: CBlock
    ) {
        if (!block.chunk.isLoaded) return
        else if (cBlock.recipes.isEmpty()) return

        if (36 - cBlock.ignoreSlots.size != cBlock.containedItems.values.filter { i -> !i.isEmpty }.size) return

        val pseudoInventory: Inventory = CustomCrafterAPI.getCraftingGUI()
        cBlock.containedItems.entries.forEach { (index, item) ->
            pseudoInventory.setItem(index, item)
        }
        val sourceRecipes: List<CRecipe> = CustomCrafterAPI.getRecipes()
            .filterIsInstance<AutoCraftRecipe>()
            .filter { r -> r.getCBlockTitle() in cBlock.recipes }

        val result: Search.SearchResult = Search.search(
            crafterID = PSEUDO_UUID,
            inventory = pseudoInventory,
            sourceRecipes = sourceRecipes
        ) ?: return

        if (result.size() == 0) return

        val (autoCraftRecipe, relation, vanilla) = CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER(sourceRecipes, result, block)

        if (autoCraftRecipe != null  && relation != null && vanilla != null) {
            if (CustomCrafterAPI.AUTO_CRAFTING_RESULT_PICKUP_RESOLVER_PRIORITIZE_CUSTOM) {
                giveCustomResult(autoCraftRecipe, relation, pseudoInventory, block)
            } else giveVanillaResult(vanilla, pseudoInventory, block)
        }  else if (vanilla != null) {
            giveVanillaResult(vanilla, pseudoInventory, block)
        } else return
    }

    private fun giveCustomResult(
        autoCraftRecipe: AutoCraftRecipe,
        relation: MappedRelation,
        gui: Inventory,
        block: Block
    ) {
        val transformed: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(gui)
            ?: return
        val results: MutableList<ItemStack> = autoCraftRecipe.getAutoCraftResults(
            block =  block,
            relate = relation,
            mapped = transformed,
            calledTimes = autoCraftRecipe.getMinAmount(transformed, isCraftGUI = false, shift = true) ?: 1
        )

        autoCraftRecipe.runAutoCraftContainers(
            block = block,
            relate = relation,
            mapped = transformed,
            results = results
        )

        val view: CraftView = CraftView.fromInventory(gui)!!
            .getDecrementedCraftView(true, autoCraftRecipe to relation)

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
package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import io.github.sakaki_aruka.customcrafter.internal.gui.PredicateProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Serializable
internal data class SlotsModifyGUI(
    val worldID: String,
    val targetBlockX: Int,
    val targetBlockY: Int,
    val targetBlockZ: Int,
    override val id: String = CustomCrafterGUI.getID(SlotsModifyGUI::class).toString(),
    override val contextComponentSlot: Int = 45,
): CustomCrafterGUI.UnPageableGUI, PageOpenTrigger {
    companion object: PredicateProvider<CustomCrafterGUI> {
        override fun <T: Event> predicate(event: T): CustomCrafterGUI? {
            if (event !is PlayerInteractEvent) return null
            if (event.action != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() != Event.Result.ALLOW) return null
            val clicked: Block = event.clickedBlock
                ?.takeIf { b -> b.type in InternalAPI.AUTO_CRAFTING_BLOCKS }
                ?: return null
            val half: Int = InternalAPI.AUTO_CRAFTING_BASE_BLOCK_SIDE / 2
            val xzRange: IntRange = (-1 * half..half)
            val types: Set<Material> = setOf(CustomCrafterAPI.getAutoCraftingBaseBlock())

            for (dy: Int in -1..1) {
                for (dx: Int in xzRange) {
                    for (dz: Int in xzRange) {
                        if (dx == 0 && dz == 0) continue
                        if (clicked.world.getBlockAt(clicked.x + dx, clicked.y + dy, clicked.z + dz).type !in types) {
                            return null
                        }
                    }
                }
            }
            return SlotsModifyGUI(
                clicked.world.uid.toString(),
                clicked.x,
                clicked.y,
                clicked.z
            )
        }

        val PLACEABLE_SLOT = ItemStack(Material.LIME_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Item Place: OK"))
            }
        }

        val UN_PLACEABLE_SLOT = ItemStack(Material.RED_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Item Place: NG"))
            }
        }
    }

    override fun <T : Event> getFirstPage(event: T): Inventory? {
        if (event !is PlayerInteractEvent) return null
        val inventory: Inventory = Bukkit.createInventory(null, 54, Component.text("Auto Craft (Slot)"))

        // debug TODO: impl here
        val block: Block = getBlock() ?: return null
        val cBlock: CBlock = CBlock.fromBlock(block) ?: return null

        (0..<54).forEach { i ->
            inventory.setItem(i, CustomCrafterGUI.UN_CLICKABLE_SLOT)
        }

        Converter.getAvailableCraftingSlotIndices().forEach { i ->
            inventory.setItem(i, PLACEABLE_SLOT)
        }

        cBlock.ignoreSlots.forEach { i ->
            inventory.setItem(i, UN_PLACEABLE_SLOT)
        }

        inventory.setItem(contextComponentSlot, getDefaultContextComponent())

        return inventory
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        val cloned: ItemStack = contextItem.clone()
        val json: String = Json.encodeToString(this)
        cloned.editMeta { meta ->
            meta.persistentDataContainer.set(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING,
                json
            )
        }
        return cloned
    }

    override fun from(contextItem: ItemStack): CustomCrafterGUI? {
        val json: String = contextItem.itemMeta.persistentDataContainer.get(
            CustomCrafterGUI.CONTEXT_KEY,
            PersistentDataType.STRING
        ) ?: return null
        return Json.decodeFromString(json)
    }

    override fun eventReaction(
        event: InventoryClickEvent,
        ui: CustomCrafterGUI,
        inventory: Inventory
    ) {
        if (ui !is SlotsModifyGUI) return
        val availableSlots: Set<Int> = Converter.getAvailableCraftingSlotIndices()
        val player: Player = event.whoClicked as? Player ?: return
        val block: Block = Bukkit.getWorlds()
            .firstOrNull { w -> w.uid.toString() == this.worldID }
            ?.getBlockAt(targetBlockX, targetBlockY, targetBlockZ)
            ?: return
        val cBlock: CBlock = CBlock.fromBlock(block) ?: return

        event.isCancelled = true

        when (event.rawSlot) {
            in availableSlots -> {
                if (event.currentItem?.isSimilar(PLACEABLE_SLOT) == true) {
                    // register ignore slot
                    cBlock.ignoreSlots.add(event.rawSlot)
                    inventory.setItem(event.rawSlot, UN_PLACEABLE_SLOT)
                } else if (event.currentItem?.isSimilar(UN_PLACEABLE_SLOT) == true) {
                    // unregister ignore slot
                    cBlock.ignoreSlots.remove(event.rawSlot)
                    inventory.setItem(event.rawSlot, PLACEABLE_SLOT)
                }
                cBlock.write(block)
            }

            contextComponentSlot -> {
                val autoCraftRecipes: Set<AutoCraftRecipe> = getAvailableRecipeWithGivenSlots(
                    targetBlock = block,
                    availableSlots = Converter.getAvailableCraftingSlotIndices() - cBlock.ignoreSlots
                )

                if (autoCraftRecipes.isEmpty()) return

                val pages: MutableMap<Int, Map<Int, ByteArray>> = mutableMapOf()
                val recipePages: MutableMap<Int, Map<Int, String>> = mutableMapOf()
                autoCraftRecipes
                    .chunked(45)
                    .withIndex()
                    .forEach { (pageIndex, autoCraftRecipes) ->
                        val map: MutableMap<Int, ByteArray> = mutableMapOf()
                        val rMap: MutableMap<Int, String> = mutableMapOf()
                        autoCraftRecipes
                            .withIndex()
                            .forEach { (slotIndex, recipe) ->
                                map[slotIndex] = recipe.getAutoCraftDisplayItem(player).serializeAsBytes()
                                rMap[slotIndex] = recipe.autoCraftID.toString()
                            }
                        pages[pageIndex] = map
                        recipePages[pageIndex] = rMap
                    }

                val recipeModifyGUI = RecipeModifyGUI(
                    worldID = block.world.uid.toString(),
                    targetBlockX = block.x,
                    targetBlockY = block.y,
                    targetBlockZ = block.z,
                    recipePages = recipePages,
                    pages = pages
                )
                val contextComponent: ItemStack = recipeModifyGUI.write(recipeModifyGUI.getDefaultContextComponent()) ?: return

                val renderedGUI: Inventory = recipeModifyGUI.firstPage(
                    Bukkit.createInventory(
                        null,
                        54,
                        Component.text("Auto Craft (Recipe Modify)")
                    )
                ) ?: return
                renderedGUI.setItem(contextComponentSlot, contextComponent)
                player.openInventory(renderedGUI)
            }
        }
    }

    private fun getAvailableRecipeWithGivenSlots(
        targetBlock: Block,
        availableSlots: Set<Int>
    ): Set<AutoCraftRecipe> {
        return CustomCrafterAPI.AUTO_CRAFTING_SOURCE_RECIPES_PROVIDER(targetBlock)
            .filter { i ->
                if (i.type == CRecipeType.NORMAL) {
                    i.items.keys.map { c -> c.toIndex() }.toSet() == availableSlots
                } else {
                    // amorphous
                    i.items.size == availableSlots.size
                }
            }.toSet()
    }

    private fun getBlock(): Block? {
        val world: World = Bukkit.getWorlds()
            .firstOrNull { w -> w.uid.toString() == this.worldID }
            ?: return null
        return world.getBlockAt(
            this.targetBlockX,
            this.targetBlockY,
            this.targetBlockZ
        )
    }
}

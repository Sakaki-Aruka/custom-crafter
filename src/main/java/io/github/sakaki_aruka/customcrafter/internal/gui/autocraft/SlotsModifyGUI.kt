package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.AutoCraft
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlock
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import io.github.sakaki_aruka.customcrafter.internal.gui.PredicateProvider
import io.github.sakaki_aruka.customcrafter.internal.gui.ReactionProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

@Serializable
internal data class SlotsModifyGUI(
    val worldID: String,
    val targetBlockX: Int,
    val targetBlockY: Int,
    val targetBlockZ: Int,
    val customCrafterInternalGuiAutoCraftSlotsModifyGui: String? = null,
    override val id: String = CustomCrafterGUI.getIdOrRegister(SlotsModifyGUI::class).toString(),
    override val contextComponentSlot: Int = 8,
): CustomCrafterGUI.UnPageableGUI, PageOpenTrigger, ReactionProvider {
    companion object: PredicateProvider<CustomCrafterGUI>, CustomCrafterGUI.GuiDeserializer {
        override fun <T: Event> predicate(event: T): CustomCrafterGUI? {
            if (event !is PlayerInteractEvent) return null
            if (event.action != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() != Event.Result.ALLOW) return null
            val clicked: Block = event.clickedBlock
                ?.takeIf { b -> b.type in InternalAPI.AUTO_CRAFTING_BLOCKS }
                ?.takeIf { b -> CBlock.fromBlock(b) == null }
                ?: return null
            if (!AutoCraft.baseBlockCheck(clicked)) return null
            return SlotsModifyGUI(
                clicked.world.uid.toString(),
                clicked.x,
                clicked.y,
                clicked.z
            )
        }

        override fun from(contextItem: ItemStack): CustomCrafterGUI? {
            val json: String = contextItem.itemMeta.persistentDataContainer.get(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING
            ) ?: return null
            return Json.decodeFromString<SlotsModifyGUI>(json)
        }

        override fun id(): UUID = CustomCrafterGUI.getIdOrRegister(SlotsModifyGUI::class)

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

    override fun getDefaultContextComponent(): ItemStack {
        val base: ItemStack = super.getDefaultContextComponent()
        base.editMeta { meta ->
            meta.displayName(Component.text("Select Recipes"))
        }
        return base
    }

    override fun <T : Event> getFirstPage(event: T): Inventory? {
        if (event !is PlayerInteractEvent) return null
        val inventory: Inventory = Bukkit.createInventory(null, 54, Component.text("Auto Craft (Slot)"))
        (0..<54).forEach { i ->
            inventory.setItem(i, CustomCrafterGUI.UN_CLICKABLE_SLOT)
        }

        Converter.getAvailableCraftingSlotIndices().forEach { i ->
            inventory.setItem(i, PLACEABLE_SLOT)
        }

        Converter.getAvailableCraftingSlotIndices().forEach { slot ->
            inventory.setItem(slot, UN_PLACEABLE_SLOT)
        }

        inventory.setItem(contextComponentSlot, getDefaultContextComponent())

        return inventory
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        val cloned: ItemStack = contextItem.clone()
        val json: String = Json.encodeToString<SlotsModifyGUI>(this)

        cloned.editMeta { meta ->
            meta.persistentDataContainer.set(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING,
                buildJsonObject {
                    Json.parseToJsonElement(json).jsonObject
                        .forEach { (key, value) ->
                            put(key, value)
                        }
                    put("id", CustomCrafterGUI.getIdOrRegister(SlotsModifyGUI::class).toString())
                }.toString()
            )
        }
        return cloned
    }

    override fun eventReaction(
        event: InventoryClickEvent,
        ui: CustomCrafterGUI,
        inventory: Inventory,
        isTopInventory: Boolean
    ) {
        if (ui !is SlotsModifyGUI) return

        event.isCancelled = true

        val availableSlots: Set<Int> = Converter.getAvailableCraftingSlotIndices()
        val player: Player = event.whoClicked as? Player ?: return
        val block: Block = Bukkit.getWorlds()
            .firstOrNull { w -> w.uid.toString() == this.worldID }
            ?.getBlockAt(targetBlockX, targetBlockY, targetBlockZ)
            ?: return
        val cBlock:CBlock = CBlock.fromBlock(block) ?: CBlock.create(block) ?: return //CBlock(recipes = mutableSetOf())
        if (!isTopInventory) return

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
                //cBlock.write(block)
                cBlock.update(block, setOf(CBlockDB.CBlockTableType.C_BLOCK))
            }

            contextComponentSlot -> {

                //cBlock.write(block)
                cBlock.update(block, setOf(CBlockDB.CBlockTableType.C_BLOCK))

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
                                map[slotIndex] = recipe.autoCraftDisplayItemProvider(player).serializeAsBytes()
                                rMap[slotIndex] = recipe.getCBlockTitle() //autoCraftID.toString()
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
}

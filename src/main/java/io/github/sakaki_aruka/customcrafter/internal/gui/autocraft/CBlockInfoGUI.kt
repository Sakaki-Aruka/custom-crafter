package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.giveItems
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.AutoCraft
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
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Crafter
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * When a player clicks a data-written block, this will be displayed.
 * @since 5.0.10
 */
@Serializable
internal data class CBlockInfoGUI(
    val worldID: String,
    val targetBlockX: Int,
    val targetBlockY: Int,
    val targetBlockZ: Int,
    val customCrafterInternalGuiAutoCraftCBlockInfoGui: String? = null,
    override val id: String = CustomCrafterGUI.getIdOrRegister(CBlockInfoGUI::class).toString(),
    override val contextComponentSlot: Int = 8
): CustomCrafterGUI.UnPageableGUI, PageOpenTrigger, ReactionProvider {
    companion object: PredicateProvider<CustomCrafterGUI>, CustomCrafterGUI.GuiDeserializer {
        override fun <T : Event> predicate(event: T): CustomCrafterGUI? {
            if (event !is PlayerInteractEvent) return null
            if (event.action != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() != Event.Result.ALLOW) return null
            val clicked: Block = event.clickedBlock
                ?.takeIf { b -> b.type in InternalAPI.AUTO_CRAFTING_BLOCKS }
                ?.takeIf { b -> CBlock.fromBlock(b) != null }
                ?: return null

            if (!AutoCraft.baseBlockCheck(clicked)) return null
            return CBlockInfoGUI(clicked.world.uid.toString(), clicked.x, clicked.y, clicked.z)
        }

        override fun from(contextItem: ItemStack): CustomCrafterGUI? {
            val json: String = contextItem.itemMeta.persistentDataContainer.get(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING
            ) ?: return null
            return Json.decodeFromString<CBlockInfoGUI>(json)
        }

        override fun id(): UUID = CustomCrafterGUI.getIdOrRegister(CBlockInfoGUI::class)
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        val cloned: ItemStack = contextItem.clone()
        val json: String = Json.encodeToString<CBlockInfoGUI>(this)
        cloned.editMeta { meta ->
            meta.persistentDataContainer.set(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING,
                buildJsonObject {
                    Json.parseToJsonElement(json).jsonObject
                        .forEach { (key, value) ->
                            put(key, value)
                        }
                    put("id", CustomCrafterGUI.getIdOrRegister(CBlockInfoGUI::class).toString())
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
        if (ui !is CBlockInfoGUI) return

        event.isCancelled = true

        if (!isTopInventory) return

        when (event.rawSlot) {
            53 -> {
                // clear all data and open SlotModifyGUI
                val crafter: Crafter = getBlock()?.let { b -> b.state as? Crafter } ?: return
                val cBlock: CBlock = CBlock.fromBlock(crafter.block) ?: return

                (event.whoClicked as Player).giveItems(true, *cBlock.reset(crafter.block).toTypedArray())

                val gui: Inventory = SlotsModifyGUI(
                    crafter.block.world.uid.toString(),
                    crafter.block.x,
                    crafter.block.y,
                    crafter.block.z
                ).getFirstPage(PlayerInteractEvent(
                    event.whoClicked as Player, Action.RIGHT_CLICK_BLOCK, null, crafter.block, BlockFace.UP
                )) ?: return
                event.whoClicked.openInventory(gui)
            }
            else -> return
        }
    }

    override fun <T : Event> getFirstPage(event: T): Inventory? {
        if (event !is PlayerInteractEvent) return null
        val block: Block = event.clickedBlock ?: return null
        val cBlock: CBlock = CBlock.fromBlock(block) ?: return null
        val gui: Inventory = Bukkit.createInventory(null, 54, Component.text("Auto Craft (Info / UnEditable)"))

        (0..<54).forEach { i ->
            gui.setItem(i, CustomCrafterGUI.UN_CLICKABLE_SLOT)
        }

        Converter.getAvailableCraftingSlotIndices().forEach { i ->
            gui.setItem(i, SlotsModifyGUI.PLACEABLE_SLOT)
        }

        cBlock.ignoreSlots.forEach { i ->
            gui.setItem(i, SlotsModifyGUI.UN_PLACEABLE_SLOT)
        }

        val names: List<Component> = cBlock.recipes
            .mapNotNull { id ->
                CustomCrafterAPI.AUTO_CRAFTING_SOURCE_RECIPES_PROVIDER(block)
                    .firstOrNull { recipe -> recipe.getCBlockTitle() == id }
                    ?.let { recipe ->
                        MiniMessage.miniMessage().deserialize("<white>${recipe.name}")
                    }
            }

        gui.setItem(contextComponentSlot, getDefaultContextComponent().apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Registered Recipes"))
                lore(names)
            }
        })

        gui.setItem(53, ItemStack(Material.END_CRYSTAL).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Clear data & Open Modify Page"))
            }
        })

        return gui
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

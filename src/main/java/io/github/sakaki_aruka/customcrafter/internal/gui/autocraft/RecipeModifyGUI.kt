package io.github.sakaki_aruka.customcrafter.internal.gui.autocraft

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.ReactionProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.modules.SerializersModule
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

@Serializable
internal data class RecipeModifyGUI(
    val worldID: String,
    val targetBlockX: Int,
    val targetBlockY: Int,
    val targetBlockZ: Int,
    val recipePages: Map<Int, Map<Int, String>>, // Map<PageNum, Map<SlotNum, AutoCraftingIdentifier's ID>
    val customCrafterInternalGuiAutoCraftRecipeModifyGui: String? = null,
    override val id: String = CustomCrafterGUI.getIdOrRegister(RecipeModifyGUI::class).toString(),
    override val pages: Map<Int, Map<Int, ByteArray>>,
    override val previousPageButtonSlot: Int = 45,
    override val nextPageButtonSlot: Int = 53,
    override var currentPage: Int = 0,
    override val contextComponentSlot: Int = 49
): CustomCrafterGUI.PageableGUI, ReactionProvider {

    companion object: CustomCrafterGUI.GuiDeserializer {
        override fun from(contextItem: ItemStack): CustomCrafterGUI? {
            val json: String = contextItem.itemMeta.persistentDataContainer.get(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING
            ) ?: return null
            return Json.decodeFromString<RecipeModifyGUI>(json)
        }

        override fun id(): UUID = CustomCrafterGUI.getIdOrRegister(RecipeModifyGUI::class)
    }

    override fun getConstantItems(): Map<Int, ItemStack> {
        return (45..50).map { i ->
            i to CustomCrafterGUI.UN_CLICKABLE_SLOT
        }.toMap()
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        val cloned: ItemStack = contextItem.clone()
        val json: String = Json.encodeToString(this)
        cloned.editMeta { meta ->
            meta.persistentDataContainer.set(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING,
                buildJsonObject {
                    Json.parseToJsonElement(json).jsonObject
                        .forEach { (key, value) ->
                            put(key, value)
                        }
                    put("id", CustomCrafterGUI.getIdOrRegister(RecipeModifyGUI::class).toString())
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
        event.isCancelled = true

        if (ui !is RecipeModifyGUI
            || event.currentItem == null
            || event.currentItem?.isEmpty == true
            || !isTopInventory
        ) return
        val player: Player = event.whoClicked as? Player ?: return

        when (event.rawSlot) {
            previousPageButtonSlot -> {
                player.openInventory(previousPage(baseInventory()) ?: return)
            }
            nextPageButtonSlot -> {
                player.openInventory(nextPage(baseInventory()) ?: return)
            }
            in (0..<45) -> {
                val block: Block = Bukkit.getWorlds()
                    .firstOrNull { w -> w.uid.toString() == worldID }
                    ?.let { w ->
                        w.getBlockAt(
                            this.targetBlockX,
                            this.targetBlockY,
                            this.targetBlockZ
                        )
                    } ?: return

                val recipe: AutoCraftRecipe = recipePages[currentPage]
                    ?.get(event.rawSlot)
                    ?.let { recipeID ->
                        CustomCrafterAPI.AUTO_CRAFTING_SETTING_PAGE_SUGGESTION(block, player)
                            .firstOrNull { i ->
                                i.getCBlockTitle() == recipeID
                            }
                    } ?: return

                val cBlock: CBlock = CBlock.fromBlock(block) ?: return

                if (cBlock.recipes.contains(recipe.getCBlockTitle())) {
                    cBlock.recipes.remove(recipe.getCBlockTitle())
                } else {
                    cBlock.recipes.add(recipe.getCBlockTitle())
                }
                //cBlock.write(block)
                cBlock.update(block, setOf(CBlockDB.CBlockTableType.C_BLOCK))
            }
        }
    }

    private fun baseInventory(): Inventory {
        return Bukkit.createInventory(null, 54, Component.text("Auto Craft (Recipe)"))
    }
}

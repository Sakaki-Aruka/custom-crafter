package io.github.sakaki_aruka.customcrafter.internal.gui

import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftingGUI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

@Serializable
internal data class OldWarnGUI(
    override val id: String = CustomCrafterGUI.getIdOrRegister(OldWarnGUI::class).toString(),
    override val contextComponentSlot: Int = 13
): CustomCrafterGUI.UnPageableGUI, ReactionProvider {
    companion object: CustomCrafterGUI.GuiDeserializer {
        override fun from(contextItem: ItemStack): CustomCrafterGUI? {
            val json: String = contextItem.itemMeta.persistentDataContainer.get(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING
            ) ?: return null
            return Json.decodeFromString<OldWarnGUI>(json)
        }

        override fun id(): UUID = CustomCrafterGUI.getIdOrRegister(OldWarnGUI::class)

        fun getPage(): Inventory {
            val pseudoInstance = OldWarnGUI()
            val page: Inventory = Bukkit.createInventory(null, 27, Component.text("OLD GUI WARN"))
            page.setItem(pseudoInstance.contextComponentSlot, pseudoInstance.getDefaultContextComponent())
            return page
        }
    }

    override fun getDefaultContextComponent(): ItemStack {
        val base: ItemStack = super.getDefaultContextComponent().withType(Material.REDSTONE_BLOCK)
        base.editMeta { meta ->
            meta.displayName(Component.text("OLD GUI. CLICK & CLOSE."))
        }
        return base
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        val cloned: ItemStack = contextItem.clone()
        val json: String = Json.encodeToString<OldWarnGUI>(this)

        cloned.editMeta { meta ->
            meta.persistentDataContainer.set(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING,
                buildJsonObject {
                    Json.parseToJsonElement(json).jsonObject
                        .forEach { (key, value) -> put(key, value) }
                    put("id", CustomCrafterGUI.getIdOrRegister(OldWarnGUI::class).toString())
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
        if (ui !is OldWarnGUI) return

        event.isCancelled = true
        if (isTopInventory) {
            when (event.rawSlot) {
                ui.contextComponentSlot -> {
                    // close
                    event.whoClicked.closeInventory()
                }
            }
        }
    }
}

package io.github.sakaki_aruka.customcrafter.internal.gui

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import kotlin.reflect.KClass

internal sealed interface CustomCrafterGUI {
    val id: String
    val contextComponentSlot: Int

    fun write(contextItem: ItemStack): ItemStack?
    fun onClose(event: InventoryCloseEvent) {}
    fun getDefaultContextComponent(): ItemStack {
        val item = ItemStack(Material.CRAFTING_TABLE)
        item.editMeta { meta ->
            meta.displayName(Component.text(""))

            val (key, type, time) = CustomCrafterAPI.genCCKey()
            meta.persistentDataContainer.set(
                key, type, time
            )
        }
        return write(item) ?: item
    }

    companion object {
        val CONTEXT_KEY = NamespacedKey(
            CustomCrafter.getInstance(),
            "gui_context_key"
        )

        val UN_CLICKABLE_SLOT = ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text(""))
            }
        }

        val PAGES: MutableMap<KClass<out CustomCrafterGUI>, UUID> = mutableMapOf()
        val DESERIALIZERS: MutableSet<GuiDeserializer> = mutableSetOf()

        fun getIdOrRegister(
            clazz: KClass<out CustomCrafterGUI>
        ): UUID {
            return PAGES.getOrPut(clazz) { UUID.randomUUID() }
        }

        fun getGUI(inventory: Inventory): CustomCrafterGUI? {
            val keyItems: List<ItemStack> = inventory.contents
                .filterNotNull()
                .filter { i -> !i.isEmpty }
                .filter { i ->
                    i.itemMeta.persistentDataContainer.has(
                        CONTEXT_KEY,
                        PersistentDataType.STRING
                    )
                }

            if (keyItems.isEmpty()) return null

            return keyItems.firstNotNullOfOrNull { item ->
                val element: JsonElement = Json.parseToJsonElement(item.itemMeta.persistentDataContainer.get(
                    CONTEXT_KEY,
                    PersistentDataType.STRING
                )!!)

                val id: UUID = UUID.fromString(element.jsonObject["id"]!!.toString().replace("\"", ""))

                DESERIALIZERS
                    .filter { d -> d.id() == id }
                    .filter { d -> d.from(item) != null }
                    .firstNotNullOfOrNull { d -> d.from(item) }
            }
        }
    }

    interface GuiDeserializer {
        fun from(contextItem: ItemStack): CustomCrafterGUI?
        fun id(): UUID
    }

    interface PageableGUI: CustomCrafterGUI {
        val previousPageButtonSlot: Int
        val nextPageButtonSlot: Int
        var currentPage: Int
        val pages: Map<Int, Map<Int, ByteArray>> // Map<PageNum, Map<SlotNum, Item>>

        fun getConstantItems(): Map<Int, ItemStack>

        fun getNextPageButtonItem(): ItemStack {
            val item = ItemStack(Material.LIME_DYE)
            item.editMeta { meta ->
                meta.displayName(Component.text("Next Page"))
            }
            return item
        }

        fun getPreviousPageButtonItem(): ItemStack {
            val item = ItemStack(Material.RED_DYE)
            item.editMeta { meta ->
                meta.displayName(Component.text("Previous Page"))
            }
            return item
        }

        fun getNextPageContextComponent(contextItem: ItemStack): ItemStack? {
            if (currentPage + 1 > pages.keys.max()) return null
            currentPage += 1
            val cloned: ItemStack = contextItem.clone()
            write(cloned)
            return cloned
        }

        fun getPreviousPageContextComponent(contextItem: ItemStack): ItemStack? {
            if (currentPage - 1 < 0) return null
            currentPage -= 1
            val cloned: ItemStack = contextItem.clone()
            write(cloned)
            return cloned
        }

        fun firstPage(inv: Inventory? = null): Inventory? {
            val base: Inventory = inv ?: Bukkit.createInventory(null, 54)
            val firstPageContent: Map<Int, ByteArray> = pages[0] ?: return null
            base.clear()
            base.setItems(getConstantItems())
            base.setByteArrayItems(firstPageContent)
            if (pages.containsKey(1)) {
                base.setItem(nextPageButtonSlot, getNextPageButtonItem())
            }
            base.setItem(contextComponentSlot, getDefaultContextComponent())
            return base
        }

        fun nextPage(inv: Inventory? = null): Inventory? {
            val base: Inventory = inv ?: Bukkit.createInventory(null, 54)
            val nextPageContents: Map<Int, ByteArray> = pages[currentPage + 1] ?: return null
            base.clear()
            base.setItems(getConstantItems())
            base.setByteArrayItems(nextPageContents)
            base.setItem(contextComponentSlot, getNextPageContextComponent(getDefaultContextComponent()))
            if (currentPage > 0) {
                base.setItem(previousPageButtonSlot, getPreviousPageButtonItem())
            } else if (currentPage < pages.keys.max()) {
                base.setItem(nextPageButtonSlot, getNextPageButtonItem())
            }
            return base
        }

        fun previousPage(inv: Inventory? = null): Inventory? {
            if (currentPage < 1) return null
            val base: Inventory = inv ?: Bukkit.createInventory(null, 54)
            val previousPageContents: Map<Int, ByteArray> = pages[currentPage - 1] ?: return null
            base.clear()
            base.setItems(getConstantItems())
            base.setByteArrayItems(previousPageContents)
            base.setItem(contextComponentSlot, getPreviousPageContextComponent(getDefaultContextComponent()))
            if (currentPage > 0) {
                base.setItem(previousPageButtonSlot, getPreviousPageButtonItem())
            } else if (currentPage < pages.keys.max()) {
                base.setItem(nextPageButtonSlot, getNextPageButtonItem())
            }
            return base
        }

        companion object {
            private fun Inventory.setItems(items: Map<Int, ItemStack>) {
                items.forEach { (slot, item) ->
                    this.setItem(slot, item)
                }
            }

            private fun Inventory.setByteArrayItems(items: Map<Int, ByteArray>) {
                items.forEach { (slot, byteArrayItem) ->
                    this.setItem(slot, ItemStack.deserializeBytes(byteArrayItem))
                }
            }
        }

        interface GUIComponent {
            //
        }
    }

    interface UnPageableGUI: CustomCrafterGUI {
    }
}
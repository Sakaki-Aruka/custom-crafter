package io.github.sakaki_aruka.customcrafter.internal.gui

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

internal sealed interface CustomCrafterGUI {
    val id: String
    val contextComponentSlot: Int

    fun write(contextItem: ItemStack): ItemStack?
    fun from(contextItem: ItemStack): CustomCrafterGUI?
    fun eventReaction(
        event: InventoryClickEvent,
        ui: CustomCrafterGUI,
        inventory: Inventory
    )
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

        val PAGES: MutableMap<UUID, KClass<out CustomCrafterGUI>> = mutableMapOf()

        fun getGUI(inventory: Inventory): CustomCrafterGUI? {
            val contextItem: ItemStack = inventory.firstOrNull { i ->
                i.itemMeta.persistentDataContainer.has(
                    CONTEXT_KEY,
                    PersistentDataType.STRING
                )
            } ?: return null

            val id: UUID = contextItem.itemMeta.persistentDataContainer.get(
                CONTEXT_KEY,
                PersistentDataType.STRING
            )
                ?.let { s -> UUID.fromString(s) }
                ?: return null

            return PAGES[id]?.createInstance()?.from(contextItem)
        }


        fun getID(clazz: KClass<out CustomCrafterGUI>): UUID {
            return PAGES.entries.firstOrNull { (_, c) ->
                c == clazz
            }?.key ?: let {
                val id: UUID = UUID.randomUUID()
                PAGES[id] = clazz // register
                id
            }
        }
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
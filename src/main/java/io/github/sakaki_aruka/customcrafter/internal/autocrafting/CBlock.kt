package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.hasAllKeys
import io.github.sakaki_aruka.customcrafter.impl.util.KeyContainer
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Crafter
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.Collections
import kotlin.math.floor

internal class CBlock(
    val version: String,
    val type: CRecipeType,
    val name: String,
    val publisherName: String,
    val slots: List<Int>,
    val block: Block
) {
    companion object {
        val VERSION = KeyContainer("version", PersistentDataType.STRING)
        val TYPE = KeyContainer("type", PersistentDataType.STRING)
        val NAME = KeyContainer("name", PersistentDataType.STRING)
        val PUBLISHER = KeyContainer("publisher", PersistentDataType.STRING)
        val SLOTS = KeyContainer("slots", PersistentDataType.INTEGER_ARRAY)

        private val CONTAINED_ITEMS_MODIFY_CACHE: MutableMap<Location, Array<ItemStack>> // Map<Loc, Array<(static capacity) Item>>
            = Collections.synchronizedMap(mutableMapOf())

        fun hasEssentialKeys(crafter: Crafter): Boolean {
            return crafter.persistentDataContainer.hasAllKeys(VERSION, TYPE, NAME, PUBLISHER, SLOTS)
        }

        // Get
        fun of(crafter: Crafter): CBlock? {
            if (!CBlockDB.isLinked(crafter.block)) {
                return null
            }

            val container: PersistentDataContainer = crafter.persistentDataContainer
            if (!container.hasAllKeys(VERSION, TYPE, NAME, PUBLISHER, SLOTS)) {
                return null
            } else if (CRecipeType.of(container.get(InventoryUtil.fromKeyContainer(TYPE), TYPE.type)!!) == null) {
                return null
            }

            val cBlock = CBlock(
                version = container.get(InventoryUtil.fromKeyContainer(VERSION), VERSION.type)!!,
                type = CRecipeType.of(container.get(InventoryUtil.fromKeyContainer(TYPE), TYPE.type)!!)!!,
                name = container.get(InventoryUtil.fromKeyContainer(NAME), NAME.type)!!,
                publisherName = container.get(InventoryUtil.fromKeyContainer(PUBLISHER), PUBLISHER.type)!!,
                slots = container.get(InventoryUtil.fromKeyContainer(SLOTS), SLOTS.type)!!.toList(),
                block = crafter.block
            )

            cBlock.writeToContainer()
            return cBlock
        }
    }

    fun isPlayerModifyMode(): Boolean {
        return synchronized(CONTAINED_ITEMS_MODIFY_CACHE) {
            CONTAINED_ITEMS_MODIFY_CACHE.keys.contains(this.block.location)
        }
    }

    fun setToCache(index: Int, item: ItemStack): Boolean {
        if (index < 0) {
            throw IllegalArgumentException("'index' must be a positive number.")
        }
        val items: Array<ItemStack> = getCacheItems()?.toTypedArray() ?: run {
            throw IllegalStateException("Could not get already registered cache items. (Block=${this.block.location})")
        }
        if (index >= items.size) {
            throw IllegalArgumentException("'index' must be lesser than contained items capacity. (index=${index}, capacity=${items.size})")
        }
        synchronized(CONTAINED_ITEMS_MODIFY_CACHE) {
            CONTAINED_ITEMS_MODIFY_CACHE[this.block.location]?.set(index, item)
        }
        return true
    }

    fun removeCache(index: Int) {
        if (index < 0) return
        val items: Array<ItemStack> = getCacheItems()?.toTypedArray() ?: return
        if (index >= items.size) return
        synchronized(CONTAINED_ITEMS_MODIFY_CACHE) {
            CONTAINED_ITEMS_MODIFY_CACHE[this.block.location]?.set(index, ItemStack.empty())
        }
    }

    private fun createCache(array: Array<ItemStack>): Boolean {
        return synchronized(CONTAINED_ITEMS_MODIFY_CACHE) {
            CONTAINED_ITEMS_MODIFY_CACHE.put(this.block.location, array) == null
        }
    }

    fun getCacheItems(): List<ItemStack>? {
        val copiedEntry: Array<ItemStack> = synchronized(CONTAINED_ITEMS_MODIFY_CACHE) {
            CONTAINED_ITEMS_MODIFY_CACHE[this.block.location]
        } ?: return null

        return copiedEntry.toList()
    }

    fun enterPlayerModifyMode(): Boolean {
        if (isPlayerModifyMode()) return false
        val containedItems: List<ItemStack> = getContainedItems().takeIf { items ->
            items.isNotEmpty()
        } ?: List(this.slots.size) { ItemStack.empty() }
        return createCache(containedItems.toTypedArray())
    }

    fun exitPlayerModifyMode() {
        if (!isPlayerModifyMode()) return
        val containedItems: List<ItemStack> = getCacheItems() ?: return
        this.clearContainedItems()
        this.addItems(*containedItems.toTypedArray())
    }

    fun isSupportedVersion(): Boolean {
        val candidate: Set<String> = CustomCrafterAPI.AUTO_CRAFTING_CONFIG_COMPATIBILITIES[CustomCrafterAPI.API_VERSION]
            ?: return false
        return candidate.contains(this.version)
    }

    fun updateRecipe(recipe: AutoCraftRecipe) {
        this.getContainedItems().forEach { item ->
            if (!item.isEmpty) {
                this.block.world.dropItem(this.block.getRelative(BlockFace.DOWN, 1).location, item)
            }
        }
        CBlockDB.unlink(this.block)
        CBlockDB.linkWithoutItems(this.block, recipe)
        writeToContainer()
    }

    fun clearContainedItems() {
        CBlockDB.clearContainedItems(this.block)
    }

    fun getContainedItems(): List<ItemStack> {
        return CBlockDB.getContainedItems(this.block)
    }

    fun addItems(vararg items: ItemStack): Boolean {
        return CBlockDB.addItems(this.block, *items)
    }

    fun writeToContainer() {
        val crafter: Crafter = this.block.state as? Crafter
            ?: throw IllegalStateException("[CBlock] The specified block can not convert to 'Crafter'.")

        (0..<9).forEach { index ->
            crafter.setSlotDisabled(index, false)
        }
        crafter.update()

        crafter.persistentDataContainer.set(InventoryUtil.fromKeyContainer(VERSION), VERSION.type, this.version)
        crafter.persistentDataContainer.set(InventoryUtil.fromKeyContainer(NAME), NAME.type, this.name)
        crafter.persistentDataContainer.set(InventoryUtil.fromKeyContainer(TYPE), TYPE.type, this.type.type)
        crafter.persistentDataContainer.set(InventoryUtil.fromKeyContainer(PUBLISHER), PUBLISHER.type, this.publisherName)
        crafter.persistentDataContainer.set(InventoryUtil.fromKeyContainer(SLOTS), SLOTS.type, this.slots.toIntArray())
        crafter.update()
    }

    fun getRecipe(): AutoCraftRecipe? {
        if (!isSupportedVersion()) {
            return null
        }

        val recipe: AutoCraftRecipe = CustomCrafterAPI.getRecipes()
            .filterIsInstance<AutoCraftRecipe>()
            .filter { r -> r.publisherPluginName == this.publisherName }
            .filter { r -> r.type == this.type }
            .filter { r -> r.name == this.name }
            .firstOrNull { r -> r.items.size == this.slots.size }
            ?: return null

        return recipe
    }

    fun getDropLocation(): Location {
        return Location(
            this.block.world,
            floor(this.block.location.x) + 0.5,
            floor(this.block.location.y) - 0.5,
            floor(this.block.location.z) + 0.5
        )
    }
}
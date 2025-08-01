package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.hasAllKeys
import io.github.sakaki_aruka.customcrafter.impl.util.KeyContainer
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Crafter
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

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
        val SUPPORTED_VERSIONS_MAP: Map<String, Set<String>> = mapOf(
            "0.1.10" to setOf("0.1.10"), // v5.0.10
            "0.1.11" to setOf("0.1.11")  // v5.0.11
        )

        val RECIPE_SEARCH_CACHE: MutableMap<Block, AutoCraftRecipe> = mutableMapOf()
        val RECIPE_SEARCH_CACHE_EXPIRE_UNIX_TIMES: MutableMap<Block, Long> = mutableMapOf()
        const val RECIPE_SEARCH_CACHE_DELETE_INTERVAL_SECONDS = 600

        // Get
        fun of(crafter: Crafter): CBlock? {
            if (!CBlockDB.isLinked(crafter.block)) {
                return null
            }

            val container: PersistentDataContainer = crafter.persistentDataContainer
            if (!container.hasAllKeys(listOf(VERSION, TYPE, NAME, PUBLISHER, SLOTS))) {
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

    fun isSupportedVersion(v: String): Boolean {
        val candidate: Set<String> = SUPPORTED_VERSIONS_MAP[CustomCrafterAPI.API_VERSION]
            ?: return false
        return candidate.contains(v)
    }

    fun updateRecipe(recipe: AutoCraftRecipe) {
        this.getContainedItems().forEach { item ->
            this.block.world.dropItem(this.block.getRelative(BlockFace.DOWN, 1).location, item)
        }
        CBlockDB.unlink(this.block)
        CBlockDB.linkWithoutItems(this.block, recipe)
        writeToContainer()
    }

    fun getContainedItems(): List<ItemStack> {
        return CBlockDB.getContainedItems(this.block)
    }

    fun addItems(vararg items: ItemStack): Boolean {
        return CBlockDB.addItems(this.block, *items)
    }

    fun writeToContainer() {
        if (this.block.state !is Crafter) {
            throw IllegalStateException("[CBlock] The specified block can not convert to 'Crafter'.")
        }
        val container: PersistentDataContainer = (this.block.state as Crafter).persistentDataContainer
        container.set(InventoryUtil.fromKeyContainer(VERSION), VERSION.type, this.version)
        container.set(InventoryUtil.fromKeyContainer(NAME), NAME.type, this.name)
        container.set(InventoryUtil.fromKeyContainer(TYPE), TYPE.type, this.type.type)
        container.set(InventoryUtil.fromKeyContainer(PUBLISHER), PUBLISHER.type, this.publisherName)
        container.set(InventoryUtil.fromKeyContainer(SLOTS), SLOTS.type, this.slots.toIntArray())
        this.block.state.update()
    }

    fun getRecipe(): AutoCraftRecipe? {
        if (!isSupportedVersion(this.version)) {
            return null
        }

        if (RECIPE_SEARCH_CACHE.containsKey(this.block)) {
            RECIPE_SEARCH_CACHE_EXPIRE_UNIX_TIMES[this.block] = System.currentTimeMillis() + RECIPE_SEARCH_CACHE_DELETE_INTERVAL_SECONDS * 1000
            return RECIPE_SEARCH_CACHE[this.block]
        }

        val recipe: AutoCraftRecipe = CustomCrafterAPI.getRecipes()
            .filterIsInstance<AutoCraftRecipe>()
            .filter { r -> r.publisherPluginName == this.publisherName }
            .filter { r -> r.type == this.type }
            .filter { r -> r.name == this.name }
            .firstOrNull { r -> r.items.size == this.slots.size }
            ?: return null

        RECIPE_SEARCH_CACHE[this.block] = recipe

        object: BukkitRunnable() {
            override fun run() {
                if (!RECIPE_SEARCH_CACHE.containsKey(this@CBlock.block)) {
                    return
                }
                if (System.currentTimeMillis() >= RECIPE_SEARCH_CACHE_EXPIRE_UNIX_TIMES[this@CBlock.block]!!) {
                    RECIPE_SEARCH_CACHE.remove(this@CBlock.block)
                    RECIPE_SEARCH_CACHE_EXPIRE_UNIX_TIMES.remove(this@CBlock.block)
                }
            }
        }.runTaskLaterAsynchronously(CustomCrafter.getInstance(), RECIPE_SEARCH_CACHE_DELETE_INTERVAL_SECONDS * 20L)

        return recipe
    }
}
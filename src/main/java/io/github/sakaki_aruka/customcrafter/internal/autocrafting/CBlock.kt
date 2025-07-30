package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.hasAllKeys
import io.github.sakaki_aruka.customcrafter.impl.util.KeyContainer
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal class CBlock(
    val version: String,
    val type: CRecipeType,
    val name: String,
    val publisherName: String,
    val slots: List<Int>,
    val currentSlot: Int,
    val block: Block
) {
    companion object {
        val VERSION = KeyContainer("version", PersistentDataType.STRING)
        val TYPE = KeyContainer("type", PersistentDataType.STRING)
        val NAME = KeyContainer("name", PersistentDataType.STRING)
        val PUBLISHER = KeyContainer("publisher", PersistentDataType.STRING)
        val SLOTS = KeyContainer("slots", PersistentDataType.INTEGER_ARRAY)
        val CURRENT_SLOT = KeyContainer("current_slot", PersistentDataType.INTEGER)

        // Get
        fun of(crafter: Crafter): CBlock? {
            if (!CBlockDB.isLinked(crafter.block)) {
                return null
            }

            val container: PersistentDataContainer = crafter.persistentDataContainer
            if (!container.hasAllKeys(listOf(VERSION, TYPE, NAME, PUBLISHER, SLOTS, CURRENT_SLOT))) {
                return null
            } else if (CRecipeType.of(container.get(InventoryUtil.fromKeyContainer(TYPE), TYPE.type)!!) == null) {
                return null
            }

            return CBlock(
                version = container.get(InventoryUtil.fromKeyContainer(VERSION), VERSION.type)!!,
                type = CRecipeType.of(container.get(InventoryUtil.fromKeyContainer(TYPE), TYPE.type)!!)!!,
                name = container.get(InventoryUtil.fromKeyContainer(NAME), NAME.type)!!,
                publisherName = container.get(InventoryUtil.fromKeyContainer(PUBLISHER), PUBLISHER.type)!!,
                slots = container.get(InventoryUtil.fromKeyContainer(SLOTS), SLOTS.type)!!.toList(),
                currentSlot = container.get(InventoryUtil.fromKeyContainer(CURRENT_SLOT), CURRENT_SLOT.type)!!,
                block = crafter.block
            )
        }
    }

    fun isLinked(): Boolean {
        return CBlockDB.isLinked(this.block)
    }

    fun getContainedItems(): List<ItemStack> {
        return CBlockDB.getContainedItems(this.block)
    }

    fun addItems(vararg items: ItemStack): Boolean {
        return CBlockDB.addItems(this.block, *items)
    }

//    fun getRecipe(): AutoCraftRecipe? {
//        return CustomCrafterAPI.getRecipes()
//            .filterIsInstance<AutoCraftRecipe>()
//            .filter { r ->  }
//    }
}
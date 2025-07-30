package io.github.sakaki_aruka.customcrafter.impl.util

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.io.Serializable

object InventoryUtil {

    internal fun PersistentDataContainer.hasAllKeys(keys: Collection<KeyContainer<out Serializable, out Serializable>>): Boolean {
        for (key in keys) {
            if (!this.has(fromKeyContainer(key), key.type)) {
                return false
            }
        }
        return true
    }

    internal fun <T, U> fromKeyContainer(key: KeyContainer<T, U>): NamespacedKey {
        return NamespacedKey(CustomCrafter.getInstance(), key.key)
    }

    fun disassemble(item: ItemStack): List<ItemStack> {
        val result: MutableList<ItemStack> = mutableListOf()
        val times: Int = item.amount / item.maxStackSize
        val remaining: Int = item.amount % item.maxStackSize

        if (times > 0) {
            repeat(times) {
                result.add(item.asQuantity(item.maxStackSize))
            }
        }

        if (remaining > 0) {
            result.add(item.asQuantity(remaining))
        }
        return result.toList()
    }

    fun addToItemList(item: ItemStack, list: List<ItemStack>): Pair<List<ItemStack>, List<ItemStack>> {
        val base: MutableList<ItemStack> = mutableListOf()
        val cloned: ItemStack = item.clone()
        for (i: ItemStack in list) {
            if (i.amount >= i.maxStackSize) {
                base.add(i)
                continue
            }
            val canAdd: Int = i.maxStackSize - i.amount
            if (cloned.amount <= canAdd) {
                base.add(i.asQuantity(i.amount + item.amount))
                continue
            }

            base.add(i.asQuantity(i.maxStackSize))
            cloned.amount = cloned.amount - canAdd
        }

        return if (cloned.amount == 0) {
            base.toList() to emptyList()
        } else {
            base.toList() to disassemble(cloned)
        }
    }

    fun compress(items: List<ItemStack>): List<ItemStack> {
        val stackLimitOver: MutableMap<ItemStack, Int> = mutableMapOf()
        for (item: ItemStack in items) {
            val similar: ItemStack? = stackLimitOver.keys.find { i -> i.isSimilar(item.asOne()) }
            if (similar == null) {
                stackLimitOver[item.asOne()] = item.amount
                continue
            }
            stackLimitOver[similar] = stackLimitOver[similar]!! + item.amount
        }
        val result: MutableList<ItemStack> = mutableListOf()
        stackLimitOver.forEach { (key, amount) ->
            result.addAll(disassemble(key.asQuantity(amount)))
        }
        return result
    }

    fun Player.giveItems(
        saveLimit: Boolean = true,
        vararg items: ItemStack
    ) {
        items.forEach { item: ItemStack ->
            if (item.amount <= item.type.maxStackSize || !saveLimit) {
                addToInventory(item, this)
            } else {
                val one: ItemStack = item.asOne()
                val q: Int = item.amount / item.type.maxStackSize
                val r: Int = item.amount % item.type.maxStackSize
                repeat(q) {
                    addToInventory(one.asQuantity(item.type.maxStackSize), this)
                }
                addToInventory(one.asQuantity(r), this)
            }
        }
    }

    private fun addToInventory(
        item: ItemStack,
        player: Player
    ) {
        player.inventory.addItem(item).forEach { (_, over) ->
            player.world.dropItem(player.location, over)
        }
    }
}
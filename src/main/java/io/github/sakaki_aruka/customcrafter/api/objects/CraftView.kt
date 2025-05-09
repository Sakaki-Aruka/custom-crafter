package io.github.sakaki_aruka.customcrafter.api.objects

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import kotlinx.serialization.json.Json
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.Base64
import kotlin.math.max

/**
 * A view of crafting gui.
 *
 * @param[materials] A mapping of materials what were placed by a player.
 * @param[result] An item that is placed the result item slot.
 */

data class CraftView internal constructor(
    val materials: Map<CoordinateComponent, ItemStack>,
    val result: ItemStack
) {
    companion object {
        /**
         * converting an [Inventory] to [CraftView].
         * ```
         * // an example from Java.
         * Inventory gui = CustomCrafterAPI.getCraftingGUI();
         * CraftView view = CraftView.fromInventory(gui, true);
         *
         * // an example from Kotlin
         * val gui = CustomCrafterAPI.getCraftingGUI()
         * val view = CraftView.fromInventory(gui)
         * ```
         *
         * @param[inventory] convert target
         * @param[paddingAir] padding empty slots with [ItemStack.empty] or not. default value is true. (default value can only use from Kotlin.)
         * @return[CraftView?] A result of converting. If a provided inventory is not custom crafter's gui, returns Null.
         */
        fun fromInventory(
            inventory: Inventory,
            paddingAir: Boolean = true
        ): CraftView? {
            val mapped: MutableMap<CoordinateComponent, ItemStack> = Converter.standardInputMapping(inventory)?.toMutableMap() ?: return null
            if (paddingAir) {
                Converter.getAvailableCraftingSlotComponents()
                    .filter { !mapped.keys.contains(it) }
                    .forEach { coordinate ->
                        mapped[coordinate] = ItemStack.empty()
                    }
            }
            val result: ItemStack = inventory.getItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT) ?: ItemStack.empty()
            return CraftView(mapped, result)
        }

        /**
         * decodes a json string to CraftView
         *
         * @param[json] an input json string
         * @return[CraftView] a deserialized CraftView
         * @since 5.0.8
         */
        fun fromJson(json: String): CraftView {
            val map: Map<Int, String> = Json.decodeFromString(json)
            val deserialized: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
            map.forEach { (index, item) ->
                val itemByteArray: ByteArray = Base64.getDecoder().decode(item)
                deserialized[CoordinateComponent.fromIndex(index, followLimit = false)] = ItemStack.deserializeBytes(itemByteArray)
            }
            val result: ItemStack =
                if (Int.MAX_VALUE in map.keys) {
                    ItemStack.deserializeBytes(Base64.getDecoder().decode(map[Int.MAX_VALUE]))
                }
                else ItemStack.empty()
            return CraftView(deserialized, result)
        }
    }

    /**
     * converts a view to Custom Crafter's gui.
     *
     * @param[dropItemsOnClose] CustomCrafterAPI#getCraftingGUI 'dropItemsOnClose' (default = false, since 5.0.8)
     * @see[CustomCrafterAPI.getCraftingGUI]
     * @return[Inventory] Custom Crafter's gui
     */
    fun toCraftingGUI(
        dropItemsOnClose: Boolean = false
    ): Inventory {
        val gui: Inventory = CustomCrafterAPI.getCraftingGUI(dropItemsOnClose = dropItemsOnClose)
        this.materials.entries.forEach { (c, item) ->
            gui.setItem(c.x + c.y * 9, item)
        }
        gui.setItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT, this.result)
        return gui
    }

    /**
     * converts a view to [ByteArray] (ByteArray equals byte[] in Java)
     *
     * @return[String] a serialized CraftView string
     * @since 5.0.8
     */
    fun toJson(): String {
        val converted: MutableMap<Int, String> = mutableMapOf()
        for (index in (0..<54)) {
            val item: ItemStack = this.materials[CoordinateComponent.fromIndex(index, followLimit = false)]
                .takeIf { i -> i?.type != Material.AIR } ?: continue
            converted[index] = Base64.getEncoder().encodeToString(item.serializeAsBytes())
        }

        this.result
            .takeIf { i -> i.type != Material.AIR }
            ?.let { result ->
                converted[Int.MAX_VALUE] = Base64.getEncoder().encodeToString(result.serializeAsBytes())
            }
        return Json.encodeToString(converted)
    }

    /**
     * decrement items from the provided CraftView
     *
     * if [forCustomSettings] is not null, this runs a process for CRecipe.
     *
     * only for Shift clicked
     *
     * @param[view] current CraftView
     * @param[shiftUsed] a crafter used shift-click or not
     * @param[forCustomSettings] a matched result info. (requires these when matched custom recipe)
     * @since 5.0.8
     */
    fun getDecrementedCraftView(
        shiftUsed: Boolean = true,
        forCustomSettings: Pair<CRecipe, MappedRelation>? = null
    ): CraftView {
        val minAmount: Int = this.materials.minOf { (_, i) -> i.amount }
        return forCustomSettings?.let { (cRecipe, mapped) ->
            val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
            mapped.components.forEach { component ->
                val matter: CMatter = cRecipe.items[component.recipe]!!
                val isMass: Boolean = matter.mass
                val decrementAmount: Int =
                    if (isMass) 1
                    else if (shiftUsed) (minAmount / matter.amount) * matter.amount
                    else matter.amount
                val newAmount: Int = max(0, this.materials[component.input]!!.amount - decrementAmount)
                map[component.input] =
                    if (newAmount == 0) ItemStack.empty()
                    else let {
                        val newItem: ItemStack = this.materials[component.input]?.clone() ?: ItemStack.empty()
                        newItem.amount = newAmount
                        newItem
                    }
            }
            CraftView(map, ItemStack.empty())
        } ?: run {
            val map: MutableMap<CoordinateComponent, ItemStack> = mutableMapOf()
            this.materials.forEach { (c, item) ->
                val newAmount: Int = max(0, item.amount - if (shiftUsed) minAmount else 1)
                map[c] =
                    if (newAmount == 0) ItemStack.empty()
                    else let {
                        val newItem: ItemStack = item.clone()
                        newItem.amount = newAmount
                        newItem
                    }
            }
            CraftView(map, ItemStack.empty())
        }
    }
}

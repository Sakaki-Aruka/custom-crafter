package com.github.sakakiaruka.customcrafter.customcrafter.api

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test.APITest
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test.ConverterTest
import com.github.sakakiaruka.customcrafter.customcrafter.api.active_test.test.EnchantTest
import com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.recipe.CRecipe
import com.github.sakakiaruka.customcrafter.customcrafter.api.listener.InventoryClickListener
import com.github.sakakiaruka.customcrafter.customcrafter.api.listener.InventoryCloseListener
import com.github.sakakiaruka.customcrafter.customcrafter.api.listener.PlayerInteractListener
import com.github.sakakiaruka.customcrafter.customcrafter.api.processor.Converter
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object CustomCrafterAPI {
    const val VERSION: String = "0.1"
    const val IS_STABLE: Boolean = false
    const val IS_BETA: Boolean = true

    val AUTHORS: Set<String> = setOf("Sakaki-Aruka")

    var RESULT_GIVE_CANCEL: Boolean = false
    var ENABLE_HISTORY_DATABASE = false
    val RECIPES: MutableList<CRecipe> = mutableListOf()
    var BASE_BLOCK: Material = Material.GOLD_BLOCK

    internal var BASE_BLOCK_SIDE: Int = 3
    const val CRAFTING_TABLE_MAKE_BUTTON_SLOT: Int = 35
    const val CRAFTING_TABLE_RESULT_SLOT: Int = 44
    const val CRAFTING_TABLE_TOTAL_SIZE: Int = 54

    internal fun setup() {
        val instance: CustomCrafter = CustomCrafter.getInstance()
        Bukkit.getPluginManager().registerEvents(InventoryClickListener, instance)
        Bukkit.getPluginManager().registerEvents(InventoryCloseListener, instance)
        Bukkit.getPluginManager().registerEvents(PlayerInteractListener, instance)

        if (IS_BETA) {
            // run tests
            APITest.run()
            ConverterTest.run()
            EnchantTest.run()
        }
    }

    /**
     * set base block's side size.
     * default size = 3.
     *
     * @param[size] this argument must be odd and more than zero.
     * @return[Boolean] if successful to change, returns true else false.
     */
    fun setBaseBlockSideSize(size: Int): Boolean {
        if (size <= 0 || size % 2 != 1) return false
        BASE_BLOCK_SIDE = size
        return true
    }

    /**
     * get base block's side size.
     *
     * @return[Int] size
     */
    fun getBaseBlockSideSize(): Int = BASE_BLOCK_SIDE

    /**
     * provides elements of custom crafter's gui component
     * returned Triple contained below elements.
     * first([NamespacedKey]): "custom_crafter:gui_created"
     * second([PersistentDataType.LONG]): a type of 'third'
     * third([Long]): epoch time when called this.
     *
     * @return[Triple]
     */
    fun genCCKey() = Triple(
        NamespacedKey(CustomCrafter.getInstance(), "gui_created"),
        PersistentDataType.LONG,
        System.currentTimeMillis()
    )
    private val blank = ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply {
            displayName(Component.empty())
        }
    }
    private val makeButton = ItemStack(Material.ANVIL).apply {
        itemMeta = itemMeta.apply {
            displayName(Component.text("Making items"))
            val key = genCCKey()
            persistentDataContainer.set(key.first, key.second, key.third)
        }
    }

    /**
     * returns custom crafter gui
     *
     * @return[Inventory] custom crafter gui
     */
    fun getCraftingGUI(): Inventory {
        val gui: Inventory = Bukkit.createInventory(null, CRAFTING_TABLE_TOTAL_SIZE, Component.text("Custom Crafter"))
        (0..<54).forEach { slot -> gui.setItem(slot, blank) }
        Converter.getAvailableCraftingSlotComponents().forEach { c ->
            val index: Int = c.x + c.y * 9
            gui.setItem(index, ItemStack.empty())
        }
        gui.setItem(CRAFTING_TABLE_MAKE_BUTTON_SLOT, makeButton)
        return gui
    }

    /**
     * returns the provided inventory is custom crafter gui or not.
     *
     * @param[inventory] input inventory
     * @return[Boolean] is custom crafter gui or not
     */
    fun isCustomCrafterGUI(inventory: Inventory): Boolean {
        if (inventory.size != 54) return false
        val makeButton: ItemStack = inventory.getItem(CRAFTING_TABLE_MAKE_BUTTON_SLOT)
            ?.takeIf { it.type == makeButton.type }
            ?: return false
        val key = genCCKey()
        return makeButton.itemMeta.persistentDataContainer.has(key.first, key.second)
    }

    /**
     * returns the provided inventory is OLDER than custom crafter reloaded or enabled or not.
     * if you provide an inventory what is not a custom crafter gui, this throws an Exception.
     *
     * @param[inventory] provided inventory
     * @throws[IllegalArgumentException] thrown when the provided is not custom crafter gui
     * @throws[IllegalStateException] thrown when get an error on get gui created epoch time
     * @return[Boolean] older or not
     */
    fun isGUITooOld(inventory: Inventory): Boolean {
        if (!isCustomCrafterGUI(inventory)) throw IllegalArgumentException("'inventory' must be a CustomCrafter's gui.")
        val button: ItemStack = inventory.getItem(CRAFTING_TABLE_MAKE_BUTTON_SLOT)!!
        val key = genCCKey()
        val time: Long = button.itemMeta.persistentDataContainer.get(key.first, key.second)
            ?: throw IllegalStateException("'time' not found. (Internal Error)")
        return time < CustomCrafter.INITIALIZED
    }
}
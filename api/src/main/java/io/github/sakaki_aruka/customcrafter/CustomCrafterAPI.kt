package io.github.sakaki_aruka.customcrafter

import io.github.sakaki_aruka.customcrafter.api.event.CustomCrafterAPIPropertiesChangeEvent
import io.github.sakaki_aruka.customcrafter.api.event.RegisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.event.UnregisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

object CustomCrafterAPI {
    /**
     * Custom Crafter API version.
     */
    const val API_VERSION: String = "5.0.18"

    /**
     * Custom Crafter API is stable or not.
     */
    const val IS_STABLE: Boolean = false

    /**
     * Custom Crafter API is beta or not.
     */
    const val IS_BETA: Boolean = true

    /**
     * Custom Crafter API author names
     */
    @JvmField
    val AUTHORS: Set<String> = setOf("Sakaki-Aruka")

    /**
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RESULT_GIVE_CANCEL]
     */
    private var RESULT_GIVE_CANCEL: AtomicBoolean = AtomicBoolean(false)

    /**
     * Returns a boolean value that means the Custom Crafter API give result items to players or not.
     * @return[Boolean] Give or not
     * @since 5.0.11
     */
    @JvmStatic
    fun getResultGiveCancel(): Boolean = RESULT_GIVE_CANCEL.get()

    /**
     * Sets a boolean value that means the Custom Crafter API give result items to players or not.
     *
     * You have to set this to true if you want to give result items processing in your plugin.
     *
     * Default = `false`
     * @param[v] Give or not
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.11
     */
    @JvmStatic
    @JvmOverloads
    fun setResultGiveCancel(v: Boolean, calledAsync: Boolean = false) {
        val currentValue: Boolean = RESULT_GIVE_CANCEL.getAndSet(v)
        if (currentValue != v) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RESULT_GIVE_CANCEL.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(v),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * Sets `ResultGiveCancel` to false (default value).
     * @since 5.0.13
     */
    @JvmStatic
    @JvmOverloads
    fun setResultGiveCancelDefault(calledAsync: Boolean = false) {
        if (RESULT_GIVE_CANCEL.getAndSet(false)) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RESULT_GIVE_CANCEL.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(true),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(false),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK]
     */
    private var BASE_BLOCK: AtomicReference<Material> = AtomicReference(Material.GOLD_BLOCK)
    /**
     * Gets a base block type.
     * @return[Material] base block type
     * @since 5.0.9
     */
    @JvmStatic
    fun getBaseBlock(): Material = BASE_BLOCK.get()

    /**
     * Sets base block with given material.
     *
     * If a given material is not a block type, throws [IllegalArgumentException].
     * @param[type] base block type
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @throws[IllegalArgumentException] when specified not a block type
     * @since 5.0.9
     */
    @JvmStatic
    @JvmOverloads
    fun setBaseBlock(type: Material, calledAsync: Boolean = false) {
        if (!type.isBlock || type.isAir) {
            throw IllegalArgumentException("'type' must meet 'Material#isBlock'.")
        }

        val currentValue: Material = BASE_BLOCK.getAndSet(type)
        if (type != currentValue) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(type),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * Sets `BaseBlock` to `Material.GOLD_BLOCK` (default value).
     * @since 5.0.13
     */
    @JvmStatic
    @JvmOverloads
    fun setBaseBlockDefault(calledAsync: Boolean = false) {
        val currentValue: Material = BASE_BLOCK.getAndSet(Material.GOLD_BLOCK)
        if (currentValue != Material.GOLD_BLOCK) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(Material.GOLD_BLOCK),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * use 'multiple result candidate' feature or not.
     * - true: if the system gets some result candidates, shows all candidates to a player.
     * - false: provides only a first matched item. (no prompt)
     * @suppress
     * @since 5.0.8
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE]
     */
    private var USE_MULTIPLE_RESULT_CANDIDATE_FEATURE: AtomicBoolean = AtomicBoolean(false)

    /**
     * Returns a boolean value that means 'multiple result candidate' feature enabled or not.
     *
     * - `true`: If the system gets some result candidates, shows all candidates to a player.
     * - `false`: The API provides only a first matched item. (no prompt)
     * @return[Boolean] Enabled or not
     * @since 5.0.11 (original 5.0.8)
     */
    @JvmStatic
    fun getUseMultipleResultCandidateFeature(): Boolean = USE_MULTIPLE_RESULT_CANDIDATE_FEATURE.get()

    /**
     * Sets 'multiple result candidate' feature enables or not.
     *
     * - `true`: If the system gets some result candidates, shows all candidates to a player.
     * - `false`: The API provides only a first matched item. (no prompt)
     * @param[v] Multiple result candidate feature enable or not
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.11 (original 5.0.8)
     */
    @JvmStatic
    @JvmOverloads
    fun setUseMultipleResultCandidateFeature(v: Boolean, calledAsync: Boolean = false) {
        val currentValue: Boolean = USE_MULTIPLE_RESULT_CANDIDATE_FEATURE.getAndSet(v)
        if (v != currentValue) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(v),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * Sets `useMultipleResultCandidateFeature` to false (default value).
     * @since 5.0.13
     */
    @JvmStatic
    @JvmOverloads
    fun setUseMultipleResultCandidateFeatureDefault(calledAsync: Boolean = false) {
        if (USE_MULTIPLE_RESULT_CANDIDATE_FEATURE.getAndSet(false)) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(true),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(false),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    private var USE_CUSTOM_CRAFT_UI = AtomicBoolean(true)
    /**
     * Returns a boolean value that means 'Custom Craft UI open' enabled or not.
     * @return[Boolean] Enabled or not
     * @since 5.0.13-1
     */
    @JvmStatic
    fun getUseCustomCraftUI(): Boolean = USE_CUSTOM_CRAFT_UI.get()

    /**
     * Sets 'Custom Craft UI open' enables or not.
     *
     * @param[v] Enable or not
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.13-1
     */
    @JvmStatic
    @JvmOverloads
    fun setUseCustomCraftUI(v: Boolean, calledAsync: Boolean = false) {
        val currentValue: Boolean = USE_CUSTOM_CRAFT_UI.getAndSet(v)
        if (v != currentValue) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(v),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * Sets 'Custom Craft UI open' enable.
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.13-1
     */
    @JvmStatic
    @JvmOverloads
    fun setUseCustomCraftUIDefault(calledAsync: Boolean = false) {
        if (!USE_CUSTOM_CRAFT_UI.getAndSet(true)) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(false),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(true),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * Checks full-compatibility
     *
     * ```kotlin
     * // Check Example in your plugin
     * @Override
     * fun onEnable() {
     *     if (!CustomCrafterAPI.hasFullCompatibility("5.0.15")) {
     *         println("This plugin has not full-compatibility with loaded CustomCrafter.")
     *         println("Loaded Version: ${CustomCrafterAPI.API_VERSION}")
     *         Bukkit.pluginManager.disablePlugin(this)
     *         return
     *     }
     * }
     * ```
     *
     * @param[version] CustomCrafter version string that is used by your plugin.
     * @since 5.0.13
     */
    @JvmStatic
    fun hasFullCompatibility(version: String): Boolean {
        return version in setOf("5.0.17-p2", "5.0.18")
    }


    /**
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE]
     */
    private var BASE_BLOCK_SIDE = AtomicInteger(3)//3
    /**
     * set base block's side size.
     *
     * default size = 3.
     *
     * @param[size] this argument must be odd and more than zero.
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @return[Boolean] if successful to change, returns true else false.
     */
    @JvmStatic
    @JvmOverloads
    fun setBaseBlockSideSize(size: Int, calledAsync: Boolean = false): Boolean {
        if (size < 3 || size % 2 != 1) {
            return false
        }
        val currentValue: Int = BASE_BLOCK_SIDE.getAndSet(size)
        if (size != currentValue) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(size),
                isAsync = calledAsync
            ).callEvent()
        }
        return true
    }

    /**
     * get base block's side size.
     *
     * @return[Int] size
     */
    @JvmStatic
    fun getBaseBlockSideSize(): Int = BASE_BLOCK_SIDE.get()

    /**
     * Sets base block's side size to 3 (default value).
     * @since 5.0.13
     */
    @JvmStatic
    @JvmOverloads
    fun setBaseBlockSideSizeDefault(calledAsync: Boolean = false) {
        val currentValue: Int = BASE_BLOCK_SIDE.getAndSet(3)
        if (currentValue != 3) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(3),
                isAsync = calledAsync
            ).callEvent()
        }
    }


    private var CRAFT_UI_DESIGNER: AtomicReference<CraftUIDesigner> = AtomicReference(CraftUI)
    /**
     * Returns a current CraftUI designer.
     * @return[CraftUIDesigner]
     * @since 5.0.16
     */
    @JvmStatic
    fun getCraftUIDesigner(): CraftUIDesigner = CRAFT_UI_DESIGNER.get()

    /**
     * Sets a new CraftUI designer if a specified is valid.
     * @param[designer] New CraftUI designer
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.16
     */
    @JvmStatic
    @JvmOverloads
    fun setCraftUIDesigner(designer: CraftUIDesigner, calledAsync: Boolean = false) {
        val nullContext = CraftUIDesigner.Context(player = null)
        val baked: CraftUIDesigner.Baked = CraftUIDesigner.bake(designer, nullContext)
        baked.isValid().exceptionOrNull()?.let { throw it }

        val currentValue: CraftUIDesigner = CRAFT_UI_DESIGNER.getAndSet(designer)
        CustomCrafterAPIPropertiesChangeEvent(
            propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.CRAFT_UI_DESIGNER.name,
            oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
            newValue = CustomCrafterAPIPropertiesChangeEvent.Property(designer),
            isAsync = calledAsync
        ).callEvent()
    }

    /**
     * Sets the default CraftUI designer.
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.16
     */
    @JvmStatic
    @JvmOverloads
    fun setCraftUIDesignerDefault(calledAsync: Boolean = false) {
        val currentValue: CraftUIDesigner = CRAFT_UI_DESIGNER.getAndSet(CraftUI)
        CustomCrafterAPIPropertiesChangeEvent(
            propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.CRAFT_UI_DESIGNER.name,
            oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(currentValue),
            newValue = CustomCrafterAPIPropertiesChangeEvent.Property(CraftUI),
            isAsync = calledAsync
        ).callEvent()
    }

    /**
     * An item that is used for an all-candidates-menu's not displayable items slot.
     *
     * To get, use [getAllCandidateNotDisplayableItem].
     *
     * To set, use [setAllCandidateNotDisplayableItem].
     *
     * @suppress
     * @since 5.0.9
     */
    internal var ALL_CANDIDATE_NO_DISPLAYABLE_ITEM = defaultAllCandidateNotDisplayableItems()

    /**
     * used for All Candidate feature (not displayable item lore generator)
     *
     * @suppress
     * @since 5.0.9
     */
    internal var ALL_CANDIDATE_NO_DISPLAYABLE_ITEM_LORE_SUPPLIER: (String) -> List<Component>? = { recipeName ->
        listOf(MiniMessage.miniMessage().deserialize("<white>Recipe Name: $recipeName"))
    }


    /**
     * returns an IMMUTABLE list what contains all registered recipes.
     *
     * NOTICE: it is immutable, so you cannot modify its components.
     *
     * @return[List]<[CRecipe]> recipes list
     */
    @JvmStatic
    fun getRecipes(): List<CRecipe> {
        return synchronized(CustomCrafter.RECIPES) {
            CustomCrafter.RECIPES.toList()
        }
    }

    /**
     * registers a provided recipe and calls [RegisterCustomRecipeEvent].
     *
     * if a called event is cancelled, always fail to register recipe.
     *
     * in normally, a result of `RECIPES.add(recipe)`.
     *
     * @param[recipes] a recipe what you want to register.
     * @throws[IllegalStateException] Calls when the specified recipe is invalid
     */
    @JvmStatic
    fun registerRecipe(vararg recipes: CRecipe): Boolean {
        if (recipes.any { it.isValidRecipe().isFailure }) {
            val builder = StringBuilder()
            builder.append(System.lineSeparator())
            recipes.forEach { recipe ->
                recipe.isValidRecipe().exceptionOrNull()?.let { e ->
                    val name: String = recipe.name.takeIf { name -> name.isNotBlank() } ?: "(Empty Name)"
                    builder.append("Recipe: $name, Error: ${e.message ?: "(Empty Error Message)"}")
                    builder.append(System.lineSeparator())
                }
            }
            throw IllegalStateException(builder.toString())
        }
        if (!RegisterCustomRecipeEvent(recipes.toList()).callEvent()) {
            return false
        }
        return synchronized(CustomCrafter.RECIPES) {
            CustomCrafter.RECIPES.addAll(recipes)
        }
    }

    /**
     * unregisters a provided recipe and calls [UnregisterCustomRecipeEvent].
     *
     * if a called event is cancelled, always fail to unregister recipe.
     *
     * in normally, a result of `RECIPES.remove(recipe)`
     *
     * @param[recipes] a recipe what you want to unregister.
     */
    @JvmStatic
    fun unregisterRecipe(vararg recipes: CRecipe): Boolean {
        val event = UnregisterCustomRecipeEvent(recipes.toList())
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return false
        }
        return synchronized(CustomCrafter.RECIPES) {
            CustomCrafter.RECIPES.removeAll(recipes)
        }
    }

    /**
     * Unregisters all registered recipes.
     *
     * @return[Boolean] Unregistering successful or failed
     * @since 5.0.16
     */
    @JvmStatic
    fun unregisterAllRecipes(): Boolean {
        val event = UnregisterCustomRecipeEvent(getRecipes())
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return false
        }
        synchronized(CustomCrafter.RECIPES) {
            CustomCrafter.RECIPES.clear()
        }
        return true
    }

    /**
     * Get an item that is used for an all-candidates-menu's not displayable items slot.
     * @return[ItemStack] An item what is displayed when no displayable items on all-candidates-menu.
     * @since 5.0.9
     */
    @JvmStatic
    fun getAllCandidateNotDisplayableItem() = synchronized(ALL_CANDIDATE_NO_DISPLAYABLE_ITEM) {
        ALL_CANDIDATE_NO_DISPLAYABLE_ITEM
    }

    /**
     * Set an item that is used for an all-candidates-menu's not displayable items slot.
     * If the specified items material is not `Material#isItem`, this throws Errors.
     *
     * `loreSupplier` must receive a recipe-name and return a lore-list.
     *
     * A default loreSupplier is
     * ```
     * // This is a very simple lore supplier.
     * val supplier: (String) -> List<Component>? = { recipeName ->
     *         listOf(MiniMessage.miniMessage().deserialize("<white>Recipe Name: $recipeName"))
     *     }
     * ```
     * If this receives "Janssons frestelse", returns a white character component is "Recipe Name: Janssons frestelse".
     *
     * And also, you can set null to this.
     * If you did, an all-candidates-menu's not displayable item does not show lore.
     *
     *
     * @param[item] an item
     * @param[loreSupplier] a lore supplier
     * @throws[IllegalArgumentException] If the provided items material is not `Material#isItem`, thrown.
     * @since 5.0.9
     */
    @JvmStatic
    fun setAllCandidateNotDisplayableItem(
        item: ItemStack,
        loreSupplier: (String) -> List<Component>?
    ) {
        if (!item.type.isItem) {
            throw IllegalArgumentException("'item' material must be 'Material#isItem'.")
        }
        synchronized(ALL_CANDIDATE_NO_DISPLAYABLE_ITEM) {
            ALL_CANDIDATE_NO_DISPLAYABLE_ITEM = item
        }
        synchronized(ALL_CANDIDATE_NO_DISPLAYABLE_ITEM_LORE_SUPPLIER) {
            ALL_CANDIDATE_NO_DISPLAYABLE_ITEM_LORE_SUPPLIER = loreSupplier
        }
    }

    /**
     * Get an item that is used for an all-candidates-menu's not displayable items slot in default.
     * @return[ItemStack] an item
     * @since 5.0.9
     */
    @JvmStatic
    fun defaultAllCandidateNotDisplayableItems(): ItemStack {
        val item = ItemStack(Material.COMMAND_BLOCK)
        item.editMeta { meta ->
            meta.displayName(MiniMessage.miniMessage().deserialize("<red>Not Displayable Item"))
        }
        return item
    }
}
package io.github.sakaki_aruka.customcrafter

import io.github.sakaki_aruka.customcrafter.api.event.CustomCrafterAPIPropertiesChangeEvent
import io.github.sakaki_aruka.customcrafter.api.event.RegisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.event.UnregisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CustomCrafterAPI {
    /**
     * Custom Crafter API version.
     */
    const val API_VERSION: String = "5.0.15"

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
    val AUTHORS: Set<String> = setOf("Sakaki-Aruka")

    /**
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RESULT_GIVE_CANCEL]
     */
    private var RESULT_GIVE_CANCEL: Boolean = false

    /**
     * Returns a boolean value that means the Custom Crafter API give result items to players or not.
     * @return[Boolean] Give or not
     * @since 5.0.11
     */
    fun getResultGiveCancel(): Boolean = RESULT_GIVE_CANCEL

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
    fun setResultGiveCancel(v: Boolean, calledAsync: Boolean = false) {
        if (RESULT_GIVE_CANCEL != v) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RESULT_GIVE_CANCEL.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(RESULT_GIVE_CANCEL),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(v),
                isAsync = calledAsync
            ).callEvent()
        }
        RESULT_GIVE_CANCEL = v
    }

    /**
     * Sets `ResultGiveCancel` to false (default value).
     * @since 5.0.13
     */
    fun setResultGiveCancelDefault(calledAsync: Boolean = false) {
        if (RESULT_GIVE_CANCEL) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RESULT_GIVE_CANCEL.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(RESULT_GIVE_CANCEL),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(false),
                isAsync = calledAsync
            ).callEvent()
        }
        RESULT_GIVE_CANCEL = false
    }

    /**
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK]
     */
    private var BASE_BLOCK: Material = Material.GOLD_BLOCK
    /**
     * Gets a base block type.
     * @return[Material] base block type
     * @since 5.0.9
     */
    fun getBaseBlock(): Material = BASE_BLOCK

    /**
     * Sets base block with given material.
     *
     * If a given material is not a block type, throws [IllegalArgumentException].
     * @param[type] base block type
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @throws[IllegalArgumentException] when specified not a block type
     * @since 5.0.9
     */
    fun setBaseBlock(type: Material, calledAsync: Boolean = false) {
        if (!type.isBlock || type.isAir) {
            throw IllegalArgumentException("'type' must meet 'Material#isBlock'.")
        }

        if (type != BASE_BLOCK) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Material>(BASE_BLOCK),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Material>(type),
                isAsync = calledAsync
            ).callEvent()
        }
        BASE_BLOCK = type
    }

    /**
     * Sets `BaseBlock` to `Material.GOLD_BLOCK` (default value).
     * @since 5.0.13
     */
    fun setBaseBlockDefault(calledAsync: Boolean = false) {
        if (BASE_BLOCK != Material.GOLD_BLOCK) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Material>(BASE_BLOCK),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Material>(Material.GOLD_BLOCK),
                isAsync = calledAsync
            ).callEvent()
        }
        BASE_BLOCK = Material.GOLD_BLOCK
    }

    /**
     * use 'multiple result candidate' feature or not.
     * - true: if the system gets some result candidates, shows all candidates to a player.
     * - false: provides only a first matched item. (no prompt)
     * @suppress
     * @since 5.0.8
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE]
     */
    private var USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = false

    /**
     * Returns a boolean value that means 'multiple result candidate' feature enabled or not.
     *
     * - `true`: If the system gets some result candidates, shows all candidates to a player.
     * - `false`: The API provides only a first matched item. (no prompt)
     * @return[Boolean] Enabled or not
     * @since 5.0.11 (original 5.0.8)
     */
    fun getUseMultipleResultCandidateFeature(): Boolean = USE_MULTIPLE_RESULT_CANDIDATE_FEATURE

    /**
     * Sets 'multiple result candidate' feature enables or not.
     *
     * - `true`: If the system gets some result candidates, shows all candidates to a player.
     * - `false`: The API provides only a first matched item. (no prompt)
     * @param[v] Multiple result candidate feature enable or not
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.11 (original 5.0.8)
     */
    fun setUseMultipleResultCandidateFeature(v: Boolean, calledAsync: Boolean = false) {
        if (v != USE_MULTIPLE_RESULT_CANDIDATE_FEATURE) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(USE_MULTIPLE_RESULT_CANDIDATE_FEATURE),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(v),
                isAsync = calledAsync
            ).callEvent()
        }
        USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = v
    }

    /**
     * Sets `useMultipleResultCandidateFeature` to false (default value).
     * @since 5.0.13
     */
    fun setUseMultipleResultCandidateFeatureDefault(calledAsync: Boolean = false) {
        if (USE_MULTIPLE_RESULT_CANDIDATE_FEATURE) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(USE_MULTIPLE_RESULT_CANDIDATE_FEATURE),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(false),
                isAsync = calledAsync
            ).callEvent()
        }
        USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = false
    }

    private var USE_CUSTOM_CRAFT_UI = true
    /**
     * Returns a boolean value that means 'Custom Craft UI open' enabled or not.
     * @return[Boolean] Enabled or not
     * @since 5.0.13-1
     */
    fun getUseCustomCraftUI(): Boolean = USE_CUSTOM_CRAFT_UI

    /**
     * Sets 'Custom Craft UI open' enables or not.
     *
     * @param[v] Enable or not
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.13-1
     */
    fun setUseCustomCraftUI(v: Boolean, calledAsync: Boolean = false) {
        if (v != USE_CUSTOM_CRAFT_UI) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(USE_CUSTOM_CRAFT_UI),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(v),
                isAsync = calledAsync
            ).callEvent()
        }
        USE_CUSTOM_CRAFT_UI = v
    }

    /**
     * Sets 'Custom Craft UI open' enable.
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @since 5.0.13-1
     */
    fun setUseCustomCraftUIDefault(calledAsync: Boolean = false) {
        if (!USE_CUSTOM_CRAFT_UI) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_CUSTOM_CRAFT_UI.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(USE_CUSTOM_CRAFT_UI),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Boolean>(true),
                isAsync = calledAsync
            ).callEvent()
        }
        USE_CUSTOM_CRAFT_UI = true
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
    fun hasFullCompatibility(version: String): Boolean {
        return version in setOf("5.0.15")
    }


    /**
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE]
     */
    private var BASE_BLOCK_SIDE: Int = 3
    /**
     * set base block's side size.
     *
     * default size = 3.
     *
     * @param[size] this argument must be odd and more than zero.
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @return[Boolean] if successful to change, returns true else false.
     */
    fun setBaseBlockSideSize(size: Int, calledAsync: Boolean = false): Boolean {
        if (size < 3 || size % 2 != 1) return false
        if (size != BASE_BLOCK_SIDE) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Int>(BASE_BLOCK_SIDE),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Int>(size),
                isAsync = calledAsync
            ).callEvent()
        }
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
     * Sets base block's side size to 3 (default value).
     * @since 5.0.13
     */
    fun setBaseBlockSideSizeDefault(calledAsync: Boolean = false) {
        if (BASE_BLOCK_SIDE != 3) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property<Int>(BASE_BLOCK_SIDE),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property<Int>(3),
                isAsync = calledAsync
            ).callEvent()
        }
        BASE_BLOCK_SIDE = 3
    }


    internal const val CRAFTING_TABLE_MAKE_BUTTON_SLOT: Int = 35
    const val CRAFTING_TABLE_RESULT_SLOT: Int = 44
    const val CRAFTING_TABLE_TOTAL_SIZE: Int = 54

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
     * @param[recipe] a recipe what you want to register.
     * @throws[IllegalStateException] Calls when the specified recipe is invalid
     */
    fun registerRecipe(recipe: CRecipe): Boolean {
        recipe.isValidRecipe().exceptionOrNull()?.let { t -> throw t }
        if (!RegisterCustomRecipeEvent(recipe).callEvent()) return false
        return synchronized(CustomCrafter.RECIPES) {
            CustomCrafter.RECIPES.add(recipe)
        }
    }

    /**
     * unregisters a provided recipe and calls [UnregisterCustomRecipeEvent].
     *
     * if a called event is cancelled, always fail to unregister recipe.
     *
     * in normally, a result of `RECIPES.remove(recipe)`
     *
     * @param[recipe] a recipe what you want to unregister.
     */
    fun unregisterRecipe(recipe: CRecipe): Boolean {
        val event = UnregisterCustomRecipeEvent(recipe)
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) return false
        return synchronized(CustomCrafter.RECIPES) {
            CustomCrafter.RECIPES.remove(recipe)
        }
    }

    /**
     * Get an item that is used for an all-candidates-menu's not displayable items slot.
     * @return[ItemStack] An item what is displayed when no displayable items on all-candidates-menu.
     * @since 5.0.9
     */
    fun getAllCandidateNotDisplayableItem() = ALL_CANDIDATE_NO_DISPLAYABLE_ITEM

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
    fun setAllCandidateNotDisplayableItem(
        item: ItemStack,
        loreSupplier: (String) -> List<Component>?
    ) {
        if (!item.type.isItem) throw IllegalArgumentException("'item' material must be 'Material#isItem'.")
        ALL_CANDIDATE_NO_DISPLAYABLE_ITEM = item
        ALL_CANDIDATE_NO_DISPLAYABLE_ITEM_LORE_SUPPLIER = loreSupplier
    }

    /**
     * Get an item that is used for an all-candidates-menu's not displayable items slot in default.
     * @return[ItemStack] an item
     * @since 5.0.9
     */
    fun defaultAllCandidateNotDisplayableItems(): ItemStack {
        val item = ItemStack(Material.COMMAND_BLOCK)
        item.editMeta { meta ->
            meta.displayName(MiniMessage.miniMessage().deserialize("<red>Not Displayable Item"))
        }
        return item
    }
}
package io.github.sakaki_aruka.customcrafter

import io.github.sakaki_aruka.customcrafter.event.CustomCrafterAPIPropertiesChangeEvent
import io.github.sakaki_aruka.customcrafter.event.RegisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.event.UnregisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

object CustomCrafterAPI {
    /**
     * Represents the release stage of the Custom Crafter API.
     *
     * Use [CustomCrafterAPI.VERSION_TYPE] to get the current release stage.
     *
     * | Value | Meaning |
     * |-------|---------|
     * | [ALPHA]  | Early stage. APIs may change significantly. |
     * | [BETA]   | Feature-complete, but APIs may still change. |
     * | [RC]     | Release candidate. No further API changes expected. |
     * | [STABLE] | Production-ready. |
     *
     * ```kotlin
     * if (CustomCrafterAPI.VERSION_TYPE != CustomCrafterAPI.VersionType.STABLE) {
     *     logger.warning("CustomCrafter API is not a stable release: ${CustomCrafterAPI.VERSION_TYPE}")
     * }
     * ```
     * @since 5.2.0
     */
    enum class VersionType(val type: String) {
        /** Early development stage. APIs may change significantly. */
        ALPHA("ALPHA"),
        /** Feature-complete, but may contain bugs. APIs may still change. */
        BETA("BETA"),
        /** Release candidate. Stable enough for testing; no further API changes expected. */
        RC("RC"),
        /** Production-ready release. */
        STABLE("STABLE")
    }

    /**
     * Major version number of the Custom Crafter API.
     *
     * Incremented when incompatible API changes are introduced.
     *
     * @see[MINOR_VERSION]
     * @see[PATCH_VERSION]
     * @see[API_VERSION]
     * @since 5.2.0
     */
    const val MAJOR_VERSION = 5

    /**
     * Minor version number of the Custom Crafter API.
     *
     * Incremented when new functionality is added in a backward-compatible manner.
     *
     * @see[MAJOR_VERSION]
     * @see[PATCH_VERSION]
     * @see[API_VERSION]
     * @since 5.2.0
     */
    const val MINOR_VERSION = 2

    /**
     * Patch version number of the Custom Crafter API.
     *
     * Incremented when backward-compatible bug fixes are made.
     *
     * @see[MAJOR_VERSION]
     * @see[MINOR_VERSION]
     * @see[API_VERSION]
     * @since 5.2.0
     */
    const val PATCH_VERSION = 0

    /**
     * Custom Crafter API version string.
     */
    const val API_VERSION: String = "${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}"

    /**
     * CustomCrafterAPI version type
     * @since 5.2.0
     */
    val VERSION_TYPE = VersionType.BETA

    /**
     * Checks full-compatibility
     *
     * This function will be removed in 6.0.0.
     * Use [MAJOR_VERSION], [MINOR_VERSION], [PATCH_VERSION] for version comparison,
     * and [VERSION_TYPE] to check the release stage.
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
    @Deprecated(
        message = "Removal for 6.0.0. Use 'VersionType' and (MAJOR|MINOR|PATCH)_VERSION instead.",
        replaceWith = ReplaceWith("MAJOR_VERSION"),
        level = DeprecationLevel.WARNING
    )
    fun hasFullCompatibility(version: String): Boolean {
        return version in setOf("5.2.0")
    }

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
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE]
     */
    private var BASE_BLOCK_SIDE = AtomicInteger(3)//3
    /**
     * Sets base block's side size.
     *
     * Default size = 3.
     *
     * @param[size] Must be an odd number and 3 or greater. Returns false if the condition is not met.
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @return[Boolean] `true` if the value was changed successfully, `false` if [size] is invalid.
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


    enum class NameStrictLevel(private val priority: Int) {
        NOTHING(3),
        WEAK(2),
        STRICT(1);

        companion object {
            @JvmStatic
            fun contains(level: NameStrictLevel, targets: Set<String>, sources: Set<String>): Boolean {
                // TODO: change to normal class method, reduce STRICT level loop
                return when (level) {
                    NOTHING -> false
                    WEAK -> targets.any { t -> sources.contains(t) }
                    STRICT -> targets.any { t -> sources.any { s -> level.matches(t, s) } }
                }
            }
        }

        fun tryChange(tryValue: NameStrictLevel): NameStrictLevel {
            return if (this.priority > tryValue.priority) {
                tryValue
            } else {
                this
            }
        }

        fun matches(target: String, pattern: String): Boolean {
            return when (this) {
                NOTHING -> false
                WEAK -> target == pattern
                STRICT -> target.filterNot { it.isWhitespace() } == pattern.filterNot { it.isWhitespace() }
            }
        }

        fun hasDuplicate(target: List<String>): Boolean {
            if (this == NOTHING) {
                return false
            }

            if (target.size != target.toSet().size) {
                return true
            }
            if (this == WEAK) {
                return false
            }

            val spaceRemoved: List<String> = target.map { str ->
                str.filterNot { it.isWhitespace() }
            }
            return target.size != spaceRemoved.toSet().size
        }
    }

    internal class CRecipeWrapper(
        val recipe: CRecipe,
        val pluginJarFileHash: Int,
        val id: UUID = UUID.randomUUID()
    )

    private val recipes: ConcurrentHashMap<String, MutableList<CRecipeWrapper>> = ConcurrentHashMap()

    @JvmStatic
    fun getRegisteredRecipeNames(): Set<String> {
        return recipes.keys
    }

    @JvmStatic
    fun getRegisteredRecipeFromName(filter: (String) -> Boolean): List<CRecipe> {
        return recipes.entries.filter { (name, _) -> filter(name) }
            .flatMap { (_, list) -> list }
            .map { it.recipe }
    }

    @JvmStatic
    fun getRegisteredRecipeFromName(recipeName: String): List<CRecipe> {
        return getRegisteredRecipeFromName { name -> name == recipeName }
    }

    @JvmStatic
    fun getRegisteredRecipeFromPlugin(plugin: JavaPlugin): List<CRecipe> {
        return recipes.entries.flatMap { (_, list) -> list }
            .filter { it.pluginJarFileHash == plugin.hashCode() }
            .map { it.recipe }
    }

    @JvmField
    val DEFAULT_RECIPE_NAME_STRICT_LEVEL = NameStrictLevel.NOTHING
    private var recipeNameStrictLevel: AtomicReference<NameStrictLevel> = AtomicReference(DEFAULT_RECIPE_NAME_STRICT_LEVEL)

    @JvmStatic
    fun getRecipeNameStrictLevel(): NameStrictLevel = recipeNameStrictLevel.get()

    @JvmStatic
    @JvmOverloads
    fun setRecipeNameStrictLevel(level: NameStrictLevel, calledAsync: Boolean = false) {
        val oldValue = recipeNameStrictLevel.getAndSet(level.tryChange(level))
        if (level != oldValue) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RECIPE_NAME_STRICT_LEVEL.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(oldValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(level),
                isAsync = calledAsync
            ).callEvent()
        }
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
        return synchronized(recipes) {
            this.recipes.values.flatten().map { it.recipe }
        }
    }

    /**
     * registers a provided recipe and calls [RegisterCustomRecipeEvent].
     *
     * @param[recipes] a recipe what you want to register.
     * @throws[IllegalStateException] Calls when the specified recipe is invalid
     */
    @JvmStatic
    @JvmOverloads
    fun registerRecipe(
        recipes: List<CRecipe>,
        plugin: JavaPlugin = CustomCrafter.getInstance(),
        nameStrictLevel: NameStrictLevel = getRecipeNameStrictLevel()
    ) {
        if (recipes.isEmpty()) {
            return
        }
        if (nameStrictLevel.hasDuplicate(recipes.map { it.name })) {
            throw IllegalArgumentException("Duplicated name found. (current strict level: ${getRecipeNameStrictLevel().name})")
        }
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

        synchronized(this.recipes) {
            if (NameStrictLevel.contains(nameStrictLevel, this.recipes.keys, recipes.map { it.name }.toSet())) {
                throw IllegalArgumentException("Duplicated name found. (current strict level: ${getRecipeNameStrictLevel().name}")
            }
            for (r in recipes) {
                val wrapper = CRecipeWrapper(r, plugin.hashCode())
                this.recipes[r.name]?.add(wrapper)
                    ?: run { this.recipes[r.name] = Collections.synchronizedList(mutableListOf(wrapper)) }
            }
        }

        RegisterCustomRecipeEvent(recipes.toList()).callEvent()
    }

    @JvmStatic
    @JvmOverloads
    fun unregisterRecipe(name: String? = null, plugin: JavaPlugin? = CustomCrafter.getInstance()) {
        val unregisteredRecipes: MutableList<CRecipe> = mutableListOf()

        if (name == null && plugin == null) {
            unregisterAllRecipes()
            return
        }

        if (plugin != null) {
            synchronized(recipes) {
                recipes.forEach(Long.MAX_VALUE) { n, list ->
                    val removeTargets: List<CRecipeWrapper> = list.filter { wrapper ->
                        wrapper.pluginJarFileHash == plugin.hashCode() && (name?.equals(n) ?: true)
                    }
                    list.removeAll(removeTargets)
                    unregisteredRecipes.addAll(removeTargets.map { it.recipe })
                    if (list.isEmpty()) {
                        recipes.remove(n)
                    }
                }
            }
            UnregisterCustomRecipeEvent(unregisteredRecipes.toList()).callEvent()
            return
        }

        // name only filter
        synchronized(recipes) {
            recipes.remove(name)?.let { list ->
                unregisteredRecipes.addAll(list.map { wrapper -> wrapper.recipe })
            }
        }
        UnregisterCustomRecipeEvent(unregisteredRecipes.toList()).callEvent()
    }

    /**
     * Unregisters all registered recipes.
     *
     * @since 5.0.16
     */
    @JvmStatic
    fun unregisterAllRecipes() {
        val removedRecipes: MutableList<CRecipe> = mutableListOf()
        synchronized(recipes) {
            recipes.forEach { (_, wrappers) -> removedRecipes.addAll(wrappers.map { it.recipe }) }
            recipes.clear()
        }
        UnregisterCustomRecipeEvent(removedRecipes).callEvent()
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
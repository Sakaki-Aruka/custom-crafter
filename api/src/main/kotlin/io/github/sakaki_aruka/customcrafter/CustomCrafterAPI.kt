package io.github.sakaki_aruka.customcrafter

import io.github.sakaki_aruka.customcrafter.event.CustomCrafterAPIPropertiesChangeEvent
import io.github.sakaki_aruka.customcrafter.event.RegisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.event.UnregisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.ui.CraftUIDesigner
import io.github.sakaki_aruka.customcrafter.ui.AllCandidateUIDesigner
import io.github.sakaki_aruka.customcrafter.ui.AllCandidateUIDesigner.Companion.bakeWithEmptyContext
import io.github.sakaki_aruka.customcrafter.ui.CraftUIDesigner.Companion.bake
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.VisibleForTesting
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
     * Release stage of the Custom Crafter API.
     * @see[VersionType]
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
     * Author names of the Custom Crafter API.
     */
    @JvmField
    val AUTHORS: Set<String> = setOf("Sakaki-Aruka")

    /**
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RESULT_GIVE_CANCEL]
     */
    private var RESULT_GIVE_CANCEL: AtomicBoolean = AtomicBoolean(false)

    /**
     * Default value of result-give-cancel feature.
     * @see[setResultGiveCancel]
     * @since 5.2.0
     */
    const val DEFAULT_RESULT_GIVE_CANCEL = false

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
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK]
     */
    private var BASE_BLOCK: AtomicReference<Material> = AtomicReference(Material.GOLD_BLOCK)

    /**
     * Default value of base-block.
     * @see[setBaseBlock]
     * @since 5.2.0
     */
    @JvmField
    val DEFAULT_BASE_BLOCK = Material.GOLD_BLOCK

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
     * use 'multiple result candidate' feature or not.
     * - true: if the system gets some result candidates, shows all candidates to a player.
     * - false: provides only a first matched item. (no prompt)
     * @suppress
     * @since 5.0.8
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE]
     */
    private var USE_MULTIPLE_RESULT_CANDIDATE_FEATURE: AtomicBoolean = AtomicBoolean(false)

    /**
     * Default value of use-multiple-result-candidate feature.
     * @see[setUseMultipleResultCandidateFeature]
     * @since 5.2.0
     */
    const val DEFAULT_USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = false

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

    private var USE_CUSTOM_CRAFT_UI = AtomicBoolean(true)

    /**
     * Default value of use-custom-craft-ui
     * @see[setUseCustomCraftUI]
     * @since 5.2.0
     */
    const val DEFAULT_USE_CUSTOM_CRAFT_UI = true

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
     * @suppress
     * @see[CustomCrafterAPIPropertiesChangeEvent.PropertyKey.BASE_BLOCK_SIDE]
     */
    private var BASE_BLOCK_SIDE = AtomicInteger(3)

    /**
     * Default value of base-block-side
     * @see[setBaseBlockSideSize]
     * @since 5.2.0
     */
    const val DEFAULT_BASE_BLOCK_SIDE = 3

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
     * Gets the base block's side size.
     *
     * @return[Int] current side size
     * @see[DEFAULT_BASE_BLOCK_SIDE]
     * @see[setBaseBlockSideSize]
     */
    @JvmStatic
    fun getBaseBlockSideSize(): Int = BASE_BLOCK_SIDE.get()


    private val CRAFT_UI_DESIGNER: AtomicReference<CraftUIDesigner> = AtomicReference(CraftUIDesigner.DEFAULT)

    /**
     * Default value of craft-ui-designer.
     * @see[setCraftUIDesigner]
     * @since 5.2.0
     */
    @JvmField
    val DEFAULT_CRAFT_UI_DESIGNER = CraftUIDesigner.DEFAULT

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
        val baked: CraftUIDesigner.Baked = designer.bake(nullContext)
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
     * Restriction level for recipe name duplication, applied during recipe registration.
     *
     * Introduced to prevent confusion when multiple recipes are displayed simultaneously.
     *
     * - [NOTHING]: No restriction.
     * - [WEAK]: A recipe cannot be registered if an existing recipe has an exactly matching name.
     * - [STRICT]: A recipe cannot be registered if its name, with all whitespace removed, matches an existing recipe name.
     *
     * @since 5.2.0
     */
    enum class NameStrictLevel(private val priority: Int) {
        NOTHING(3),
        WEAK(2),
        STRICT(1);

        /**
         * Returns whether [targets] contains any element from [sources],
         * evaluated at the level on which this method is called.
         *
         * @param[targets] Set of strings to check against
         * @param[sources] Set of strings to look for
         * @return[Boolean] `true` if any element of [sources] is matched in [targets] under this level
         * @since 5.2.0
         */
        fun contains(targets: Set<String>, sources: Set<String>): Boolean {
            return when (this) {
                NOTHING -> false
                WEAK -> targets.any { sources.contains(it) }
                STRICT -> {
                    fun removeSpaces(set: Set<String>): Set<String> {
                        return set.map { entry -> entry.filterNot { c -> c.isWhitespace() } }.toSet()
                    }
                    removeSpaces(targets).any { e -> removeSpaces(sources).contains(e) }
                }
            }
        }

        /**
         * Used by [CustomCrafterAPI.setRecipeNameStrictLevel] to determine whether the level
         * can be changed from the current level to [tryValue].
         * The level can only move in the stricter direction; loosening is not allowed.
         *
         * @param[tryValue] The target level to attempt to change to
         * @return[NameStrictLevel] [tryValue] if the change is permitted, or the current level if not
         * @since 5.2.0
         */
        fun tryChange(tryValue: NameStrictLevel): NameStrictLevel {
            return if (this.priority > tryValue.priority) {
                tryValue
            } else {
                this
            }
        }

        /**
         * Returns whether [pattern] matches [target], evaluated at the level on which this method is called.
         * @param[target] The string to test
         * @param[pattern] The string to match against
         * @return[Boolean] `true` if [pattern] matches [target] under this level
         * @since 5.2.0
         */
        fun matches(target: String, pattern: String): Boolean {
            return when (this) {
                NOTHING -> false
                WEAK -> target == pattern
                STRICT -> target.filterNot { it.isWhitespace() } == pattern.filterNot { it.isWhitespace() }
            }
        }

        /**
         * Returns whether [target] contains duplicate names according to this level.
         *
         * @param[target] List of strings to check for duplicates
         * @return[Boolean] `true` if any duplicates are detected under this level
         * @since 5.2.0
         */
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

    /**
     * Returns a set of all registered recipe names.
     * @return[Set] set of registered recipe names (`Set<String>`)
     * @since 5.2.0
     */
    @JvmStatic
    fun getRegisteredRecipeNames(): Set<String> {
        return recipes.keys
    }

    /**
     * Returns registered recipes whose names satisfy the given filter predicate.
     * @param[filter] Predicate applied to each recipe name; matching recipes are included
     * @return[List] list of matched recipes (`List<CRecipe>`)
     * @since 5.2.0
     */
    @JvmStatic
    fun getRegisteredRecipeFromName(filter: (String) -> Boolean): List<CRecipe> {
        return recipes.entries.filter { (name, _) -> filter(name) }
            .flatMap { (_, list) -> list }
            .map { it.recipe }
    }

    /**
     * Returns registered recipes whose names exactly match the given string.
     * @param[recipeName] Exact recipe name to search for
     * @return[List] list of matched recipes (`List<CRecipe>`)
     * @since 5.2.0
     */
    @JvmStatic
    fun getRegisteredRecipeFromName(recipeName: String): List<CRecipe> {
        return getRegisteredRecipeFromName { name -> name == recipeName }
    }

    /**
     * Returns recipes that are registered by given plugin instance.
     * @param[plugin] Search plugin instance
     * @return[List] list of matched recipes (`List<CRecipe>`)
     * @since 5.2.0
     */
    @JvmStatic
    fun getRegisteredRecipeFromPlugin(plugin: JavaPlugin): List<CRecipe> {
        return recipes.entries.flatMap { (_, list) -> list }
            .filter { it.pluginJarFileHash == plugin.hashCode() }
            .map { it.recipe }
    }


    /**
     * Default value of recipe-name-strict-level.
     * @see[setRecipeNameStrictLevel]
     * @since 5.2.0
     */
    @JvmField
    val DEFAULT_RECIPE_NAME_STRICT_LEVEL = NameStrictLevel.NOTHING
    private var recipeNameStrictLevel: AtomicReference<NameStrictLevel> = AtomicReference(DEFAULT_RECIPE_NAME_STRICT_LEVEL)

    /**
     * Returns current recipe-name-strict-level
     * @return[NameStrictLevel] current level
     * @since 5.2.0
     */
    @JvmStatic
    fun getRecipeNameStrictLevel(): NameStrictLevel = recipeNameStrictLevel.get()

    @VisibleForTesting
    internal fun setRecipeNameStrictLevelDefault() {
        recipeNameStrictLevel.set(DEFAULT_RECIPE_NAME_STRICT_LEVEL)
    }

    /**
     * Sets the recipe name strict level.
     *
     * The level can only change in the stricter direction; loosening is not permitted.
     * @param[level] Target strict level to apply
     * @param[calledAsync] Called from async processing or not. (Default = false)
     * @see[NameStrictLevel]
     * @since 5.2.0
     */
    @JvmStatic
    @JvmOverloads
    fun setRecipeNameStrictLevel(level: NameStrictLevel, calledAsync: Boolean = false) {
        val newLevel = recipeNameStrictLevel.get().tryChange(level)
        val oldValue = recipeNameStrictLevel.getAndSet(newLevel)
        if (newLevel != oldValue) {
            CustomCrafterAPIPropertiesChangeEvent(
                propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.RECIPE_NAME_STRICT_LEVEL.name,
                oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(oldValue),
                newValue = CustomCrafterAPIPropertiesChangeEvent.Property(newLevel),
                isAsync = calledAsync
            ).callEvent()
        }
    }

    /**
     * Returns an immutable list of all registered recipes.
     *
     * The returned list cannot be modified.
     * @return[List]<[CRecipe]> immutable list of all registered recipes
     */
    @JvmStatic
    fun getRecipes(): List<CRecipe> {
        return synchronized(recipes) {
            this.recipes.values.flatten().map { it.recipe }
        }
    }

    /**
     * Registers the provided recipes and fires [RegisterCustomRecipeEvent].
     *
     * @param[recipes] Recipes to register
     * @param[plugin] Plugin instance registering the recipes
     * @throws[IllegalArgumentException] if [recipes] contains duplicate names under the current strict level
     * @throws[IllegalStateException] if any recipe in [recipes] is invalid
     */
    @JvmStatic
    @JvmOverloads
    fun registerRecipe(
        recipes: List<CRecipe>,
        plugin: JavaPlugin = CustomCrafter.getInstance(),
    ) {
        if (recipes.isEmpty()) {
            return
        }
        if (getRecipeNameStrictLevel().hasDuplicate(recipes.map { it.name })) {
            throw IllegalArgumentException("'recipes' has duplicated name recipes. (current strict level: ${getRecipeNameStrictLevel().name})")
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
            if (getRecipeNameStrictLevel().contains(this.recipes.keys, recipes.map { it.name }.toSet())) {
                throw IllegalArgumentException("Duplicated name found. (current strict level: ${getRecipeNameStrictLevel().name}")
            }
            for (r in recipes) {
                val wrapper = CRecipeWrapper(r, plugin.hashCode())
                this.recipes[r.name]?.add(wrapper)
                    ?: run { this.recipes[r.name] = Collections.synchronizedList(mutableListOf(wrapper)) }
            }
        }

        RegisterCustomRecipeEvent(recipes.toList(), plugin.pluginMeta).callEvent()
    }

    /**
     * Unregisters recipes that match the specified conditions.
     *
     * If both [name] and [plugin] are `null`, all registered recipes are unregistered.
     * @param[name] Target recipe name to filter by. If `null`, name is not used as a filter.
     * @param[plugin] Plugin instance to filter by registering plugin.
     *   Defaults to [CustomCrafter.getInstance], targeting recipes registered without an explicit plugin.
     *   Pass `null` to exclude the plugin from the filter criteria.
     */
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

    private val allCandidateUIDesigner: AtomicReference<AllCandidateUIDesigner> = AtomicReference(AllCandidateUIDesigner.DEFAULT)

    /**
     * Default value of AllCandidateUIDesigner
     * @see[setAllCandidateUIDesigner]
     * @since 5.2.0
     */
    @JvmField
    val DEFAULT_ALL_CANDIDATE_UI_DESIGNER = AllCandidateUIDesigner.DEFAULT

    /**
     * Returns current [AllCandidateUIDesigner]
     * @return[AllCandidateUIDesigner] current designer
     * @since 5.2.0
     */
    @JvmStatic
    fun getAllCandidateUIDesigner(): AllCandidateUIDesigner = allCandidateUIDesigner.get()

    /**
     * Sets the [AllCandidateUIDesigner].
     *
     * Throws an exception if the [AllCandidateUIDesigner.Baked] produced by calling
     * [AllCandidateUIDesigner.bakeWithEmptyContext] on [designer] fails [AllCandidateUIDesigner.Baked.isValid].
     * @param[designer] The new designer to set
     * @since 5.2.0
     */
    @JvmStatic
    fun setAllCandidateUIDesigner(designer: AllCandidateUIDesigner) {
        val validationResult = designer.bakeWithEmptyContext().isValid()
        if (validationResult.isFailure) {
            validationResult.exceptionOrNull()?.let { throw it }
        }
        val oldDesigner = allCandidateUIDesigner.getAndSet(designer)
        CustomCrafterAPIPropertiesChangeEvent(
            propertyName = CustomCrafterAPIPropertiesChangeEvent.PropertyKey.ALL_CANDIDATE_UI_DESIGNER.name,
            oldValue = CustomCrafterAPIPropertiesChangeEvent.Property(oldDesigner),
            newValue = CustomCrafterAPIPropertiesChangeEvent.Property(designer)
        ).callEvent()
    }
}
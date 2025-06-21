package io.github.sakaki_aruka.customcrafter

import io.github.sakaki_aruka.customcrafter.api.event.RegisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.event.UnregisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.persistence.PersistentDataType

object CustomCrafterAPI {
    /**
     * Custom Crafter API version.
     *
     * This version is different with the plugin version string.
     */
    const val API_VERSION: String = "0.1.10"

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
     * If this true, the Custom Crafter API does not give result items to players.
     *
     * You have to set this to true if you want to give result items processing in your plugin.
     *
     * (Default Value is `false`)
     */
    var RESULT_GIVE_CANCEL: Boolean = false
    internal var BASE_BLOCK: Material = Material.GOLD_BLOCK

    /**
     * use 'multiple result candidate' feature or not.
     * - true: if the system gets some result candidates, shows all candidates to a player.
     * - false: provides only a first matched item. (no prompt)
     * @since 5.0.8
     */
    var USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = false

    /**
     * Use 'auto crafting' feature or not.
     *
     * Default is false.
     * @since 5.0.10
     */
    var USE_AUTO_CRAFTING_FEATURE = false

    /**
     * AutoCrafting feature compatibilities.
     *
     * - KEY: Target version
     * - VALUE: Versions what are compatible with the target version
     * @since 5.0.10
     */
    val AUTO_CRAFTING_CONFIG_COMPATIBILITIES: Map<String, Set<String>> = mapOf(
        "0.1.10" to setOf("0.1.10")
    )


    internal var BASE_BLOCK_SIDE: Int = 3
    const val CRAFTING_TABLE_MAKE_BUTTON_SLOT: Int = 35
    const val CRAFTING_TABLE_RESULT_SLOT: Int = 44
    internal const val ALL_CANDIDATE_PREVIOUS_SLOT: Int = 45
    internal const val ALL_CANDIDATE_SIGNATURE_SLOT: Int = 49
    internal const val ALL_CANDIDATE_NEXT_SLOT: Int = 53
    const val CRAFTING_TABLE_TOTAL_SIZE: Int = 54

    /**
     * An item that is used for an all-candidates-menu's not displayable items slot.
     *
     * To get, use [getAllCandidateNotDisplayableItem].
     *
     * To set, use [setAllCandidateNotDisplayableItem].
     *
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
     * A lambda expression used to pick only one [AutoCraftRecipe] when auto-crafting provides more than 2 recipes.
     * ```
     * // A default implementation
     * AUTO_CRAFTING_PICKUP_RESOLVER = { list ->
     *   list.firstOrNull()
     * }
     * ```
     * @since 5.0.10
     */
    var AUTO_CRAFTING_PICKUP_RESOLVER: (List<AutoCraftRecipe>) -> AutoCraftRecipe? = { list ->
        list.firstOrNull()
    }

    /**
     * A lambda expression that provides the set of recipes used as the source when the Auto Crafting feature searches for a recipe matching the recorded recipe ID.
     * ```kotlin
     * // A default implementation
     * AUTO_CRAFTING_SOURCE_RECIPES_PROVIDER: (Block) -> List<AutoCraftingIdentifier> = { _ ->
     *     CustomCrafterAPI.getRecipes().filterIsInstance<AutoCraftingIdentifier>()
     * }
     * ```
     * @since 5.0.10
     */
    var AUTO_CRAFTING_SOURCE_RECIPES_PROVIDER: (Block) -> List<AutoCraftRecipe> = { _ ->
        this.getRecipes().filterIsInstance<AutoCraftRecipe>()
    }

    /**
     * ```kotlin
     * // A default implementation
     * AUTO_CRAFTING_SETTING_PAGE_SUGGESTION: (Block, Player) -> List<AutoCraftingIdentifier> = { _, _ ->
     *     CustomCrafterAPI.getRecipes().filterIsInstance<AutoCraftingIdentifier>()
     * }
     * ```
     * @since 5.0.10
     */
    var AUTO_CRAFTING_SETTING_PAGE_SUGGESTION: (Block, Player) -> List<AutoCraftRecipe> = { _, _ ->
        this.getRecipes().filterIsInstance<AutoCraftRecipe>()
    }

    /**
     *
     * ```kotlin
     * // A Default implementation
     * AUTO_CRAFTING_RESULT_PICKUP_RESOLVER: (List<CRecipe>, Search.SearchResult, Block) -> Pair<CRecipe?, Recipe?> = { _, result, _ ->
     *     result.customs().firstOrNull() to result.vanilla()
     * }
     * ```
     * @since 5.0.10
     */
    var AUTO_CRAFTING_RESULT_PICKUP_RESOLVER: (List<CRecipe>, Search.SearchResult, Block)
        -> Triple<AutoCraftRecipe?, MappedRelation?, Recipe?> = { _, result, _ ->
            result.customs().firstOrNull { (recipe, _) -> recipe is AutoCraftRecipe }?.let { (recipe, relation) ->
                Triple(recipe as AutoCraftRecipe, relation, result.vanilla())
            } ?: Triple(null, null, result.vanilla())
    }

    /**
     * Default `true`.
     * @since 5.0.10
     */
    var AUTO_CRAFTING_RESULT_PICKUP_RESOLVER_PRIORITIZE_CUSTOM: Boolean = true

    private var AUTO_CRAFTING_BASE_BLOCK: Material = Material.GOLD_BLOCK
    /**
     * Get auto crafting base block type.
     * @return[Material] A base block of auto crafting.
     * @since 5.0.10
     */
    fun getAutoCraftingBaseBlock(): Material = AUTO_CRAFTING_BASE_BLOCK

    /**
     * Set auto crafting base block with given material.
     *
     * If a given material is not a block type, throws [IllegalArgumentException].
     * @param[type] Auto crafting base block type
     * @throws[IllegalArgumentException] When specified not block type
     * @since 5.0.10
     */
    fun setAutoCraftingBaseBlock(type: Material) {
        if (!type.isBlock) throw IllegalArgumentException("'type' must meet 'Material#isBlock'.")
        AUTO_CRAFTING_BASE_BLOCK = type
    }

    /**
     * Get base block type.
     * @return[Material] base block type
     * @since 5.0.9
     */
    fun getBaseBlock(): Material = BASE_BLOCK

    /**
     * Set base block with given material.
     *
     * If a given material is not a block type, throws [IllegalArgumentException].
     * @param[type] base block type
     * @throws[IllegalArgumentException] when specified not block type
     * @since 5.0.9
     */
    fun setBaseBlock(type: Material) {
        if (!type.isBlock) throw IllegalArgumentException("'type' must meet 'Material#isBlock'.")
        BASE_BLOCK = type
    }

    /**
     * returns an IMMUTABLE list what contains all registered recipes.
     *
     * NOTICE: it is immutable, so you cannot modify its components.
     *
     * @return[List]<[CRecipe]> recipes list
     */
    fun getRecipes(): List<CRecipe> = CustomCrafter.RECIPES.toList()

    /**
     * returns random generated coordinates.
     *
     * @param[n] amount of coordinates what you want to generate
     * @throws[IllegalArgumentException] thrown if [n] < 1
     * @return[Set<CoordinateComponent>] result coordinates
     */
    fun getRandomNCoordinates(n: Int): Set<CoordinateComponent> {
        if (n < 1) throw IllegalArgumentException("'n' must be greater than zero.")
        val result: MutableSet<CoordinateComponent> = mutableSetOf()
        repeat(n) { i ->
            result.add(CoordinateComponent(i % 9, i / 9))
        }
        return result
    }

    /**
     * registers a provided recipe and calls [RegisterCustomRecipeEvent].
     *
     * if a called event is cancelled, always fail to register recipe.
     *
     * in normally, a result of `RECIPES.add(recipe)`.
     *
     * @param[recipe] a recipe what you want to register.
     */
    fun registerRecipe(recipe: CRecipe): Boolean {
        if (!RegisterCustomRecipeEvent(recipe).callEvent()) return false
        return CustomCrafter.RECIPES.add(recipe)
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
        return CustomCrafter.RECIPES.remove(recipe)
    }

    /**
     * set base block's side size.
     *
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
     *
     * returned Triple contained below elements.
     * - first([NamespacedKey]): "custom_crafter:gui_created"
     * - second([PersistentDataType.LONG]): a type of 'third'
     * - third([Long]): epoch time when called this.
     *
     * @return[Triple]
     */
    fun genCCKey() = Triple(
        NamespacedKey("custom_crafter", "gui_created"),
        PersistentDataType.LONG,
        System.currentTimeMillis()
    )
    private val blank = ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply {
            displayName(Component.empty())
        }
    }
    private fun makeButton() = ItemStack(Material.ANVIL).apply {
        itemMeta = itemMeta.apply {
            displayName(Component.text("Making items"))
            val key = genCCKey()
            persistentDataContainer.set(key.first, key.second, key.third)
        }
    }

    /**
     * returns custom crafter gui
     *
     * @param[dropItemsOnClose] drops materials or not when a player close this gui (default = false, since = 5.0.8)
     * @return[Inventory] custom crafter gui
     */
    fun getCraftingGUI(): Inventory {
        val gui: Inventory = Bukkit.createInventory(null, CRAFTING_TABLE_TOTAL_SIZE, Component.text("Custom Crafter"))
        (0..<54).forEach { slot -> gui.setItem(slot, blank) }
        Converter.getAvailableCraftingSlotComponents().forEach { c ->
            val index: Int = c.x + c.y * 9
            gui.setItem(index, ItemStack.empty())
        }
        val makeButton: ItemStack = makeButton()
        gui.setItem(CRAFTING_TABLE_MAKE_BUTTON_SLOT, makeButton)
        gui.setItem(CRAFTING_TABLE_RESULT_SLOT, ItemStack.empty())
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
            ?.takeIf { it.type == makeButton().type }
            ?: return false
        val key = genCCKey()
        return makeButton.itemMeta.persistentDataContainer.has(key.first, key.second)
    }


    internal val allCandidateSignatureNK = NamespacedKey(CustomCrafter.getInstance(), "all_candidate_signature")

    /**
     * returns the provided inventory is all-candidates-page gui or not.
     *
     * @param[inventory] input inventory
     * @return[Boolean] is all-candidates-page gui or not
     * @since 5.0.8
     */
    fun isAllCandidatesPageGUI(inventory: Inventory): Boolean {
        if (inventory.size != 54) return false
        val signature: ItemStack = inventory.getItem(ALL_CANDIDATE_SIGNATURE_SLOT) ?: return false
        return signature.itemMeta.persistentDataContainer.getOrDefault(
            allCandidateSignatureNK,
            PersistentDataType.STRING,
            "_" // default (if not found applied)
        ) == ""
    }

    /**
     * returns the provided inventory is OLDER than custom crafter reloaded or enabled or not.
     *
     * if you provide an inventory what is not a custom crafter gui, this throws an Exception.
     *
     * @param[inventory] provided inventory
     * @throws[IllegalArgumentException] thrown when the provided is not custom crafter gui
     * @throws[IllegalStateException] thrown when get an error on get gui created epoch time
     * @return[Boolean] older or not
     */
    fun isGUITooOld(inventory: Inventory): Boolean {
        if (!isCustomCrafterGUI(inventory)
            && !isAllCandidatesPageGUI(inventory)) {
            throw IllegalArgumentException("'inventory' must be a CustomCrafter's gui.")
        }
        val (key, type, _) = genCCKey()
        val time: Long = inventory.contents
            .filterNotNull()
            .firstOrNull { item -> item.itemMeta.persistentDataContainer.has(key, type) }
            ?.let { i -> i.itemMeta.persistentDataContainer.get(key, type) }
            ?: throw IllegalStateException("'time' key contained item not found.")

        return time < CustomCrafter.INITIALIZED
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
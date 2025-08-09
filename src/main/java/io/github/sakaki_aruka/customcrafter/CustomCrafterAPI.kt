package io.github.sakaki_aruka.customcrafter

import io.github.sakaki_aruka.customcrafter.api.event.RegisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.event.UnregisterCustomRecipeEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

object CustomCrafterAPI {
    /**
     * Custom Crafter API version.
     *
     * This version is different with the plugin version string.
     */
    const val API_VERSION: String = "0.1.11"

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
        "0.1.10" to setOf("0.1.10"),
        "0.1.11" to setOf("0.1.11")
    )


    internal var BASE_BLOCK_SIDE: Int = 3
    const val CRAFTING_TABLE_MAKE_BUTTON_SLOT: Int = 35
    const val CRAFTING_TABLE_RESULT_SLOT: Int = 44
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
     *     var AUTO_CRAFTING_RESULT_PICKUP_RESOLVER: (List<CRecipe>, Search.SearchResult, Block)
     *         -> Triple<AutoCraftRecipe?, MappedRelation?, Recipe?> = { _, result, _ ->
     *             result.customs().firstOrNull { (recipe, _) -> recipe is AutoCraftRecipe }?.let { (recipe, relation) ->
     *                 Triple(recipe as AutoCraftRecipe, relation, result.vanilla())
     *             } ?: Triple(null, null, result.vanilla())
     *     }
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
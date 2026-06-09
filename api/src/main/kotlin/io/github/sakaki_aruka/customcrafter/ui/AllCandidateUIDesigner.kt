package io.github.sakaki_aruka.customcrafter.ui

import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import io.github.sakaki_aruka.customcrafter.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.search.Search
import io.github.sakaki_aruka.customcrafter.util.Converter.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Interface for customizing the AllCandidateUI, which displays all craft recipe candidates.
 *
 * All methods have default implementations, so only the parts that need customization must be overridden;
 * the rest fall back to the default behavior.
 *
 * Internally, [bake] receives a [Context] and produces a [Baked] instance with all values resolved.
 * If the resulting [Baked] fails validation (contradictory or out-of-range values),
 * a [Baked] built entirely from default values is used instead.
 *
 *
 * @see[bake]
 * @see[Baked]
 * @since 5.2.0
 */
interface AllCandidateUIDesigner {
    /**
     * Returns the title [Component] of the AllCandidateUI inventory.
     * @param[context] Context provided at bake time
     * @return[Component] inventory title component
     * @since 5.2.0
     */
    fun title(context: Context): Component = "All Candidate".toComponent()

    /**
     * Returns the slot coordinate and icon item for the previous-page button.
     *
     * The `Pair` used here is a Kotlin class, but can also be constructed from Java as `new Pair<>(element1, element2)`.
     * @param[context] Context provided at bake time
     * @return[Pair] slot coordinate and icon item for the previous-page button
     * @since 5.2.0
     */
    fun previousPageButton(context: Context): Pair<CoordinateComponent, ItemStack> {
        return CoordinateComponent.fromIndex(45) to CustomCrafterUI.PREVIOUS_BUTTON
    }

    /**
     * Returns the slot coordinate and icon item for the next-page button.
     * @param[context] Context provided at bake time
     * @return[Pair] slot coordinate and icon item for the next-page button
     * @since 5.2.0
     */
    fun nextPageButton(context: Context): Pair<CoordinateComponent, ItemStack> {
        return CoordinateComponent.fromIndex(53) to CustomCrafterUI.NEXT_BUTTON
    }

    /**
     * Returns the slot coordinate and icon item for the button that navigates back to the CraftUI.
     * @param[context] Context provided at bake time
     * @return[Pair] slot coordinate and icon item for the back-to-CraftUI button
     * @since 5.2.0
     */
    fun backToCraftUIButton(context: Context): Pair<CoordinateComponent, ItemStack> {
        val button = ItemStack.of(Material.CRAFTING_TABLE).apply {
            itemMeta = itemMeta.apply {
                displayName("<b>BACK TO CRAFT".toComponent())
            }
        }
        return CoordinateComponent.fromIndex(49) to button
    }

    /**
     * Returns the set of slot coordinates where recipe icons can be placed.
     *
     * The set size must be between 1 and 51 inclusive (total inventory slots minus the 3 button slots),
     * and must not include any coordinate occupied by a button.
     *
     * For example, if 45 recipes are available but this method returns a set of 30 slots,
     * the AllCandidateUI will paginate across 2 pages.
     * @param[context] Context provided at bake time
     * @return[Set] set of slot coordinates available for recipe icons
     * @since 5.2.0
     */
    fun recipeSlots(context: Context): Set<CoordinateComponent> {
        return (0..<45).map { CoordinateComponent.fromIndex(it) }.toSet()
    }

    /**
     * Returns the icon item shown in recipe slots where the recipe cannot produce a displayable item.
     * @param[context] Context provided at bake time
     * @return[ItemStack] placeholder icon item for non-displayable recipes
     * @since 5.2.0
     */
    fun noDisplayableItem(context: Context): ItemStack {
        val item = ItemStack.of(Material.COMMAND_BLOCK)
        item.editMeta { meta ->
            meta.displayName("<red>Not Displayable Item".toComponent())
        }
        return item
    }

    /**
     * Returns a lambda that produces a placeholder icon for recipe slots whose result item has not yet been generated.
     *
     * The lambda receives the [CRecipe] being displayed and returns the icon [ItemStack] to show in its slot.
     * @param[context] Context provided at bake time
     * @return[(CRecipe) -> ItemStack] factory that builds a placeholder icon for the given recipe
     * @since 5.2.0
     */
    fun ungeneratedIconPlaceholderItem(context: Context): (CRecipe) -> ItemStack {
        return { recipe ->
            val item = ItemStack.of(Material.BARRIER)
            item.editMeta { meta ->
                meta.displayName("UN-GENERATED".toComponent())
                meta.lore(listOf(
                    "<white>Recipe Name: <b>${recipe.name}</b>".toComponent(),
                    "<white>Items for this recipe have not been created yet.".toComponent()
                ))
            }
            item
        }
    }

    companion object {
        /**
         * Default AllCandidateUIDesigner
         * @since 5.2.0
         */
        @JvmField
        val DEFAULT = object : AllCandidateUIDesigner {}

        /**
         * Baked default AllCandidateUIDesigner
         * @since 5.2.0
         */
        @JvmField
        val BAKED_DEFAULT = DEFAULT.bakeWithEmptyContext()

        /**
         * Bakes this designer's values with the given context into an immutable [Baked] instance.
         *
         * Executed in an asynchronous context internally; if values cannot be produced within 50 milliseconds,
         * the default values are used instead.
         *
         * ```kotlin
         * // Kotlin
         * val baked = designer.bake(context)
         * ```
         * ```java
         * // Java
         * Baked baked = AllCandidateUIDesigner.bake(designer, context);
         * ```
         * @param[context] Context used to resolve designer values
         * @return[Baked] Immutable snapshot of the resolved designer values
         * @since 5.2.0
         */
        @JvmStatic
        fun AllCandidateUIDesigner.bake(context: Context): Baked {
            return Baked(
                title = this.title(context),
                recipeSlots = this.recipeSlots(context),
                previousPageButton = this.previousPageButton(context),
                nextPageButton = this.nextPageButton(context),
                backToCraftUIButton = this.backToCraftUIButton(context),
                noDisplayableItem = this.noDisplayableItem(context),
                ungeneratedIconPlaceholderItem = this.ungeneratedIconPlaceholderItem(context)
            )
        }

        /**
         * Bakes a context with [Context.emptyContext].
         * Shorthand of `.bake(Context.emptyContext)`
         *
         * ```kotlin
         * // Kotlin
         * val baked = designer.bakeWithEmptyContext()
         * ```
         * ```java
         * // Java
         * Baked baked = AllCandidateUIDesigner.bakeWithEmptyContext(designer);
         * ```
         * @see[Context.emptyContext]
         * @return[Baked] Immutable snapshot of the resolved designer values
         */
        @JvmStatic
        fun AllCandidateUIDesigner.bakeWithEmptyContext(): Baked {
            return this.bake(Context.emptyContext())
        }
    }

    /**
     * Context provided when baking an [AllCandidateUIDesigner] into a fixed-value [Baked] instance.
     * @param[searchResult] The search result available at bake time
     * @param[crafterId] UUID of the player viewing the AllCandidateUI
     * @since 5.2.0
     */
    data class Context(
        val searchResult: Search.SearchResult,
        val crafterId: UUID
    ) {
        companion object {
            /**
             * Returns a context with an empty search result and a random UUID.
             *
             * Used for validation baking when no real context is available.
             * @return[Context] minimal context with no search result and a random player UUID
             * @since 5.2.0
             */
            @JvmStatic
            fun emptyContext(): Context {
                return Context(Search.SearchResult.EMPTY, UUID.randomUUID())
            }
        }
    }

    /**
     * An immutable snapshot of a resolved [AllCandidateUIDesigner] with all values fixed at bake time.
     *
     * Obtained by calling [bake] or [bakeWithEmptyContext]. If any value fails [isValid],
     * the system falls back to a [Baked] built entirely from the [DEFAULT] designer.
     * @since 5.2.0
     */
    class Baked(
        val title: Component,
        val recipeSlots: Set<CoordinateComponent>,
        val previousPageButton: Pair<CoordinateComponent, ItemStack>,
        val nextPageButton: Pair<CoordinateComponent, ItemStack>,
        val backToCraftUIButton: Pair<CoordinateComponent, ItemStack>,
        val noDisplayableItem: ItemStack,
        val ungeneratedIconPlaceholderItem: (CRecipe) -> ItemStack
    ) {
        val recipeSlotsIndex: Set<Int> = recipeSlots.map { it.toIndex() }.toSet()

        companion object {
            private val validRange: Set<CoordinateComponent> = (0..<54).map {
                CoordinateComponent.fromIndex(it)
            }.toSet()

            private fun isValidIconItem(item: ItemStack, name: String): Result<Unit>? {
                return if (!item.isEmpty && item.amount >= 1 && item.type.isItem) {
                    null
                } else {
                    Result.failure(IllegalStateException("'$name' item must be visible. (Caused: isEmpty = ${item.isEmpty}, notEnoughAmount = ${item.amount < 1}, isNotVisibleItem = ${!item.type.isItem})"))
                }
            }
        }

        /**
         * Validates this [Baked] instance and returns the result.
         *
         * Returns [Result.success] if all values are consistent and within the valid range,
         * or [Result.failure] containing an [IllegalStateException] that describes the first violation found.
         * Use `isSuccess` / `isFailure` on the returned [Result] to check the outcome.
         * @return[Result] success if valid; failure with a descriptive exception if not
         * @since 5.2.0
         */
        fun isValid(): Result<Unit> {
            if (recipeSlots.isEmpty() || recipeSlots.size > (54 - 3)) {
                return Result.failure(IllegalStateException("'recipeSlots' size must be in range of 1 to 51. (current: ${recipeSlots.size})"))
            }

            fun validateSlotAndItem(pair: Pair<CoordinateComponent, ItemStack>, name: String): Result<Unit>? {
                val (coordinate, item) = pair
                if (coordinate !in validRange) {
                    return Result.failure(IllegalStateException("'$name' must be in the valid range. (valid range: x=0~8, y=0~5)"))
                }
                if (recipeSlots.contains(coordinate)) {
                    return Result.failure(IllegalStateException("'$name' coordinate duplicated with 'recipeSlots'. (x: ${coordinate.x}, y: ${coordinate.y})"))
                }

                isValidIconItem(item, name)?.let { return it }

                return null
            }

            if (recipeSlots.any { !validRange.contains(it) }) {
                return Result.failure(IllegalStateException("'recipeSlots' must not contain the invalid range coordinates. (valid range: x=0~8, y=0~5)"))
            }

            val buttons = setOf(
                previousPageButton to "previousPageButton",
                nextPageButton to "nextPageButton",
                backToCraftUIButton to "backToCraftUIButton"
            )

            for ((pair, name) in buttons) {
                validateSlotAndItem(pair, name)?.let { return it }
            }

            isValidIconItem(noDisplayableItem, "noDisplayableItem")?.let { return it }

            return Result.success(Unit)
        }

        /**
         * Returns the placeholder icon displayed for [recipe] until its result item has been generated.
         *
         * If the item produced by [ungeneratedIconPlaceholderItem] is not displayable
         * (empty, zero amount, or non-item material), the default placeholder from [BAKED_DEFAULT] is used instead.
         * @param[recipe] The recipe whose result item has not yet been generated
         * @return[ItemStack] placeholder icon item to display in the UI slot
         * @since 5.2.0
         */
        fun ungeneratedIcon(recipe: CRecipe): ItemStack {
            return this.ungeneratedIconPlaceholderItem(recipe)
                .takeUnless { it.isEmpty || it.amount < 1 || !it.type.isItem }
                ?: BAKED_DEFAULT.ungeneratedIconPlaceholderItem(recipe)
        }
    }
}
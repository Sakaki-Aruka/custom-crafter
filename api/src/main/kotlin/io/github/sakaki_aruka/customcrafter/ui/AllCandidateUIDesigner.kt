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
 * 全候補表示機能 UI (AllCandidateUI) をカスタマイズするためのインターフェース。
 * すべてのメソッドにデフォルト実装が存在するため、最低限必要なものだけをカスタマイズしてほかはデフォルトの設定を利用できる。
 * 実際には、 bake で Context を受け取って、値がすべて固定された Baked のインスタンスを作成してそれが利用される。
 * 作成された Baked の各メソッドが矛盾する値や範囲外の値を持つとき、バリデーションで弾かれ、すべてがデフォルト値の Baked インスタンスが利用される。
 *
 * @see[bake]
 * @see[Baked]
 * @since 5.2.0
 */
interface AllCandidateUIDesigner {
    fun title(context: Context): Component = "All Candidate".toComponent()

    fun previousPageButton(context: Context): Pair<CoordinateComponent, ItemStack> {
        return CoordinateComponent.fromIndex(45) to CustomCrafterUI.PREVIOUS_BUTTON
    }

    fun nextPageButton(context: Context): Pair<CoordinateComponent, ItemStack> {
        return CoordinateComponent.fromIndex(53) to CustomCrafterUI.NEXT_BUTTON
    }

    fun backToCraftUIButton(context: Context): Pair<CoordinateComponent, ItemStack> {
        val button = ItemStack.of(Material.CRAFTING_TABLE).apply {
            itemMeta = itemMeta.apply {
                displayName("<b>BACK TO CRAFT".toComponent())
            }
        }
        return CoordinateComponent.fromIndex(49) to button
    }

    fun recipeSlots(context: Context): Set<CoordinateComponent> {
        return (0..<45).map { CoordinateComponent.fromIndex(it) }.toSet()
    }

    fun noDisplayableItem(context: Context): ItemStack {
        val item = ItemStack.of(Material.COMMAND_BLOCK)
        item.editMeta { meta ->
            meta.displayName("<red>Not Displayable Item".toComponent())
        }
        return item
    }

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
        @JvmField
        val DEFAULT = object : AllCandidateUIDesigner {}

        /**
         * Baked default AllCandidateUIDesigner
         * @since 5.2.0
         */
        @JvmField
        val BAKED_DEFAULT = DEFAULT.bakeWithEmptyContext()

        /**
         * Bakes AllCandidateUIDesigner values with a given context.
         * 実際には非同期環境で実行され、 50 ミリ秒以内に値を返せなければデフォルトの値が利用されます。
         *
         * Java から呼び出す際は AllCandidateUIDesignerKt.bake を呼び出す必要があります。
         *
         * @param[context] Context to create a user specified ui designer
         * @return[Baked] Unmodifiable ui designer
         * @since 5.2.0
         */
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

        fun AllCandidateUIDesigner.bakeWithEmptyContext(): Baked {
            return this.bake(Context.emptyContext())
        }
    }

    data class Context(
        val searchResult: Search.SearchResult,
        val crafterId: UUID
    ) {
        companion object {
            @JvmStatic
            fun emptyContext(): Context {
                return Context(Search.SearchResult.EMPTY, UUID.randomUUID())
            }
        }
    }

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

        fun ungeneratedIcon(recipe: CRecipe): ItemStack {
            return this.ungeneratedIconPlaceholderItem(recipe)
                .takeUnless { it.isEmpty || it.amount < 1 || !it.type.isItem }
                ?: BAKED_DEFAULT.ungeneratedIconPlaceholderItem(recipe)
        }
    }
}
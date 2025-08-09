package io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.recipe.filter.CVanillaRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.Converter.toComponent
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterUI
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class AllCandidateUI(
    override var currentPage: Int = 0,
    private var view: CraftView,
    private val player: Player,
    private val result: Search.SearchResult,
    useShift: Boolean,
) : CustomCrafterUI.Pageable, InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(
        this,
        54,
        "All Candidate".toComponent()
    )
    private val elements: Map<Int, Map<Int, Pair<ItemStack, CRecipe>>>

    init {
        val e: MutableList<Pair<ItemStack, CRecipe>> = mutableListOf()
        result.vanilla()?.let { v ->
            CVanillaRecipe.fromVanilla(v as CraftingRecipe)?.let { r ->
                e.add(v.result to r)
            }
        }
        result.customs().forEach { (recipe, relation) ->
            val icon: ItemStack = recipe.getResults(
                crafterID = this.player.uniqueId,
                relate = relation,
                mapped = this.view.materials,
                shiftClicked = useShift,
                calledTimes = 1,//recipe.getMinAmount(this.view.materials, true, useShift) ?: 1,
                isMultipleDisplayCall = true
            ).firstOrNull() ?: replaceRecipeNameTemplate(
                CustomCrafterAPI.ALL_CANDIDATE_NO_DISPLAYABLE_ITEM,
                recipe.name
            )
            e.add(icon to recipe)
        }

        this.elements = e.chunked(45)
            .withIndex()
            .associate { (index, list) -> index to list }
            .map { (index, list) ->
                index to list.withIndex().associate { (i, l) -> i to l }
            }.toMap()

        this.elements[0]?.let { map ->
            map.entries.forEach { (i, pair) ->
                val (result: ItemStack, _) = pair
                this.inventory.setItem(i, result)
            }
        }

        if (this.result.size() > 45) {
            this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
        }
    }

    companion object {
        const val NEXT = 53
        const val PREVIOUS = 45
    }

    override fun flipPage() {
        if (!canFlipPage()) return
        this.currentPage++
        val targetElements: Map<Int, Pair<ItemStack, CRecipe>> = this.elements[this.currentPage]
            ?: return
        (0..<this.inventory.size).forEach { index ->
            this.inventory.setItem(
                index,
                targetElements[index]?.first ?: ItemStack.empty()
            )
        }
        this.inventory.setItem(PREVIOUS, CustomCrafterUI.PREVIOUS_BUTTON)
        if (canFlipPage()) {
            this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
        }
    }

    override fun canFlipPage(): Boolean {
        return this.currentPage < this.elements.size - 1
    }

    override fun flipBackPage() {
        if (!canFlipBackPage()) return
        this.currentPage--
        val targetElements: Map<Int, Pair<ItemStack, CRecipe>> = this.elements[this.currentPage]
            ?: return
        (0..<this.inventory.size).forEach { index ->
            this.inventory.setItem(
                index,
                targetElements[index]?.first ?: ItemStack.empty()
            )
        }
        if (canFlipBackPage()) {
            this.inventory.setItem(PREVIOUS, CustomCrafterUI.PREVIOUS_BUTTON)
        }
        this.inventory.setItem(NEXT, CustomCrafterUI.NEXT_BUTTON)
    }

    override fun canFlipBackPage(): Boolean {
        return this.currentPage > 0
    }

    override fun onClick(
        clicked: Inventory,
        event: InventoryClickEvent
    ) {
        event.isCancelled = true
        when (event.rawSlot) {
            in PREVIOUS + 1..<NEXT -> {
                return
            }

            PREVIOUS -> if (canFlipBackPage()) {
                flipBackPage()
            }

            NEXT -> if (canFlipPage()) {
                flipPage()
            }

            else -> {
                if (event.currentItem == null) {
                    return
                }
                val player: Player = event.whoClicked as? Player ?: return
                val recipe: CRecipe = this.elements[this.currentPage]?.let { icons ->
                    icons[event.rawSlot]?.second
                } ?: return
                val mappedRelation: MappedRelation? = this.result.getMergedResults()
                    .firstOrNull { (r, _) -> r == recipe }?.second

                val results: List<ItemStack> = if (recipe is CVanillaRecipe && mappedRelation == null) {
                    val minAmount: Int =
                        if (event.isShiftClick) {
                            this.view.materials.values
                                .filter { item -> item.type != Material.AIR && item.type.isItem }
                                .minOf { item -> item.amount }
                        } else 1
                    listOf(recipe.original.result.asQuantity(minAmount))
                } else if (recipe !is CVanillaRecipe && mappedRelation != null) {
                    recipe.getResults(
                        crafterID = (event.whoClicked as Player).uniqueId,
                        relate = mappedRelation,
                        mapped = view.materials,
                        shiftClicked = event.isShiftClick,
                        calledTimes = recipe.getMinAmount(view.materials, isCraftGUI = false, event.isShiftClick) ?: 1,
                        isMultipleDisplayCall = false
                    )
                } else {
                    return
                }

                if (recipe !is CVanillaRecipe) {
                    recipe.runNormalContainers(
                        crafterID = (event.whoClicked as Player).uniqueId,
                        relate = mappedRelation!!,
                        mapped = this.view.materials,
                        results = results.toMutableList(),
                        isMultipleDisplayCall = false
                    )
                }

                results.forEach { item ->
                    player.location.world.dropItem(player.location, item)
                }
                if (!this.view.result.isSimilar(ItemStack.empty())) {
                    player.location.world.dropItem(player.location, this.view.result)
                    this.view = CraftView(this.view.materials, ItemStack.empty())
                }

                this.view = this.view.getDecrementedCraftView(
                    shiftUsed = event.isShiftClick,
                    forCustomSettings = if (recipe is CVanillaRecipe) null else recipe to mappedRelation!!
                )

                val craftUI = CraftUI()
                this.view.materials.entries.forEach { (c, item) ->
                    craftUI.inventory.setItem(c.toIndex(), item)
                }
                craftUI.inventory.setItem(CustomCrafterAPI.CRAFTING_TABLE_RESULT_SLOT, this.view.result)
                player.openInventory(craftUI.inventory)
            }
        }
    }

    override fun getInventory(): Inventory = this.inventory

    private fun replaceRecipeNameTemplate(
        item: ItemStack,
        name: String
    ): ItemStack {
        val clone: ItemStack = item.clone()
        clone.lore(CustomCrafterAPI.ALL_CANDIDATE_NO_DISPLAYABLE_ITEM_LORE_SUPPLIER(name))
        return clone
    }
}
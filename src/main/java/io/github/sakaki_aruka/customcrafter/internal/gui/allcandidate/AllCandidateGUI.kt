package io.github.sakaki_aruka.customcrafter.internal.gui.allcandidate

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.event.PreCreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil.giveItems
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.ReactionProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

@Serializable
internal data class AllCandidateGUI(
    val searchResultJson: String,
    val craftViewJson: String,
    override val pages: Map<Int, Map<Int, ByteArray>>,
    override val id: String = CustomCrafterGUI.getIdOrRegister(AllCandidateGUI::class).toString(),
    override val previousPageButtonSlot: Int = 45,
    override val nextPageButtonSlot: Int = 53,
    override var currentPage: Int = 0,
    override val contextComponentSlot: Int = 49
): CustomCrafterGUI.PageableGUI, ReactionProvider {
    companion object: CustomCrafterGUI.GuiDeserializer {
        override fun from(contextItem: ItemStack): CustomCrafterGUI? {
            val json: String = contextItem.itemMeta.persistentDataContainer.get(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING
            ) ?: return null
            return Json.decodeFromString<AllCandidateGUI>(json)
        }

        override fun id(): UUID = CustomCrafterGUI.getIdOrRegister(AllCandidateGUI::class)
    }

    override fun getConstantItems(): Map<Int, ItemStack> {
        return (45..53).associateWith { i ->
            CustomCrafterGUI.UN_CLICKABLE_SLOT
        }
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        val cloned: ItemStack = contextItem.clone()
        val json: String = Json.encodeToString(this)
        cloned.editMeta { meta ->
            meta.persistentDataContainer.set(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING,
                buildJsonObject {
                    Json.parseToJsonElement(json).jsonObject
                        .forEach { (key, value) ->
                            put(key, value)
                        }
                    put("id", CustomCrafterGUI.getIdOrRegister(AllCandidateGUI::class).toString())
                }.toString()
            )
        }
        return cloned
    }

    override fun onClose(event: InventoryCloseEvent) {
        val view: CraftView = CraftView.fromJson(this.craftViewJson)
        (event.player as? Player)?.let { player ->
            player.giveItems(saveLimit = true, *view.materials.values.toTypedArray())
            player.giveItems(saveLimit = true, view.result)
        }
    }

    override fun eventReaction(
        event: InventoryClickEvent,
        ui: CustomCrafterGUI,
        inventory: Inventory,
        isTopInventory: Boolean
    ) {
        if (ui !is AllCandidateGUI) return
        event.isCancelled = true

        if (!isTopInventory) return

        val clickedItem: ItemStack = event.currentItem.takeIf { i -> i != null && !i.isEmpty } ?: return
        val player: Player = event.whoClicked as? Player ?: return
        val searchResult: Search.SearchResult = Search.SearchResult.fromJson(this.searchResultJson)
        val craftView: CraftView = CraftView.fromJson(this.craftViewJson)
        val isShiftUsed: Boolean = event.click.isShiftClick

        when (event.rawSlot) {
            previousPageButtonSlot -> {
                this.previousPage()?.let { inv: Inventory -> player.openInventory(inv) }
            }

            nextPageButtonSlot -> {
                this.nextPage()?.let { inv: Inventory -> player.openInventory(inv) }
            }

            in (0..<45) -> {
                val clickedItemIsVanilla: Boolean = this.currentPage == 0 && event.rawSlot == 0 && searchResult.vanilla() != null

                val newView: CraftView = if (clickedItemIsVanilla) {
                    craftVanilla(searchResult.vanilla()!!, craftView, isShiftUsed, player)
                } else {
                    val customsIndex: Int = this.currentPage * 45 + event.rawSlot - if (clickedItemIsVanilla) 1 else 0
                    val (recipe: CRecipe, relate: MappedRelation) = searchResult.customs()[customsIndex]
                    craftCustom(recipe, relate, craftView, isShiftUsed, player)
                }

                val preEvent = PreCreateCustomItemEvent(player, newView, event.click)
                Bukkit.getPluginManager().callEvent(preEvent)
                if (preEvent.isCancelled) return

                val newResult: Search.SearchResult = Search.search(
                    player.uniqueId,
                    newView,
                    natural = !CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE
                ) ?: return

                CreateCustomItemEvent(player, newView, newResult, event.click)

                if (newResult.size() == 0) {
                    // no result found
                    newView.materials.values.forEach { item ->
                        player.inventory.addItem(item).forEach { (_, over) ->
                            player.world.dropItem(player.location, over)
                        }
                    }
                    player.closeInventory()
                    return
                }

                val pages: MutableMap<Int, Map<Int, ByteArray>> = mutableMapOf()
                val resultsList: MutableList<ItemStack> = mutableListOf()
                newResult.vanilla()?.let { v -> resultsList.add(v.result) }
                newResult.customs().takeIf { list -> list.isNotEmpty() }
                    ?.let { list ->
                        list.forEach { (recipe, relation) ->
                            val displayItem: ItemStack = recipe.getResults(
                                crafterID = player.uniqueId,
                                relate = relation,
                                mapped = newView.materials,
                                shiftClicked = isShiftUsed,
                                calledTimes = recipe.getMinAmount(map = newView.materials, isCraftGUI = true, shift = isShiftUsed) ?: 1,
                                isMultipleDisplayCall = true
                            ).firstOrNull { item -> !item.isEmpty && item.type.isItem }
                                ?: replaceRecipeNameTemplate(CustomCrafterAPI.ALL_CANDIDATE_NO_DISPLAYABLE_ITEM, recipe.name)

                            resultsList.add(displayItem)
                        }
                    }

                resultsList.chunked(45)
                    .withIndex()
                    .forEach { (index, list) ->
                        val map: Map<Int, ByteArray> = list.map { i -> i.serializeAsBytes() }
                            .withIndex()
                            .map { (index, byteArray) -> index to byteArray }
                            .toMap()
                        pages[index] = map
                    }

                val gui = AllCandidateGUI(
                    searchResultJson = newResult.toJson(),
                    craftViewJson = newView.toJson(),
                    pages = pages
                )

                gui.firstPage()?.let { firstPage -> player.openInventory(firstPage) }
            }
        }
    }

    private fun replaceRecipeNameTemplate(
        item: ItemStack,
        name: String
    ): ItemStack {
        val clone: ItemStack = item.clone()
        clone.lore(CustomCrafterAPI.ALL_CANDIDATE_NO_DISPLAYABLE_ITEM_LORE_SUPPLIER(name))
        return clone
    }

    private fun craftVanilla(
        vanillaRecipe: Recipe,
        view: CraftView,
        isShiftUsed: Boolean,
        player: Player
    ): CraftView {
        val minAmount: Int = view.materials.values
            .filter { item -> !item.isEmpty }
            .minOf { item -> item.amount }
        val decrementAmount: Int = if (isShiftUsed) 1 else minAmount
        val result: ItemStack = vanillaRecipe.result.apply { amount *= decrementAmount }

        player.inventory.addItem(result)
            .forEach { (_, over) -> player.world.dropItem(player.location, over) }

        return view.getDecrementedCraftView(shiftUsed = isShiftUsed)
    }

    private fun craftCustom(
        recipe: CRecipe,
        relate: MappedRelation,
        view: CraftView,
        isShiftUsed: Boolean,
        player: Player
    ): CraftView {
        val minAmount: Int = recipe.getMinAmount(map = view.materials, isCraftGUI = true, shift = isShiftUsed) ?: 1

        recipe.getResults(
            crafterID = player.uniqueId,
            relate = relate,
            mapped = view.materials,
            shiftClicked = isShiftUsed,
            calledTimes = minAmount,
            isMultipleDisplayCall = true
        ).let { results ->
            recipe.runNormalContainers(
                crafterID = player.uniqueId,
                relate = relate,
                mapped = view.materials,
                results = results,
                isMultipleDisplayCall = true
            )
        }

        return view.getDecrementedCraftView(shiftUsed = isShiftUsed)
    }
}

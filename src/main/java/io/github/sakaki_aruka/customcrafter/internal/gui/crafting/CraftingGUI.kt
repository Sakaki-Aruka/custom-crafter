package io.github.sakaki_aruka.customcrafter.internal.gui.crafting

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.event.PreCreateCustomItemEvent
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.gui.CustomCrafterGUI
import io.github.sakaki_aruka.customcrafter.internal.gui.PageOpenTrigger
import io.github.sakaki_aruka.customcrafter.internal.gui.PredicateProvider
import io.github.sakaki_aruka.customcrafter.internal.gui.ReactionProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import kotlin.math.max

@Serializable
internal data class CraftingGUI(
    override val id: String = CustomCrafterGUI.getIdOrRegister(CraftingGUI::class).toString(),
    override val contextComponentSlot: Int = 35
): CustomCrafterGUI.UnPageableGUI, PageOpenTrigger, ReactionProvider {
    companion object: PredicateProvider<CustomCrafterGUI>, CustomCrafterGUI.GuiDeserializer {

        const val MAKE_BUTTON_SLOT = 35
        const val RESULT_SLOT = 44

        override fun <T : Event> predicate(event: T): CustomCrafterGUI? {
            if (event !is PlayerInteractEvent) return null
            else if (event.clickedBlock == null) return null
            else if (event.action != Action.RIGHT_CLICK_BLOCK) return null
            else if (event.clickedBlock!!.type != Material.CRAFTING_TABLE) return null

            val underCenterLoc: Location = event.clickedBlock!!.location.apply { y -= 1 }
            val underCenter: Block = underCenterLoc.block
            if (underCenter.type != CustomCrafterAPI.BASE_BLOCK) return null
            for (dx in (-1..1)) {
                for (dz in (-1..1)) {
                    val block: Block = Location(underCenter.world, underCenterLoc.x + dx, underCenterLoc.y, underCenterLoc.z + dz).block
                    if (block.type != CustomCrafterAPI.BASE_BLOCK) return null
                }
            }

            return CraftingGUI()
        }

        override fun from(contextItem: ItemStack): CustomCrafterGUI? {
            val json: String = contextItem.itemMeta.persistentDataContainer.get(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING
            ) ?: return null
            return Json.decodeFromString<CraftingGUI>(json)
        }

        override fun id(): UUID = CustomCrafterGUI.getIdOrRegister(CraftingGUI::class)
    }

    override fun write(contextItem: ItemStack): ItemStack? {
        val cloned: ItemStack = contextItem.clone()
        val json: String = Json.encodeToString<CraftingGUI>(this)

        cloned.editMeta { meta ->
            meta.persistentDataContainer.set(
                CustomCrafterGUI.CONTEXT_KEY,
                PersistentDataType.STRING,
                buildJsonObject {
                    Json.parseToJsonElement(json).jsonObject
                        .forEach { (key, value) -> put(key, value) }
                    put("id", CustomCrafterGUI.getIdOrRegister(CraftingGUI::class).toString())
                }.toString()
            )
        }
        return cloned
    }

    override fun <T : Event> getFirstPage(event: T): Inventory? {
        if (event !is PlayerInteractEvent) return null
        val inventory: Inventory = CustomCrafterAPI.getCraftingGUI()
        val contextComponent: ItemStack = getDefaultContextComponent().withType(Material.ANVIL)
        inventory.setItem(contextComponentSlot, write(contextComponent))
        return inventory
    }

    override fun eventReaction(
        event: InventoryClickEvent,
        ui: CustomCrafterGUI,
        inventory: Inventory,
        isTopInventory: Boolean
    ) {
        if (ui !is CraftingGUI) return
        val player: Player = event.whoClicked as? Player ?: return

        if (isTopInventory) {
            event.isCancelled = true
            when (event.rawSlot) {
                RESULT_SLOT -> {
                    event.currentItem?.let { item ->
                        player.inventory.addItem(item).forEach { (_, over) ->
                            player.world.dropItem(player.location, over)
                        }
                    }
                }

                MAKE_BUTTON_SLOT -> {
                    val view: CraftView = CraftView.fromInventory(inventory) ?: return
                    if (view.materials.values.none { i -> !i.isEmpty }) return
                    val preEvent = PreCreateCustomItemEvent(player, view, event.click)
                    Bukkit.getPluginManager().callEvent(preEvent)
                    if (preEvent.isCancelled) return

                    val result: Search.SearchResult = Search.search(
                        player.uniqueId,
                        inventory,
                        natural = !CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE
                    ) ?: return

                    CreateCustomItemEvent(player, view, result, event.click).callEvent()
                    if (CustomCrafterAPI.RESULT_GIVE_CANCEL) return

                    if (result.customs().isEmpty() && result.vanilla() == null) return
                    val mass: Boolean = event.click == ClickType.SHIFT_LEFT

                    if (CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE && result.size() > 1) {
                        // all-candidate page process
                    } else {
                        // normal crafting process
                        normalCrafting(result, inventory, mass, player)
                    }
                }

                in Converter.getAvailableCraftingSlotIndices() -> {
                    // material slots
                    event.isCancelled = false
                }

                else -> {
                    // un-clickable slots
                    return
                }
            }
        } else {
            // player's inventory
            event.isCancelled = false
            return
        }
    }

    private fun normalCrafting(
        result: Search.SearchResult,
        gui: Inventory,
        mass: Boolean,
        player: Player
    ) {
        if (result.customs().isEmpty() && result.vanilla() == null) return
        val mapped: Map<CoordinateComponent, ItemStack> = Converter.standardInputMapping(gui) ?: return

        if (result.customs().isNotEmpty()) {
            // custom recipe
            val (recipe, relate) = result.customs().first()
            if (getMinAmountWithoutMass(relate, recipe, mapped) <= 0) return
            var amount = 1
            recipe.items.forEach { (c, m) ->
                val inputCoordinate = relate.components.find { component -> component.recipe == c }
                    ?.input
                    ?: return
                val min: Int = getMinAmountWithoutMass(relate, recipe, mapped)
                amount = if (m.mass) 1 else (m.amount * (if (mass) min else 1))

                val slot: Int = inputCoordinate.x + inputCoordinate.y * 9
                decrement(gui, slot, amount)
            }

            recipe.getResults(player.uniqueId, relate, mapped, mass, amount, isMultipleDisplayCall = false)
                .takeIf { it.isNotEmpty() }
                ?.let { itemList ->
                    recipe.runContainers(player.uniqueId, relate, mapped, itemList, isMultipleDisplayCall = false)
                    itemList.takeIf { it.isNotEmpty() }
                        ?.forEach { item ->
                            player.inventory.addItem(item).forEach { (_, over) ->
                                player.world.dropItem(player.location, over)
                            }
                        }
                }
        } else if (result.vanilla() != null) {
            val min: Int = mapped.values.minOf { i -> i.amount }
            val amount: Int = if (mass) min else 1
            Converter.getAvailableCraftingSlotIndices()
                .filter { s -> gui.getItem(s)?.takeIf { item -> item.type != Material.AIR } != null }
                .forEach { s -> decrement(gui, s, amount) }
            val item: ItemStack = result.vanilla()!!.result.asQuantity(amount)

            player.inventory.addItem(item).forEach { (_, over) ->
                player.world.dropItem(player.location, over)
            }
        }
    }

    private fun getMinAmountWithoutMass(
        relation: MappedRelation,
        recipe: CRecipe,
        mapped: Map<CoordinateComponent, ItemStack>
    ): Int {
        return relation.components
            .filter { c -> Converter.getAvailableCraftingSlotComponents().contains(c.input) }
            .filter { c -> mapped[c.input] != null && mapped[c.input]!!.type != Material.AIR }
            .filter { c -> !recipe.items[c.recipe]!!.mass }
            .minOf { (r, i) -> mapped[i]!!.amount / recipe.items[r]!!.amount }
    }

    private fun decrement(
        gui: Inventory,
        slot: Int,
        amount: Int
    ) {
        gui.getItem(slot)?.let { item ->
            item.amount = max(item.amount - amount, 0)
        }
    }
}

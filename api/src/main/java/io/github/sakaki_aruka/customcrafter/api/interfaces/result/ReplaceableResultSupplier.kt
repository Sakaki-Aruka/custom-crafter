package io.github.sakaki_aruka.customcrafter.api.interfaces.result

import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import io.github.sakaki_aruka.customcrafter.internal.gui.crafting.CraftUI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * A subinterface of [ResultSupplier].
 *
 * @since 5.0.21
 */
interface ReplaceableResultSupplier: ResultSupplier {

    companion object {
        private fun allOf(state: ReplaceState): Map<CoordinateComponent, ReplaceState> {
            return CoordinateComponent.squareFill(6).associateWith { state }
        }
    }

    override fun supply(ctx: ResultSupplier.Context): List<ItemStack> {
        val replaceContext = Context(ctx)
        val results: MutableMap<CoordinateComponent, ReplaceState> = ConcurrentHashMap()
        val usedQueries: AtomicReference<Map<CoordinateComponent, ItemStack>?> = AtomicReference(null)
        CompletableFuture.supplyAsync({
            val player: Player? = Bukkit.getPlayer(ctx.crafterID)
            if (player == null) {
                return@supplyAsync results.putAll(allOf(ReplaceState.PLAYER_OFFLINE))
            } else {
                player.openInventory.topInventory
                    .takeIf { it.holder is CraftUI }
                    ?.let { ui ->
                        usedQueries.set(this.replaceQueries(replaceContext))
                        for ((c, replacer) in usedQueries.get()!!.entries) {
                            val slotItem = ui.getItem(c.toIndex())
                            if (slotItem != null && !slotItem.isEmpty) {
                                results[c] = ReplaceState.ITEM_ALREADY_PLACED
                                continue
                            }

                            val replace = InternalAPI.foliaLib.scheduler.runAtEntity(player) {
                                ui.setItem(c.toIndex(), replacer)
                                results[c] = ReplaceState.SUCCESS
                            }

                            replace.get(timeoutMilli(), TimeUnit.MILLISECONDS)
                        }
                        results
                    } ?: run { return@supplyAsync results.putAll(allOf(ReplaceState.UI_CLOSED)) }
            }
        }, InternalAPI.executor)
            .completeOnTimeout(Unit,this.timeoutMilli(), TimeUnit.MILLISECONDS)
            .thenRunAsync({
                (CoordinateComponent.squareFill(6) - results.keys)
                    .takeIf { it.isNotEmpty() }
                    ?.let { remaining ->
                        remaining.forEach { c ->
                            results.putIfAbsent(c, if (usedQueries.get() == null) ReplaceState.UNKNOWN else ReplaceState.TIMEOUT)
                        }
                    }

                this.replaceResultHandler(results, usedQueries.get(), replaceContext)
            }, InternalAPI.executor)

        return emptyList()
    }

    /**
     * Provides
     * @since 5.0.21
     */
    enum class ReplaceState {
        UI_CLOSED,
        PLAYER_OFFLINE,
        ITEM_ALREADY_PLACED,
        TIMEOUT,
        UNKNOWN,
        SUCCESS;

        fun isSuccess(): Boolean = this == SUCCESS
    }

    class Context(sourceContext: ResultSupplier.Context) {
        val recipe: CRecipe = sourceContext.recipe
        val relation: MappedRelation = sourceContext.relation
        val mapped: Map<CoordinateComponent, ItemStack> = sourceContext.mapped
        val shiftClicked: Boolean = sourceContext.shiftClicked
        val calledTimes: Int = sourceContext.calledTimes
        val crafterID: UUID = sourceContext.crafterID
        val isMultipleDisplayCall: Boolean = sourceContext.isMultipleDisplayCall
    }

    fun timeoutMilli(): Long = 1000L

    /**
     * Run on async
     * @return[Map] Replace mapping
     * @since 5.0.21
     */
    fun replaceQueries(ctx: Context): Map<CoordinateComponent, ItemStack>

    fun replaceResultHandler(
        results: Map<CoordinateComponent, ReplaceState>,
        usedQueries: Map<CoordinateComponent, ItemStack>?,
        usedContext: Context
    )
}
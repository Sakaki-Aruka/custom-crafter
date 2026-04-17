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
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

/**
 * A subinterface of [ResultSupplier] that writes items back into the crafting UI slots
 * after a craft completes, instead of (or in addition to) giving them to the player.
 *
 * Typical use cases include ingredient transformation (e.g. returning a modified tool
 * to the grid) and byproduct placement (e.g. returning an empty bucket after consuming
 * a filled one).
 *
 * ## Execution flow
 * 1. [supply] is called on an executor thread (async).
 * 2. [replaceQueries] is invoked on the same executor thread to build the slot-to-item mapping.
 * 3. All UI access (`getItem` / `setItem`) is dispatched to the entity's regional scheduler
 *    via a single `runAtEntity` call, ensuring thread safety for both Paper and Folia.
 * 4. After all slots are processed (or the timeout elapses), [replaceResultHandler] is
 *    called with the result of each slot operation.
 * 5. [supply] itself always returns an empty list; item delivery to the player's inventory
 *    is the responsibility of [replaceResultHandler] if needed.
 *
 * ## Timeout behaviour
 * If the entity scheduler does not execute within [timeoutMilli] milliseconds, all slots
 * that have not been processed yet are marked [ReplaceState.TIMEOUT] and
 * [replaceResultHandler] is still called.
 *
 * @since 5.0.21
 * @see ResultSupplier
 */
interface ReplaceableResultSupplier: ResultSupplier {

    companion object {
        private fun allOf(state: ReplaceState): Map<CoordinateComponent, ReplaceState> {
            return CoordinateComponent.squareFill(6).associateWith { state }
        }

        private fun filterInUiSlots(map: Map<CoordinateComponent, ItemStack>): Map<CoordinateComponent, ItemStack> {
            return map.filter { (c, _) -> c.x in (0..<6) && c.y in (0..<6) }
        }
    }

    /**
     * Executes the slot write-back logic and always returns an empty list.
     *
     * The actual placement is performed asynchronously on the entity's regional scheduler.
     * Item delivery to the player, if required, should be handled inside [replaceResultHandler].
     *
     * @param[ctx] Context provided by the recipe system
     * @return An empty list — slot placement and any player-facing delivery are handled
     *         by [replaceResultHandler]
     * @since 5.0.21
     */
    override fun supply(ctx: ResultSupplier.Context): List<ItemStack> {
        val replaceContext = Context(ctx)
        val results: MutableMap<CoordinateComponent, ReplaceState> = ConcurrentHashMap()
        val usedQueries: AtomicReference<Map<CoordinateComponent, ItemStack>?> = AtomicReference(null)
        CompletableFuture.supplyAsync({
            val player: Player = Bukkit.getPlayer(ctx.crafterID)
                ?: return@supplyAsync results.putAll(allOf(ReplaceState.PLAYER_OFFLINE))

            usedQueries.set(this.replaceQueries(replaceContext))
            val queries = usedQueries.get()!!

            try {
                InternalAPI.foliaLib.scheduler.runAtEntity(player) {
                    val ui = player.openInventory.topInventory
                        .takeIf { it.holder is CraftUI }
                    if (ui == null) {
                        results.putAll(allOf(ReplaceState.UI_CLOSED))
                        return@runAtEntity
                    }
                    for ((c, replacer) in filterInUiSlots(queries)) {
                        val slotItem = ui.getItem(c.toIndex())
                        if (slotItem != null && !slotItem.isEmpty) {
                            results[c] = ReplaceState.ITEM_ALREADY_PLACED
                        } else {
                            ui.setItem(c.toIndex(), replacer)
                            results[c] = ReplaceState.SUCCESS
                        }
                    }
                }.get(timeoutMilli(), TimeUnit.MILLISECONDS)
            } catch (_: TimeoutException) {
                queries.keys.forEach { c -> results.putIfAbsent(c, ReplaceState.TIMEOUT) }
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
     * Represents the outcome of a single slot write-back attempt.
     *
     * @since 5.0.21
     */
    enum class ReplaceState {
        /** The crafting UI was no longer open when the write-back was attempted. */
        UI_CLOSED,

        /** The crafter was offline when [supply] was called. */
        PLAYER_OFFLINE,

        /** The target slot already contained an item; the write-back was skipped. */
        ITEM_ALREADY_PLACED,

        /** The entity scheduler did not execute within [timeoutMilli] milliseconds. */
        TIMEOUT,

        /**
         * The slot state is undetermined. This occurs when the overall [supply] operation
         * timed out before [replaceQueries] returned (i.e. [replaceQueries] itself took
         * longer than [timeoutMilli] milliseconds).
         */
        UNKNOWN,

        /** The item was successfully placed into the slot. */
        SUCCESS;

        /**
         * Returns `true` if and only if this state is [SUCCESS].
         * @return[Boolean]
         * @since 5.0.21
         */
        fun isSuccess(): Boolean = this == SUCCESS
    }

    /**
     * A subset of [ResultSupplier.Context] passed to [replaceQueries] and [replaceResultHandler].
     *
     * This class exposes the same fields as [ResultSupplier.Context] but omits
     * [ResultSupplier.Context.asyncContext], which is not relevant to slot write-back logic.
     *
     * @since 5.0.21
     */
    class Context(sourceContext: ResultSupplier.Context) {
        val recipe: CRecipe = sourceContext.recipe
        val relation: MappedRelation = sourceContext.relation
        val mapped: Map<CoordinateComponent, ItemStack> = sourceContext.mapped
        val shiftClicked: Boolean = sourceContext.shiftClicked
        val calledTimes: Int = sourceContext.calledTimes
        val crafterID: UUID = sourceContext.crafterID
        val callMode: ResultSupplier.Context.CallMode = sourceContext.callMode
    }

    /**
     * Returns the maximum number of milliseconds to wait for the entity scheduler to
     * execute the slot write-back.
     *
     * If this duration elapses before execution completes, unprocessed slots are marked
     * [ReplaceState.TIMEOUT] and [replaceResultHandler] is still invoked.
     * Defaults to `1000` ms.
     *
     * @return Timeout duration in milliseconds
     * @since 5.0.21
     */
    fun timeoutMilli(): Long = 1000L

    /**
     * Builds the mapping of crafting-grid slots to items that should be written back.
     *
     * This function is called on an executor thread (async). Avoid accessing Bukkit API or
     * game state directly; use the provided [ctx] instead.
     *
     * Only coordinates within the 6×6 crafting grid (x in 0–5, y in 0–5) are processed;
     * entries outside this range are silently ignored.
     *
     * @param[ctx] Context derived from the original [ResultSupplier.Context]
     * @return A map of [CoordinateComponent] to the [ItemStack] to place in that slot
     * @since 5.0.21
     */
    fun replaceQueries(ctx: Context): Map<CoordinateComponent, ItemStack>

    /**
     * Called after all slot write-back attempts have completed (or timed out).
     *
     * Use this callback to inspect per-slot outcomes, deliver items to the player if needed,
     * or perform any post-craft side effects.
     *
     * @param[results] Per-slot outcome of the write-back operation.
     *   Contains an entry for every coordinate in the 6×6 grid.
     * @param[usedQueries] The map returned by [replaceQueries], or `null` if [replaceQueries]
     *   did not complete before the overall timeout.
     * @param[usedContext] The [Context] that was passed to [replaceQueries]
     * @since 5.0.21
     */
    fun replaceResultHandler(
        results: Map<CoordinateComponent, ReplaceState>,
        usedQueries: Map<CoordinateComponent, ItemStack>?,
        usedContext: Context
    )
}
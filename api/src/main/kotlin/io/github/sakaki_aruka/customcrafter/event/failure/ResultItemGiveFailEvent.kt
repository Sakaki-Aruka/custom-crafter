package io.github.sakaki_aruka.customcrafter.event.failure

import io.github.sakaki_aruka.customcrafter.result.ResultSupplier
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Fired when CustomCrafterAPI fails to deliver one or more result items to the player
 * (for example, when the player's inventory is full).
 *
 * Use [getResultsIfNotObtained] to claim the remaining items and handle delivery yourself.
 * Once claimed, subsequent calls to [getResultsIfNotObtained] return `null`.
 *
 * This event is not cancellable.
 *
 * @param[remainingResults] The result items that could not be delivered
 * @param[usedSupplierContext] The context used by the [ResultSupplier] that produced the items; `null` if unavailable
 * @param[isAsync] Whether this event is fired from an asynchronous thread
 * @since 5.0.21
 */
class ResultItemGiveFailEvent(
    private val remainingResults: List<ItemStack>,
    val usedSupplierContext: ResultSupplier.Context?,
    isAsync: Boolean
): Event(isAsync) {
    private val isResultsObtained: AtomicBoolean = AtomicBoolean(false)
    companion object {
        @JvmField
        val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST

    /**
     * Returns the undistributed items and marks them as obtained.
     *
     * Returns `null` if the items have already been claimed by a previous call.
     * This ensures that only one handler receives and processes the items.
     * @return[List] the remaining result items, or `null` if already claimed
     * @since 5.0.21
     */
    fun getResultsIfNotObtained(): List<ItemStack>? {
        if (this.isResultsObtained.get()) {
            return null
        }
        this.isResultsObtained.set(true)
        return this.remainingResults
    }

    /**
     * Returns whether the result items have already been claimed via [getResultsIfNotObtained].
     * @return[Boolean] `true` if the items have already been obtained
     * @since 5.0.21
     */
    fun isResultObtained(): Boolean = this.isResultsObtained.get()
}
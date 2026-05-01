package io.github.sakaki_aruka.customcrafter.event.failure

import io.github.sakaki_aruka.customcrafter.result.ResultSupplier
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicBoolean

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

    fun getResultsIfNotObtained(): List<ItemStack>? {
        if (this.isResultsObtained.get()) {
            return null
        }
        this.isResultsObtained.set(true)
        return this.remainingResults
    }

    fun isResultObtained(): Boolean = this.isResultsObtained.get()
}
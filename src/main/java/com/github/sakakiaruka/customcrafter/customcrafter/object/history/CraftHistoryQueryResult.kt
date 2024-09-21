package com.github.sakakiaruka.customcrafter.customcrafter.`object`.history

import java.time.LocalDateTime

data class CraftHistoryQueryResult(
    val name: String,
    val isLegacy: Boolean,
    val craftedDate: LocalDateTime,
    val craftedItemNBT: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CraftHistoryQueryResult) return false

        if (name != other.name) return false
        if (isLegacy != other.isLegacy) return false
        if (craftedDate != other.craftedDate) return false
        if (!craftedItemNBT.contentEquals(other.craftedItemNBT)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isLegacy.hashCode()
        result = 31 * result + craftedDate.hashCode()
        result = 31 * result + craftedItemNBT.contentHashCode()
        return result
    }
}
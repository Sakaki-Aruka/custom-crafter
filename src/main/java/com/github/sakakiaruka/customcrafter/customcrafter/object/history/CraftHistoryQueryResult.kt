package com.github.sakakiaruka.customcrafter.customcrafter.`object`.history

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import java.time.LocalDateTime

data class CraftHistoryQueryResult(
    val name: String,
    val isLegacy: Boolean,
    val craftedDate: LocalDateTime,
    val craftedItemNBT: ByteArray
) {

    companion object {
        fun CraftHistoryQueryResult.toJson(): String {
            return Json.decodeFromJsonElement<String>(buildJsonObject {
                put("name", name)
                put("is-legacy", isLegacy)
                put("crafted-date", craftedDate.toString())
                put("item-nbt-array-charset", Charsets.UTF_8.name())
                put("item-nbt-array-length", craftedItemNBT.size)
                put("crafted-item-nbt-array", String(craftedItemNBT, Charsets.UTF_8))
            })
        }
    }

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
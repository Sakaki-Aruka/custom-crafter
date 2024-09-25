package com.github.sakakiaruka.customcrafter.customcrafter.`object`.history

import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.ZoneOffset

data class CraftHistoryQueryResult(
    val name: String,
    val isLegacy: Boolean,
    val craftedDate: LocalDateTime,
    val craftedItemNBT: ByteArray
) {

    companion object {
        fun CraftHistoryQueryResult.toJson(needNBT: Boolean): String {
            val map: Map<String, Any> = mapOf(
                Pair("name", name),
                Pair("is-legacy", isLegacy),
                Pair("crafted-date", craftedDate.toEpochSecond(ZoneOffset.of("+9")) * 1000),
                Pair("item-nbt-array-charset", Charsets.UTF_8.name()),
                Pair("item-nbt-array-length", craftedItemNBT.size),
                Pair("crafted-item-nbt-array", if (needNBT) craftedItemNBT else ByteArray(0))
            )

            return Gson().toJson(map)
        }

        fun CraftHistoryQueryResult.toObjectMap(needNBT: Boolean): Map<String, Any> {
            return mapOf(
                Pair("name", name),
                Pair("is-legacy", isLegacy),
                Pair("crafted-date", craftedDate.toEpochSecond(ZoneOffset.of("+9")) * 1000),
                Pair("item-nbt-array-charset", Charsets.UTF_8.name()),
                Pair("item-nbt-array-length", craftedItemNBT.size),
                Pair("crafted-item-nbt-array", if (needNBT) craftedItemNBT else "hidden".toByteArray(charset = Charsets.UTF_8))
            )
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
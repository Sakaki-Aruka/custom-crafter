package com.github.sakakiaruka.customcrafter.customcrafter.`object`.history

import com.github.sakakiaruka.customcrafter.customcrafter.`object`.Recipe.Recipe
import com.github.sakakiaruka.customcrafter.customcrafter.util.HistoryUtil.toExposedBlob
import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.LocalDateTime
import java.util.UUID

class CraftHistory(
    val playerUUID: UUID,
    val recipeName: String,
    val recipeHash: Int,
    val createdMCVersion: String,
    val createdItem: ExposedBlob,
    val createdAmount: Int,
    val createdTimestamp: Long,
    val createdDate: LocalDateTime,
)
 {
    constructor(
        player: Player,
        recipe: Recipe,
        item: ItemStack
    ):this(
        playerUUID = player.uniqueId,
        recipeName = recipe.name,
        recipeHash = recipe.hashCode(),
        createdMCVersion = Bukkit.getMinecraftVersion(),
        createdItem = item.toExposedBlob(),
        createdAmount = item.amount,
        createdTimestamp = System.currentTimeMillis(),
        createdDate = LocalDateTime.now()
    ) {}

    fun toStringWithoutItem(prettyPrint: Boolean = false): String {
        val values: MutableMap<String, Any> = mutableMapOf()
        values["player-uuid"] = playerUUID
        values["recipe-name"] = recipeName
        values["recipe-hash"] = recipeHash
        values["mc-version"] = createdMCVersion
        values["item-nbt-byte-array-length"] = createdItem.bytes.size
        values["amount"] = createdAmount
        values["timestamp"] = createdTimestamp
        values["date"] = createdDate.toString()
        GsonBuilder().let {
            if (prettyPrint) it.setPrettyPrinting()
            return it.create().toJson(values)
        }
    }
}
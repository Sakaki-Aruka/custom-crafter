package com.github.sakakiaruka.customcrafter.customcrafter.`object`.history

import com.github.sakakiaruka.customcrafter.customcrafter.`object`.Recipe.Recipe
import com.github.sakakiaruka.customcrafter.customcrafter.util.HistoryUtil.toExposedBlob
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.LocalDateTime
import java.util.UUID

class CraftHistory(
    @Contextual val playerUUID: UUID,
    val recipeName: String,// = recipe.name
    val recipeHash: Int,// = recipe.hashCode()
    val createdMCVersion: String,// = Bukkit.getMinecraftVersion()
    @Contextual val createdItem: ExposedBlob,// = item.toExposedBlob()
    val createdAmount: Int,// = item.amount
    val createdTimestamp: Long,// = System.currentTimeMillis()
    @Contextual val createdDate: LocalDateTime,// = LocalDateTime.now()
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

    fun toStringWithoutItem(): String {
        return ""
    }
}
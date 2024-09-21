package com.github.sakakiaruka.customcrafter.customcrafter.`object`.history

import com.github.sakakiaruka.customcrafter.customcrafter.`object`.Recipe.Recipe
import com.github.sakakiaruka.customcrafter.customcrafter.util.HistoryUtil.toExposedBlob
import com.google.gson.Gson
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.LocalDateTime
import java.util.UUID

@Serializable
class CraftHistory(
    player: Player,
    recipe: Recipe,
    item: ItemStack,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val createdDate: LocalDateTime = LocalDateTime.now()
) {
    val playerUUID: UUID = player.uniqueId
    val recipeName: String = recipe.name
    val recipeHash: Int = recipe.hashCode()
    val createdMCVersion: String = Bukkit.getMinecraftVersion()
    val createdItem: ExposedBlob = item.toExposedBlob()
    val createdAmount: Int = item.amount

    fun toStringWithoutItem(): String {
        return ""
    }
}
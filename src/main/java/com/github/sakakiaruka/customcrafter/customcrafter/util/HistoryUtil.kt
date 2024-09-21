package com.github.sakakiaruka.customcrafter.customcrafter.util

import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistory
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistoryQueryResult
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistoryTable
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object HistoryUtil {
    fun ItemStack.toExposedBlob() = ExposedBlob(serializeAsBytes())

    private const val DB_URL: String = "jdbc:sqlite:./plugins/Custom_Crafter/history.db"

    fun addHistory(history: CraftHistory) {
        Database.connect(url = DB_URL)
        transaction {
            SchemaUtils.create(CraftHistoryTable)
            CraftHistoryTable.insert {
                it[uuid] = history.playerUUID
                it[recipeName] = history.recipeName
                it[recipeHash] = history.recipeHash
                it[createdTimestamp] = history.createdTimestamp
                it[createdDate] = history.createdDate
                it[createdMCVersion] = history.createdMCVersion
                it[createdItem] = history.createdItem
                it[createdAmount] = history.createdAmount
            }
        }
    }

    fun getPlayerCreatedRecipeUnique(playerUuid: UUID): List<CraftHistoryQueryResult> {
        /*
         *
         * Pair<String, Boolean> = RecipeName, isLegacy
         * > isLegacy == true -> There are some difference in current recipe and then.
         * > isLegacy = CraftHistory(from transaction).recipeHash == currentRecipe.hashCode()
         */
        val result: MutableList<CraftHistoryQueryResult> = mutableListOf()
        Database.connect(url = DB_URL)
        transaction {

            val latestCraftingEachRecipe = CraftHistoryTable
                .select(CraftHistoryTable.recipeName, CraftHistoryTable.createdTimestamp.max())
                .groupBy(CraftHistoryTable.recipeName)

            CraftHistoryTable
                .selectAll()
                .where { CraftHistoryTable.uuid eq playerUuid }
                .where { CraftHistoryTable.createdTimestamp inSubQuery latestCraftingEachRecipe }
                .orderBy(CraftHistoryTable.createdTimestamp, SortOrder.DESC)
                .forEach { line ->
                    val recipeName: String = line[CraftHistoryTable.recipeName]
                    val hash: Int = line[CraftHistoryTable.recipeHash]
                    val queryResult = CraftHistoryQueryResult(
                        recipeName,
                        SettingsLoad.NAMED_RECIPES_MAP[recipeName]?.let { it.hashCode() != hash } ?: false,
                        line[CraftHistoryTable.createdDate],
                        line[CraftHistoryTable.createdItem].bytes
                    )
                    result.add(queryResult)
                }
        }
        return result
    }
}
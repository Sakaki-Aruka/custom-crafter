package com.github.sakakiaruka.customcrafter.customcrafter.util

import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistory
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistoryQueryResult
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistoryQueryResult.Companion.toJson
import com.github.sakakiaruka.customcrafter.customcrafter.`object`.history.CraftHistoryTable
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object HistoryUtil {
    fun ItemStack.toExposedBlob() = ExposedBlob(serializeAsBytes())

    private const val DB_URL: String = "jdbc:sqlite:./plugins/Custom_Crafter/history.db"
    private const val QUERY_LIMIT: Int = 300

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

    fun getPlayerCraftedHistoryAll(playerUuid: UUID): List<CraftHistoryQueryResult> {
        /*
         *
         * collects ALL histories what are about specified id's
         */
        val result: MutableList<CraftHistoryQueryResult> = mutableListOf()
        Database.connect(url = DB_URL)

        transaction {
            CraftHistoryTable.selectAll()
                .where { CraftHistoryTable.uuid eq playerUuid }
                .forEach { line ->
                    val recipeName: String = line[CraftHistoryTable.recipeName]
                    val hash: Int = line[CraftHistoryTable.recipeHash]
                    result.add(CraftHistoryQueryResult(
                        recipeName,
                        SettingsLoad.NAMED_RECIPES_MAP[recipeName]?.let { it.hashCode() != hash } ?: false,
                        line[CraftHistoryTable.createdDate],
                        line[CraftHistoryTable.createdItem].bytes
                    ))
                }
        }
        return result
    }

    fun getPlayerCreatedHistoryUnique(playerUuid: UUID): List<CraftHistoryQueryResult> {
        /*
         *
         * > isLegacy == true -> There are some difference in current recipe and then.
         * > isLegacy = CraftHistory(from transaction).recipeHash == currentRecipe.hashCode()
         */
        Database.connect(url = DB_URL)
        lateinit var list: List<CraftHistoryQueryResult>
        transaction {
            val map: MutableMap<String, CraftHistoryQueryResult> = mutableMapOf()
            CraftHistoryTable.selectAll()
                .limit(n = QUERY_LIMIT)
                .where { CraftHistoryTable.uuid eq playerUuid }
                .forEach { line ->
                    val recipeName: String = line[CraftHistoryTable.recipeName]
                    val hash: Int = line[CraftHistoryTable.recipeHash]
                    val queryResult = CraftHistoryQueryResult(
                        recipeName,
                        SettingsLoad.NAMED_RECIPES_MAP[recipeName]?.let { it.hashCode() != hash } ?: false,
                        line[CraftHistoryTable.createdDate],
                        line[CraftHistoryTable.createdItem].bytes
                    )

                    map[queryResult.name]?.let {
                        if (queryResult.craftedDate.isAfter(it.craftedDate)) map[queryResult.name] = queryResult
                    } ?: run {
                        map[queryResult.name] = queryResult
                    }
                }
            list = map.values.toList()
        }
        return list
    }

    fun convertToJsonList(results: List<CraftHistoryQueryResult>): List<String> {
        return results.map { e -> e.toJson(needNBT = false) }.toList()
    }
}
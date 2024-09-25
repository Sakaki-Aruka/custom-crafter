package com.github.sakakiaruka.customcrafter.customcrafter.`object`.history

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.LocalDateTime
import java.util.UUID

object CraftHistoryTable: Table() {
    private val id: Column<Int> = integer("id").autoIncrement()
    val uuid: Column<UUID?> = (uuid("player-uuid")).nullable()
    val recipeName: Column<String> = text("recipe-name")
    val recipeHash: Column<Int> = integer("recipe-hash")
    val createdTimestamp: Column<Long> = long("created-timestamp")
    val createdDate: Column<LocalDateTime> = datetime("created-date")
    val createdMCVersion: Column<String> = text("created-mc-version")
    val createdItem: Column<ExposedBlob> = blob("created-item-nbt-binary")
    val createdAmount: Column<Int> = integer("created-item-amount")
    override val primaryKey = PrimaryKey(id, name = "log-id")
}
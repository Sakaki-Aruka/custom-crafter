package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.impl.util.Converter
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.blob
import org.ktorm.schema.int
import org.ktorm.schema.text

internal object CBlockDB {

    private val DB_URL = "jdbc:sqlite:${CustomCrafter.getInstance().dataPath.resolve("auto-craft.db").toUri()}"
    private val SOURCE: HikariDataSource by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = DB_URL
            driverClassName = "org.sqlite.JDBC"
            minimumIdle = 5
            maximumPoolSize = 20
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1000000
        }
        HikariDataSource(config)
    }
    private val db: Database by lazy {
        Database.connect(SOURCE)
    }

    enum class CBlockTableType {
        C_BLOCK,
        RECIPE,
        IGNORE_SLOT,
        ITEM
    }

    interface CBlockEntity: Entity<CBlockEntity> {
        val id: Int
        val worldID: String
        val x: Int
        val y: Int
        val z: Int
        val version: String
    }

    interface CBlockRecipeEntity: Entity<CBlockRecipeEntity> {
        val id: Int
        val blockID: Int
        val name: String
    }

    interface CBlockIgnoreSlotEntity: Entity<CBlockIgnoreSlotEntity> {
        val id: Int
        val blockID: Int
        val slot: Int
    }

    interface CBlockItemEntity: Entity<CBlockItemEntity> {
        val id: Int
        val blockID: Int
        val slot: Int
        val item: ByteArray
    }

    object CBlockTable: Table<CBlockEntity>("c_block") {
        val id = int("id").primaryKey().bindTo { it.id }
        val worldID = text("world_id").bindTo { it.worldID }
        val x = int("x").bindTo { it.x }
        val y = int("y").bindTo { it.y }
        val z = int("z").bindTo { it.z }
        val version = text("version").bindTo { it.version }
    }

    object CBlockRecipeTable: Table<CBlockRecipeEntity>("c_block_recipe") {
        val id = int("id").primaryKey().bindTo { it.id }
        val blockID = int("block_id").bindTo { it.blockID }
        val name = text("name").bindTo { it.name }
    }

    object CBlockIgnoreSlotTable: Table<CBlockIgnoreSlotEntity>("c_block_ignore_slot") {
        val id = int("id").primaryKey().bindTo { it.id }
        val blockID = int("block_id").bindTo { it.blockID }
        val slot = int("slot").bindTo { it.slot }
    }

    object CBlockItemTable: Table<CBlockItemEntity>("c_block_item") {
        val id = int("id").primaryKey().bindTo { it.id }
        val blockID = int("block_id").bindTo { it.blockID }
        val slot = int("slot").bindTo { it.slot }
        val item = blob("item").bindTo { it.item }
    }

    fun initTables() {
        db.useConnection { conn ->
            val statement = conn.createStatement()

            val cBlockTableCreateSQL = """
                CREATE TABLE IF NOT EXISTS c_block (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_id TEXT NOT NULL ,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    version TEXT NOT NULL
                );
            """.trimIndent()
            statement.executeUpdate(cBlockTableCreateSQL)

            val recipeTableCreateSQL = """
                CREATE TABLE IF NOT EXISTS c_block_recipe (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    block_id INTEGER NOT NULL,
                    name TEXT NOT NULL
                );
            """.trimIndent()
            statement.executeUpdate(recipeTableCreateSQL)

            val ignoreSlotsCreateSQL = """
                CREATE TABLE IF NOT EXISTS c_block_ignore_slot (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    block_id INTEGER NOT NULL,
                    slot INTEGER NOT NULL
                );
            """.trimIndent()
            statement.executeUpdate(ignoreSlotsCreateSQL)

            val itemTableCreateSQL = """
                CREATE TABLE IF NOT EXISTS c_block_item (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    block_id INTEGER NOT NULL,
                    slot INTEGER NOT NULL,
                    item BLOB NOT NULL
                );
            """.trimIndent()
            statement.executeUpdate(itemTableCreateSQL)

            statement.close()
        }
    }

    fun create(block: Block): CBlock? {
        db.insert(CBlockTable) {
            set(it.worldID, block.world.uid.toString())
            set(it.x, block.location.blockX)
            set(it.y, block.location.blockY)
            set(it.z, block.location.blockZ)
            set(it.version, CustomCrafterAPI.API_VERSION)
        }
        val id: Int = getBlockID(block) ?: return null

        db.batchInsert(CBlockIgnoreSlotTable) {
            for (index: Int in Converter.getAvailableCraftingSlotIndices()) {
                item {
                    set(it.blockID, id)
                    set(it.slot, index)
                }
            }
        }

        return CBlock()
    }

    private fun getBlockID(block: Block): Int? {
        return db.from(CBlockTable)
            .select(CBlockTable.id)
            .where { CBlockTable.worldID eq block.world.uid.toString() }
            .where { CBlockTable.x eq block.location.blockX }
            .where { CBlockTable.y eq block.location.blockY }
            .where { CBlockTable.z eq block.location.blockZ }
            .mapNotNull { r -> r[CBlockTable.id] }
            .firstOrNull()
    }

    fun updateOrCreate(block: Block, cBlock: CBlock, types: Set<CBlockTableType>) {
        if (getBlockID(block) == null) {
            create(block)
        }

        update(block, cBlock, types)
    }

    fun update(block: Block, cBlock: CBlock, types: Set<CBlockTableType>) {
        val id: Int = getBlockID(block)!!
        for (type in types) {
            when (type) {
                CBlockTableType.RECIPE -> recipesUpdate(id, cBlock)
                CBlockTableType.IGNORE_SLOT -> slotsUpdate(id, cBlock)
                CBlockTableType.ITEM -> itemsUpdate(id, cBlock)
                CBlockTableType.C_BLOCK -> {
                    update(block, cBlock, CBlockTableType.entries.minus(CBlockTableType.C_BLOCK).toSet())
                }
            }
        }
    }

    private fun recipesUpdate(id: Int, cBlock: CBlock) {
        val dbCurrent: Set<String> = db.from(CBlockRecipeTable)
            .select(CBlockRecipeTable.name)
            .where { CBlockRecipeTable.blockID eq id }
            .mapNotNull { row -> row[CBlockRecipeTable.name] }
            .toSet()
        val addToDB: Set<String> = cBlock.recipes - dbCurrent
        val deleteFromDB: Set<String> = dbCurrent - cBlock.recipes
        if (addToDB.isNotEmpty()) {
            db.batchInsert(CBlockRecipeTable) {
                for (recipe in addToDB) {
                    item {
                        set(it.blockID, id)
                        set(it.name, recipe)
                    }
                }
            }
        }
        if (deleteFromDB.isNotEmpty()) {
            for (recipe in deleteFromDB) {
                db.delete(CBlockRecipeTable) {
                    (it.blockID eq id).and(it.name eq recipe)
                }
            }
        }
    }

    private fun slotsUpdate(id: Int, cBlock: CBlock) {
        val dbCurrent: Set<Int> = db.from(CBlockIgnoreSlotTable)
            .select(CBlockIgnoreSlotTable.slot)
            .where { CBlockIgnoreSlotTable.blockID eq id }
            .mapNotNull { row -> row[CBlockIgnoreSlotTable.slot] }
            .toSet()
        val addToDB: Set<Int> = cBlock.ignoreSlots - dbCurrent
        val deleteFromDB: Set<Int> = dbCurrent - cBlock.ignoreSlots
        if (addToDB.isNotEmpty()) {
            db.batchInsert(CBlockIgnoreSlotTable) {
                for (slot in addToDB) {
                    item {
                        set(it.blockID, id)
                        set(it.slot, slot)
                    }
                }
            }
        }
        if (deleteFromDB.isNotEmpty()) {
            for (slot in deleteFromDB) {
                db.delete(CBlockIgnoreSlotTable) {
                    (it.blockID eq id).and(it.slot eq slot)
                }
            }
        }
    }

    private fun itemsUpdate(id: Int, cBlock: CBlock) {
        val dbCurrent: Map<Int, ByteArray> = db.from(CBlockItemTable)
            .select(CBlockItemTable.slot, CBlockItemTable.item)
            .where { CBlockItemTable.blockID eq id }
            .mapNotNull { row -> row[CBlockItemTable.slot] to row[CBlockItemTable.item] }
            .filter { (index, byteArray) -> index != null && byteArray != null }
            .associate { (index, byteArray) -> index!! to byteArray!! }
        val addToDB: MutableMap<Int, ByteArray> = mutableMapOf()
        val dbUpdate: MutableMap<Int, ByteArray> = mutableMapOf()
        val deleteFromDB: MutableSet<Int> = mutableSetOf()
        for (slot in dbCurrent.keys + cBlock.containedItems.keys) {
            val db: ItemStack? = dbCurrent[slot]?.let { b -> ItemStack.deserializeBytes(b) }
            val inC: ItemStack? = cBlock.containedItems[slot]
            if (db != null && inC != null) {
                // It will update only when some different are there between DB-Current and CBlock-Current.
                if (!cBlock.containedItems[slot]!!.isSimilar(ItemStack.deserializeBytes(dbCurrent[slot]!!))) {
                    dbUpdate[slot] = cBlock.containedItems[slot]!!.serializeAsBytes()
                }
            } else if (db == null && inC != null) {
                // insert
                addToDB[slot] = cBlock.containedItems[slot]!!.serializeAsBytes()
            } else if (db != null) {
                // delete (db != null && inC == null)
                deleteFromDB.add(slot)
            }
        }

        if (addToDB.isNotEmpty()) {
            db.batchInsert(CBlockItemTable) {
                for ((slot: Int, itemByteArray: ByteArray) in addToDB.entries) {
                    item {
                        set(CBlockItemTable.slot, slot)
                        set(CBlockItemTable.item, itemByteArray)
                    }
                }
            }
        }

        if (dbUpdate.isNotEmpty()) {
            db.batchUpdate(CBlockItemTable) {
                for ((slot: Int, itemByteArray: ByteArray) in dbUpdate.entries) {
                    item {
                        set(CBlockItemTable.item, itemByteArray)
                        where {
                            (it.blockID eq id).and(it.slot eq slot)
                        }
                    }
                }
            }
        }

        if (deleteFromDB.isNotEmpty()) {
            for (slot: Int in deleteFromDB) {
                db.delete(CBlockItemTable) {
                    (CBlockItemTable.blockID eq id).and(CBlockItemTable.slot eq slot)
                }
            }
        }
    }

    fun reset(block: Block, cBlock: CBlock): List<ItemStack> {
        cBlock.recipes.clear() // Init
        cBlock.ignoreSlots.addAll(Converter.getAvailableCraftingSlotIndices()) // Init (All Ignore)
        val result: List<ItemStack> = cBlock.containedItems.values.toList()
        cBlock.containedItems.clear() // Init
        cBlock.update(block, setOf(CBlockTableType.C_BLOCK))
        return result
    }

    fun allDelete(block: Block) {
        val id: Int = getBlockID(block)!!
        db.delete(CBlockTable) { it.id eq id }
        db.delete(CBlockRecipeTable) { it.blockID eq id }
        db.delete(CBlockIgnoreSlotTable) { it.blockID eq id }
        db.delete(CBlockItemTable) { it.blockID eq id }
    }

    fun read(block: Block): CBlock? {
        val id: Int = getBlockID(block) ?: return null

        val version: String = db.from(CBlockTable)
            .select(CBlockTable.version)
            .where { CBlockTable.id eq id }
            .mapNotNull { row -> row[CBlockTable.version] }
            .firstOrNull() ?: return null

        if (version !in CustomCrafterAPI.AUTO_CRAFTING_CONFIG_COMPATIBILITIES.keys) {
            return null
        } else if (version !in CustomCrafterAPI.AUTO_CRAFTING_CONFIG_COMPATIBILITIES[version]!!) {
            InternalAPI.warn("This CBlock has not compatible with this CustomCrafter version. (target=${version}, current=${CustomCrafterAPI.API_VERSION} / WorldID=${block.world.name}, X=${block.location.blockX}, Y=${block.location.blockY}, Z=${block.location.blockZ} / c_block id (DB)=$id)")
            return null
        }

        val recipes: MutableSet<String> = db.from(CBlockRecipeTable)
            .select(CBlockRecipeTable.name)
            .where { CBlockRecipeTable.blockID eq id }
            .mapNotNull { row -> row[CBlockRecipeTable.name] }
            .toMutableSet()

        val ignoreSlots: MutableSet<Int> = db.from(CBlockIgnoreSlotTable)
            .select(CBlockIgnoreSlotTable.slot)
            .where { CBlockIgnoreSlotTable.blockID eq id }
            .mapNotNull { row -> row[CBlockIgnoreSlotTable.slot] }
            .toMutableSet()

        val containedItems: MutableMap<Int, ItemStack> = db.from(CBlockItemTable)
            .select(CBlockItemTable.slot, CBlockItemTable.item)
            .where { CBlockItemTable.blockID eq id }
            .mapNotNull { row -> row[CBlockItemTable.slot] to row[CBlockItemTable.item] }
            .filter { (slot, itemByteArray) -> slot != null && itemByteArray != null }
            .associate { (slot, itemByteArray) -> slot!! to ItemStack.deserializeBytes(itemByteArray!!) }
            .toMutableMap()
        return CBlock(recipes, ignoreSlots, containedItems)
    }

    fun copy(sourceBlock: Block, destinationBlock: Block, cBlock: CBlock) {
        //
    }
}
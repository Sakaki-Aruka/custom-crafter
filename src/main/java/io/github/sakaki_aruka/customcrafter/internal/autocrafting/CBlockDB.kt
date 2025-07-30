package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.inventory.ItemStack
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.blob
import org.ktorm.schema.int
import org.ktorm.schema.text

internal object CBlockDB {

    class NotLinkedBlockException(message: String): Exception(message)

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

    interface CBlockItemsEntity: Entity<CBlockItemsEntity> {
        val id: Int
        val worldId: String
        val x: Int
        val y: Int
        val z: Int
        val items: ByteArray
    }

    object CBlockItemsTable: Table<CBlockItemsEntity>("c_block_items") {
        val id = int("id").primaryKey().bindTo { it.id }
        val worldId = text("world_id").bindTo { it.worldId }
        val x = int("x").bindTo { it.x }
        val y = int("y").bindTo { it.y }
        val z = int("z").bindTo { it.z }
        val items = blob("items").bindTo { it.items }
    }

    fun initTables() {
        db.useConnection { conn ->
            val statement = conn.createStatement()

            val createSQL = """
                CREATE TABLE IF NOT EXISTS c_block_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_id TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    items BLOB NOT NULL
                );
            """.trimIndent()
            statement.executeUpdate(createSQL)
            statement.close()
        }
    }

    fun isLinked(block: Block): Boolean {
        return getLinkId(block) != null
    }

    fun getLinkId(block: Block): Int? {
        return db.from(CBlockItemsTable)
            .select(CBlockItemsTable.id)
            .where { CBlockItemsTable.worldId eq block.world.uid.toString() }
            .where { CBlockItemsTable.x eq block.location.blockX }
            .where { CBlockItemsTable.y eq block.location.blockY }
            .where { CBlockItemsTable.z eq block.location.blockZ }
            .mapNotNull { r -> r[CBlockItemsTable.id] }
            .firstOrNull()
    }

    fun link(block: Block, items: Collection<ItemStack>): Boolean {
        if (isLinked(block)) {
            InternalAPI.info("[CBlock] The specified block had already linked. (world=${block.world.name}, x=${block.location.blockX}, y=${block.location.blockY}, z=${block.location.blockZ})")
            return false
        }

        db.insert(CBlockItemsTable) {
            set(it.worldId, block.world.uid.toString())
            set(it.x, block.location.blockX)
            set(it.y, block.location.blockY)
            set(it.z, block.location.blockZ)
            set(it.items, ItemStack.serializeItemsAsBytes(items))
        }
        return true
    }

    fun unlink(block: Block): Boolean {
        // TODO: impl here
    }

    fun getContainedItems(block: Block): List<ItemStack> {
        val linkId: Int = getLinkId(block) ?: run {
            throw NotLinkedBlockException("[CBlock] The specified block is not linked. (world=${block.world.name}, x=${block.location.blockX}, y=${block.location.blockY}, z=${block.location.blockZ})")
        }

        val byteArray: ByteArray = db.from(CBlockItemsTable)
            .select(CBlockItemsTable.items)
            .where { CBlockItemsTable.id eq linkId }
            .mapNotNull { r -> r[CBlockItemsTable.items] }
            .firstOrNull() ?: return emptyList()

        return ItemStack.deserializeItemsFromBytes(byteArray).toList()
    }

    fun addItems(block: Block, vararg items: ItemStack): Boolean {
        val linkId: Int = getLinkId(block) ?: run {
            throw NotLinkedBlockException("[CBlock] The specified block is not linked. (world=${block.world.name}, x=${block.location.blockX}, y=${block.location.blockY}, z=${block.location.blockZ})")
        }

        if (block.state !is Crafter) {
            throw IllegalArgumentException("[CBlock] The specified block is not a crafter.")
        }

        val cBlock: CBlock = CBlock.of(block.state as Crafter) ?: return false
        val containedItems: List<ItemStack> = getContainedItems(block)

        if (cBlock.slots.size < containedItems.size + items.size) {
            return false
        }

        val newItemsList: List<ItemStack> = containedItems + items.toList()
        db.update(CBlockItemsTable) {
            set(CBlockItemsTable.items, ItemStack.serializeItemsAsBytes(newItemsList))
            where { it.id eq linkId }
        }
        return true
    }
}
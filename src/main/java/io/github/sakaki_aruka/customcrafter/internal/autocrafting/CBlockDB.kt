package io.github.sakaki_aruka.customcrafter.internal.autocrafting

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.AutoCraftRecipe
import io.github.sakaki_aruka.customcrafter.impl.util.InventoryUtil
import io.github.sakaki_aruka.customcrafter.internal.InternalAPI
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Crafter
import org.bukkit.inventory.ItemStack
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.bytes
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
        val items = bytes("items").bindTo { it.items }
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

    fun linkWithoutItems(block: Block, recipe: AutoCraftRecipe): CBlock? {
        return link(block, recipe, emptyList())
    }

    fun link(block: Block, recipe: AutoCraftRecipe, items: Collection<ItemStack>): CBlock? {
        if (isLinked(block)) {
            InternalAPI.info("[CBlock] The specified block had already linked. (world=${block.world.name}, x=${block.location.blockX}, y=${block.location.blockY}, z=${block.location.blockZ})")
            return null
        }

        db.insert(CBlockItemsTable) {
            set(it.worldId, block.world.uid.toString())
            set(it.x, block.location.blockX)
            set(it.y, block.location.blockY)
            set(it.z, block.location.blockZ)
            set(it.items, ItemStack.serializeItemsAsBytes(items))
        }

        val cBlock = CBlock(
            version = CustomCrafterAPI.API_VERSION,
            name = recipe.name,
            type = recipe.type,
            publisherName = recipe.publisherPluginName,
            slots = recipe.getSlots(),
            block = block
        )

        return cBlock
    }

    fun unlink(block: Block): Boolean {
        if (!isLinked(block)) {
            return false
        }

        val linkId: Int = getLinkId(block) ?: return false

        (block.state as? Crafter)?.let { crafter ->
            CBlock.of(crafter)?.let { cBlock ->
                val dropLocation: Location = cBlock.getDropLocation()
                cBlock.getContainedItems().forEach { item ->
                    block.world.dropItem(dropLocation, item)
                }
            }
        }

        db.delete(CBlockItemsTable) {
            it.id eq linkId
        }

        val crafter: Crafter = block.state as Crafter
        crafter.persistentDataContainer.remove(InventoryUtil.fromKeyContainer(CBlock.VERSION))
        crafter.persistentDataContainer.remove(InventoryUtil.fromKeyContainer(CBlock.TYPE))
        crafter.persistentDataContainer.remove(InventoryUtil.fromKeyContainer(CBlock.SLOTS))
        crafter.persistentDataContainer.remove(InventoryUtil.fromKeyContainer(CBlock.NAME))
        crafter.persistentDataContainer.remove(InventoryUtil.fromKeyContainer(CBlock.PUBLISHER))
        crafter.update()

        return true
    }

    fun clearContainedItems(block: Block) {
        val linkId: Int = getLinkId(block) ?: run {
            throw NotLinkedBlockException("[CBlock] The specified block is not linked. (world=${block.world.name}, x=${block.location.blockX}, y=${block.location.blockY}, z=${block.location.blockZ})")
        }

        db.update(CBlockItemsTable) {
            set(CBlockItemsTable.items, ItemStack.serializeItemsAsBytes(listOf<ItemStack>()))
            where { it.id eq linkId }
        }
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
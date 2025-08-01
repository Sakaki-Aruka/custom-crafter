package io.github.sakaki_aruka.customcrafter.internal

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.active_test.CAssert
import io.github.sakaki_aruka.customcrafter.impl.test.APITest
import io.github.sakaki_aruka.customcrafter.impl.test.ConverterTest
import io.github.sakaki_aruka.customcrafter.impl.test.MultipleCandidateTest
import io.github.sakaki_aruka.customcrafter.impl.test.SearchTest
import io.github.sakaki_aruka.customcrafter.impl.test.VanillaSearchTest
import io.github.sakaki_aruka.customcrafter.internal.autocrafting.CBlockDB
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable
import java.nio.file.Paths
import java.sql.SQLException
import kotlin.io.path.createDirectory

/**
 * @suppress
 */
internal object InternalAPI {

    internal var IS_GITHUB_ACTIONS = false
    internal var GITHUB_ACTIONS_RESULT_LOG_PATH = Paths.get("plugin-test-results.txt")

    /**
     * @since 5.0.10
     */
    fun runTests() {

        IS_GITHUB_ACTIONS = try {
            System.getenv("PLATFORM")?.let { s -> s.lowercase() == "github-actions" } ?: false
        } catch (e: Exception) {
            warn(e.message ?: "GET ENV ERROR")
            false
        }

        if (IS_GITHUB_ACTIONS) {
            // run tests
            object: BukkitRunnable() {
                override fun run() {
                    val startAt = System.currentTimeMillis()
                    APITest.run()
                    ConverterTest.run()

                    VanillaSearchTest.run()

                    MultipleCandidateTest.run()
                    try {
                        SearchTest.run()
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                    val endAt = System.currentTimeMillis()
                    info("tested in ${endAt - startAt} ms")
                    CAssert.flushStoredLog(GITHUB_ACTIONS_RESULT_LOG_PATH, overrideIfExist = true)
                    Bukkit.shutdown()
                }
            }.runTaskAsynchronously(CustomCrafter.getInstance())
        }
    }

    fun setup() {

        //debug
        CustomCrafterAPI.USE_AUTO_CRAFTING_FEATURE = true

        if (CustomCrafterAPI.USE_AUTO_CRAFTING_FEATURE) {
            try {
                Class.forName("org.sqlite.JDBC")
                if (!CustomCrafter.getInstance().dataFolder.exists()) {
                    CustomCrafter.getInstance().dataFolder.toPath().createDirectory()
                    info("plugins data folder created.")
                }
                if (CustomCrafter.getInstance().dataFolder.resolve("auto-craft.db").createNewFile()) {
                    info("[AutoCraft] auto-craft.db created.")
                }
                CBlockDB.initTables()
                info("AutoCraft DB Initialize Success")
            } catch (e: SQLException) {
                warn("AutoCraft DB Initialize Error. The feature will turn off.")
                warn(e.message ?: "? Error ?")
                CustomCrafterAPI.USE_AUTO_CRAFTING_FEATURE = false
            }
        }
    }

    fun warn(str: String) = CustomCrafter.getInstance().logger.warning(str)
    fun info(str: String) = CustomCrafter.getInstance().logger.info(str)

    /**
     * AutoCraftable blocks.
     * @since 5.0.10
     */
    val AUTO_CRAFTING_BLOCKS: Set<Material> = setOf(Material.CRAFTER)

    /**
     * AutoCrafting base blocks
     * @since 5.0.10
     */
    const val AUTO_CRAFTING_BASE_BLOCK_SIDE = 3
}
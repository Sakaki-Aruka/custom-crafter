package io.github.sakaki_aruka.customcrafter.internal

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI.IS_BETA
import io.github.sakaki_aruka.customcrafter.impl.test.APITest
import io.github.sakaki_aruka.customcrafter.impl.test.ConverterTest
import io.github.sakaki_aruka.customcrafter.impl.test.MultipleCandidateTest
import io.github.sakaki_aruka.customcrafter.impl.test.SearchTest
import io.github.sakaki_aruka.customcrafter.impl.test.VanillaSearchTest
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable

/**
 * @suppress
 */
internal object InternalAPI {

    /**
     * @since 5.0.10
     */
    fun runTests() {
            if (IS_BETA) {
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
                        CustomCrafter.getInstance().logger.info("tested in ${endAt - startAt} ms")

                    }
                }.runTaskAsynchronously(CustomCrafter.getInstance())
            }
    }

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
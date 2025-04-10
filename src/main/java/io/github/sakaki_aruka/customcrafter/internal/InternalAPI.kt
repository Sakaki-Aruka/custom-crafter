package io.github.sakaki_aruka.customcrafter.internal

import io.github.sakaki_aruka.customcrafter.CustomCrafter
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI.ALL_CANDIDATE_CURRENT_PAGE_NK
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI.ALL_CANDIDATE_INPUT_NK
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI.ALL_CANDIDATE_RESULTS_NK
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI.ALL_CANDIDATE_SIGNATURE_SLOT
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI.allCandidateSignatureNK
import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI.genCCKey
import io.github.sakaki_aruka.customcrafter.api.objects.CraftView
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import io.github.sakaki_aruka.customcrafter.api.search.Search
import io.github.sakaki_aruka.customcrafter.internal.listener.InventoryCloseListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * @suppress
 */
internal object InternalAPI {
    /**
     * @since 5.0.8
     */
    fun allCandidatesSignature(): ItemStack {
        return ItemStack(Material.GRASS_BLOCK).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Signature"))
                val (key, type, value) = genCCKey()
                persistentDataContainer.set(key, type, value)
                persistentDataContainer.set(
                    allCandidateSignatureNK,
                    PersistentDataType.STRING,
                    "")
            }
        }
    }

    /**
     * set currentPage, results and input values to the target items container.
     *
     * @since 5.0.8
     */
    fun setAllCandidatesSignatureValues(
        page: Int,
        result: Search.SearchResult,
        input: CraftView,
        target: ItemStack
    ) {
        target.editMeta { meta ->
            meta.persistentDataContainer.set(
                ALL_CANDIDATE_CURRENT_PAGE_NK,
                PersistentDataType.INTEGER,
                page
            )

            meta.persistentDataContainer.set(
                ALL_CANDIDATE_RESULTS_NK,
                PersistentDataType.STRING,
                result.toJson()
            )

            meta.persistentDataContainer.set(
                ALL_CANDIDATE_INPUT_NK,
                PersistentDataType.STRING,
                input.toJson()
            )
        }
    }

    /**
     * @param[currentPage] currentPage index
     * @param[items] display items
     * @param[signature] signature item (default = null)
     * @param[placePreviousButton] place a jump to previous page button or not (default = false)
     * @param[placeNextButton] place a jump to next page button or not (default = false)
     * @since 5.0.8
     */
    internal fun getAllCandidateGUI(
        items: Map<CoordinateComponent, ItemStack>,
        currentPage: Int = 0,
        signature: ItemStack? = null,
        placePreviousButton: Boolean = false,
        placeNextButton: Boolean = false,
        dropItemsOnClose: Boolean = false
    ): Inventory {
        val gui: Inventory = Bukkit.createInventory(null, 54, Component.text("Multiple Result"))
        items.forEach { (c, item) -> gui.setItem(c.toIndex(), item) }
        val pageWrittenSignature: ItemStack = (signature ?: allCandidatesSignature()).clone()
        pageWrittenSignature.editMeta { meta ->
            meta.persistentDataContainer.set(
                ALL_CANDIDATE_CURRENT_PAGE_NK,
                PersistentDataType.INTEGER,
                currentPage)
        }

        if (!dropItemsOnClose) InventoryCloseListener.setNoDropMarker(pageWrittenSignature)

        gui.setItem(ALL_CANDIDATE_SIGNATURE_SLOT, pageWrittenSignature)

        if (placePreviousButton) {
            val previous = ItemStack(Material.ENDER_PEARL)
            previous.editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(CustomCrafter.getInstance(), "all_candidate_page_jump_type"),
                    PersistentDataType.STRING,
                    "previous"
                )
            }
            gui.setItem(45, previous)
        }
        if (placeNextButton) {
            val next = ItemStack(Material.ENDER_EYE)
            next.editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(CustomCrafter.getInstance(), "all_candidate_page_jump_type"),
                    PersistentDataType.STRING,
                    "next"
                )
            }
            gui.setItem(53, next)
        }

        /* signature item contains
         * - (Int) current page
         * - (String) input inventory Json
         * - (String) Search.Result Json
         */
        return gui
    }
}
package online.aruka.demo.recipe

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.api.objects.result.ResultSupplier
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ShapelessRecipeProvider {
    fun glowBerry(): CRecipe {
        val glowstone: CMatter = CMatterImpl.single(Material.GLOWSTONE_DUST)
        val berry: CMatter = CMatterImpl.single(Material.SWEET_BERRIES)
        /*
         * # -> glowstone
         * + -> sweet berries
         *
         * #+
         *
         * returns 1x glow berries
         */
        return CRecipeImpl.amorphous(
            name = "glow berry recipe",
            items = listOf(glowstone, berry),
            results = listOf(
                ResultSupplier.timesSingle(ItemStack.of(Material.GLOW_BERRIES))
            )
        )
    }
}
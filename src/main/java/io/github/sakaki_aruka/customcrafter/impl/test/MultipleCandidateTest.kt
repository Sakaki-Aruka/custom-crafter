package io.github.sakaki_aruka.customcrafter.impl.test

import io.github.sakaki_aruka.customcrafter.CustomCrafterAPI
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent
import org.bukkit.Material

object MultipleCandidateTest {
    fun run() {
//        registerRecipe()
    }

//    private fun registerRecipe() {
//        CustomCrafterAPI.USE_MULTIPLE_RESULT_CANDIDATE_FEATURE = true
//        val matter: CMatter = CMatterImpl.single(Material.STONE)
//        val recipe: CRecipe = CRecipeImpl(
//            "stone",
//            mapOf(CoordinateComponent.fromIndex(0) to matter),
//            type = CRecipeType.NORMAL
//        )
//        val recipe2: CRecipe = CRecipeImpl(
//            "stone2",
//            mapOf(CoordinateComponent.fromIndex(0) to matter),
//            type = CRecipeType.NORMAL
//        )
//        CustomCrafterAPI.registerRecipe(recipe)
//        CustomCrafterAPI.registerRecipe(recipe2)
//    }
}
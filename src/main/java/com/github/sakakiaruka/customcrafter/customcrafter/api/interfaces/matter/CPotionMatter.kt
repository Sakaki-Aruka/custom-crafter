package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionComponent

/**
 * This interface's implementing types will be subject to checks regarding potion effects when used as materials.
 */
interface CPotionMatter: CMatter {
    val potionComponents: Set<CPotionComponent>
}
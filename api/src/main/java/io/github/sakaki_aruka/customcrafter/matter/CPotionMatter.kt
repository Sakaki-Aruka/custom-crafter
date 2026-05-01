package io.github.sakaki_aruka.customcrafter.matter

import io.github.sakaki_aruka.customcrafter.matter.potion.CPotionComponent

/**
 * This interface's implementing types will be subject to checks regarding potion effects when used as materials.
 */
interface CPotionMatter: CMatter {
    val potionComponents: Set<CPotionComponent>
}
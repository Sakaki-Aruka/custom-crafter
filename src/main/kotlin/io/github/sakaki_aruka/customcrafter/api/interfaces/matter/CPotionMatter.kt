package io.github.sakaki_aruka.customcrafter.api.interfaces.matter

import io.github.sakaki_aruka.customcrafter.api.objects.matter.potion.CPotionComponent

/**
 * This interface's implementing types will be subject to checks regarding potion effects when used as materials.
 */
interface CPotionMatter: CMatter {
    val potionComponents: Set<CPotionComponent>
}
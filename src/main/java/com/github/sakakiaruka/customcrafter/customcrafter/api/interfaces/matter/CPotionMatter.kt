package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionComponent

interface CPotionMatter: CMatter {
    val potionComponents: Set<CPotionComponent>
}
package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.potion.CPotionComponent

interface CPotionMatter {
    val potionComponents: Set<CPotionComponent>
}
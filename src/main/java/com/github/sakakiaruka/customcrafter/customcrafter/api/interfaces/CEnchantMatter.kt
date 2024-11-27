package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent

interface CEnchantMatter {
    val enchantComponents: Set<CEnchantComponent>
}